package com.threeboys.toneup.socketio.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
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

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class FcmService {
    private final UserRepository userRepository;
    private final DeviceTokenRepository deviceTokenRepository;

    public void sendMessage(ChatMessage message, Set<Long> userIds) {

//        try{
//            FileInputStream serviceAccount =
//                    new FileInputStream("firebase/service-key.json");
//
//            FirebaseOptions options = FirebaseOptions.builder()
//                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//                    .build();
//
//            FirebaseApp.initializeApp(options);
//
//        } catch (Exception e) {
//            throw new RuntimeException(e.getMessage());
//        }

        Long senderId = message.getSenderId();

        //senderId로 유저 닉네임 조회
        UserEntity userEntity = userRepository.findById(senderId).orElseThrow(()-> new UserNotFoundException(senderId));
        String senderNickname = userEntity.getNickname();

        //토큰 조회(chatroom_user에서 유저 찾아서 device_token토큰에서 isActive 한 토큰만 조회)
        List<String> registrationTokens = deviceTokenRepository.findActiveTokensByUserIds(userIds);
        registrationTokens.set(0,registrationTokens.getFirst().trim());
//        System.out.println(registrationTokens.getFirst());
        //추후에 단체 톡방 위해 MulticastMessage 방식으로 일단 구현
        try {
//            Notification notification = Notification.builder()
//                    .setTitle(senderNickname)
//                    .setBody(message.getContent())
//                    .build();
//
//            Message castMessage = Message.builder()
//                    .setToken(registrationTokens.getFirst())
//                    .setNotification(notification)
//                    .setApnsConfig(
//                            ApnsConfig.builder()
//                                    .putHeader("apns-priority", "10")
//                                    .putHeader("apns-push-type", "alert")
//                                    .setAps(
//                                            Aps.builder()
//                                                    .setContentAvailable(true)
//                                                    .setSound("default")
//                                                    .build()
//                                    )
//                                    .build()
//                    )
//                    .build();
//            FirebaseMessaging.getInstance().send(castMessage);
            MulticastMessage multicastMessage = createMulticastMessage(message, senderNickname, registrationTokens);

            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(multicastMessage);
//            System.out.println(response.getSuccessCount() + " : 1이 나와야함");
//            System.out.println(response.getFailureCount() + " : 0이 나와야함");
            if (response.getFailureCount() > 0) {
                List<SendResponse> responses = response.getResponses();
//                System.out.println(response.getResponses().getFirst().getException().getMessage());
//                System.out.println(response.getResponses().getFirst().getException().getMessagingErrorCode());
//                System.out.println(response.getResponses().getFirst().getException().getCause());
//                System.out.println(response.getResponses().getFirst().getException().getHttpResponse());
//                System.out.println(response.getResponses().getFirst().getException().getLocalizedMessage());
//
//                for (StackTraceElement element : response.getResponses().getFirst().getException().getStackTrace()) {
//                    System.out.println(element);
//                };

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
//            catch (FirebaseMessagingException ex) {
//
//            throw new RuntimeException(ex);
//        }
    }
    catch (FirebaseMessagingException e){
            log.error("FCM Send error : " , e);
        }

    }

    private  MulticastMessage createMulticastMessage(ChatMessage message, String senderNickname, List<String> registrationTokens){

        String roomId = message.getRoomId();
        Long senderId = message.getSenderId();
        String content = message.getContent();
        String type = message.getType().toString();

        return MulticastMessage.builder()
                .setNotification(Notification.builder()
                        .setTitle(senderNickname + "님의 메시지")
                        .setBody(content)
                        .build())
//                .setApnsConfig(ApnsConfig.builder()
//                        .putHeader("apns-priority", "10") // 실시간 푸시
//                        .putHeader("apns-push-type", "alert")  // ★ 필수
//                        .setAps(Aps.builder()
//                                .setContentAvailable(true) // 백그라운드 푸시 가능
//                                .setSound("default")       // 사운드 설정
//                                .build())
//                        .build())
//                .putData("senderNickname", senderNickname)
//                .putData("senderId", senderId.toString())
//                .putData("content", content)
//                .putData("type", type)
//                .putData("roomId", roomId)
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
