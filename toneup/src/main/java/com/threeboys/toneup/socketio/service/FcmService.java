package com.threeboys.toneup.socketio.service;

import com.google.firebase.messaging.*;
import com.threeboys.toneup.chat.domain.ChatRoomUser;
import com.threeboys.toneup.socketio.DTO.ChatMessage;
import com.threeboys.toneup.socketio.repository.DeviceTokenRepository;
import com.threeboys.toneup.user.entity.UserEntity;
import com.threeboys.toneup.user.exception.UserNotFoundException;
import com.threeboys.toneup.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Slf4j
@RequiredArgsConstructor
@Service
public class FcmService {
    private final UserRepository userRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    public void sendMessage(ChatMessage message) {
        Long roomId = message.getRoomId();
        Long senderId = message.getSenderId();
        String content = message.getContent();

        //senderId로 유저 닉네임 조회
        UserEntity userEntity = userRepository.findById(senderId).orElseThrow(()-> new UserNotFoundException(senderId));
        String senderNickname = userEntity.getNickname();

        //토큰 조회(chatroom_user에서 유저 찾아서 device_token토큰에서 isActive 한 토큰만 조회)
        List<String> registrationTokens = deviceTokenRepository.findActiveTokensByRoomId(roomId);
//        List<String> registrationTokens =chatRoomUsers.stream().map(chatRoomUser::get);

        //추후에 단체 톡방 위해 MulticastMessage 방식으로 일단 구현
        try {
            MulticastMessage multicastMessage = MulticastMessage.builder()
                    .putData("senderNickname", senderNickname)
                    .putData("content", content)
                    .addAllTokens(registrationTokens)
                    .build();
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
            }
        }catch (FirebaseMessagingException e){
            log.error("FCM Send error : " , e);
        }

    }
}
