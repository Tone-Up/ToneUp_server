package com.threeboys.toneup.socketio.service;

import com.google.firebase.messaging.*;
import com.threeboys.toneup.socketio.domain.DeviceToken;
import com.threeboys.toneup.socketio.dto.ChatMessage;
import com.threeboys.toneup.socketio.repository.DeviceTokenRepository;
import com.threeboys.toneup.user.entity.UserEntity;
import com.threeboys.toneup.user.exception.UserNotFoundException;
import com.threeboys.toneup.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class FcmService {
    private final UserRepository userRepository;
    private final DeviceTokenRepository deviceTokenRepository;

    public void sendMessage(ChatMessage message, Set<Long> userIds) {
        Long senderId = message.getSenderId();

        //senderId로 유저 닉네임 조회
        UserEntity userEntity = userRepository.findById(senderId).orElseThrow(()-> new UserNotFoundException(senderId));
        String senderNickname = userEntity.getNickname();

        //토큰 조회(chatroom_user에서 유저 찾아서 device_token토큰에서 isActive 한 토큰만 조회)
        List<String> registrationTokens = deviceTokenRepository.findActiveTokensByUserIds(userIds);

        //추후에 단체 톡방 위해 MulticastMessage 방식으로 일단 구현
        try {
            MulticastMessage multicastMessage = createMulticastMessage(message, senderNickname, registrationTokens);

            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(multicastMessage);
            if (response.getFailureCount() > 0) {
                List<SendResponse> responses = response.getResponses();
                List<String> failedTokens = new ArrayList<>();
                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        // The order of responses corresponds to the order of the registration tokens.
                        failedTokens.add(registrationTokens.get(i));
                    }
                }
                //실패 토큰 재전송 시도
                FirebaseMessaging.getInstance().sendEachForMulticast(createMulticastMessage(message, senderNickname, failedTokens));
            }
        }catch (FirebaseMessagingException e){
            log.error("FCM Send error : " , e);
        }

    }

    private  MulticastMessage createMulticastMessage(ChatMessage message, String senderNickname, List<String> registrationTokens){

        String roomId = message.getRoomId();
        Long senderId = message.getSenderId();
        String content = message.getContent();
        String type = message.getType().toString();

        return MulticastMessage.builder()
                .putData("senderNickname", senderNickname)
                .putData("senderId", senderId.toString())
                .putData("content", content)
                .putData("type", type)
                .putData("roomId", roomId)
                .addAllTokens(registrationTokens)
                .build();

    }

    @Transactional
    public void activateTokenForUser(Long userId, String fcmToken) {
        if(deviceTokenRepository.existsByToken(fcmToken)){
            List<DeviceToken> userTokens = deviceTokenRepository.findByUserId(userId);

            for (DeviceToken fcm : userTokens) {
                if (fcm.getToken().equals(fcmToken)) {
                    if (!fcm.isActive()) {
                        fcm.changeIsActive(true);
                    }
                } else {
                    if (fcm.isActive()) {
                        fcm.changeIsActive(false);
                    }
                }
            }
        }else{
            UserEntity user = userRepository.getReferenceById(userId);

            DeviceToken deviceToken = new DeviceToken(user,fcmToken,"PHONE", true);
            deviceTokenRepository.save(deviceToken);
        }
    }
}
