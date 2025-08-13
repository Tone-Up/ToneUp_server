package com.threeboys.toneup.socketio.controller;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.threeboys.toneup.chat.service.ChatMessagesService;
import com.threeboys.toneup.socketio.dto.ChatListEventResponse;
import com.threeboys.toneup.socketio.annotation.SocketController;
import com.threeboys.toneup.socketio.annotation.SocketMapping;
import com.threeboys.toneup.socketio.dto.ChatMessage;
import com.threeboys.toneup.socketio.dto.RoomRequest;
import com.threeboys.toneup.socketio.service.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@SocketController
@Slf4j
public class RoomController {

    private final ChatMessagesService chatMessagesService;
    private final FcmService fcmService;

//    @SocketMapping(endpoint = "joinRoom", requestCls = RoomRequest.class)
//    public void joinRoom(SocketIOClient client, RoomRequest request) {
//        String room = request.getRoom();
//        client.joinRoom(room);
//        // db 에서 안읽은 메시지 읽음 처리
//        // 읽은 메시지 db에서 가져오기
//        // 클라한테 읽은 메시지들 이벤트로 보내기
//        //이후 상대방에게 읽음 알림? 보내기??
//
//        client.getNamespace().getRoomOperations(room).sendEvent("joinRoom", "방 '" + room + "'에 입장했습니다.");
//        log.info("client{}가 방 : {} 에 입장했습니다.", client.getSessionId(), room);
//    }

//    @SocketMapping(endpoint = "leaveRoom", requestCls = RoomRequest.class)
//    public void leaveRoom(SocketIOClient client, RoomRequest request) {
//        String roomId = request.getRoom();
//        client.leaveRoom(roomId);
//
//        String nickname = client.getHandshakeData().getSingleUrlParam("nickname");
//        client.getNamespace().getRoomOperations(roomId).sendEvent("leaveRoom", "유저 : " + nickname + "이 나갔습니다.");
//        log.info("client{}가 방 : {} 에서 나갔습니다.", client.getSessionId(), roomId);
//    }

    @SocketMapping(endpoint = "chat", requestCls = ChatMessage.class)
    public void chat(SocketIOClient client, ChatMessage message) {
        Long roomId = Long.parseLong(message.getRoomId());
        String content = message.getContent();
        Long senderId = message.getSenderId();

        // 상대방이 현재 이 방에 존재하면
        Collection<SocketIOClient> clients = client.getNamespace().getRoomOperations(roomId.toString()).getClients();

        // 현재 방에 연결된 유저들 ID 수집
        Set<Long> connectedUserIds = clients.stream()
                .filter(SocketIOClient::isChannelOpen)
                .map(c -> Long.parseLong(c.get("userId")))
                .collect(Collectors.toSet());

        // 해당 채팅방의 모든 유저 ID (ex. DB에 저장되어 있는)
        Set<Long> roomUserIds = chatMessagesService.getUserIdsInRoom(roomId); // ex. [1, 2]
        int roomSize = roomUserIds.size();

        // 상대 유저들 ID (나 제외)
        Set<Long> otherUserIds = roomUserIds.stream()
                .filter(id -> !id.equals(senderId))
                .collect(Collectors.toSet());

        Set<Long> notConnectedUserIds = roomUserIds.stream()
                .filter(id -> !connectedUserIds.contains(id))
                .collect(Collectors.toSet());

//        boolean isReceiverInRoom = clients.stream().filter(SocketIOClient::isChannelOpen)
//                .anyMatch(c -> !Objects.equals(c.get("userId"), senderId));
//        long connectedReceivers = clients.stream()
//                .filter(SocketIOClient::isChannelOpen)
//                .filter(c -> {
//                    Long userId = Long.parseLong(c.get("userId"));
//                    return !userId.equals(senderId);
//                })
//                .count();


        int unreadCount = roomSize - connectedUserIds.size();
        log.info("unreadCount : {}", unreadCount);

        //(방에 있는 유저들에게) 소켓 통신 메시지 전송
        client.getNamespace().getRoomOperations(roomId.toString())
                .sendEvent("chat", message);

        if(unreadCount ==0){
            //unread_count 증가 없이(unread_count default 0으로 설정) db 저장 후
            chatMessagesService.saveMessage(message, true, unreadCount);
        }//아닐경우(이 방에 존재하지 않을경우)
        else{
            //unread_count 증가 하고(방 인원 수 -1 현재 방 인원수 2명 기준) db 저장 후
            chatMessagesService.saveMessage(message, false, unreadCount);

            //웹소켓 연결만 되어있는 유저들(채팅 리스트에만 있는 상태)에게 사용자가 읽지 않은 메시지 수 증가해서 이벤트 보내기
            //즉, 방 밖에 있는 유저들에게 chatListUpdate 전송
//            Collection<SocketIOClient> allConnectedClients = client.getNamespace().getAllClients();
//
//            for (SocketIOClient checkClient : allConnectedClients) {
//                if (!client.isChannelOpen()) continue;
//
//                Long userId = Long.parseLong(checkClient.get("userId"));
//                if (otherUserIds.contains(userId)) {
//                    int userUnreadCount = 1;//chatMessagesService.userUnreadCount(userId)+
//                    ChatListEventResponse updateDto = new ChatListEventResponse(message, userUnreadCount);
//                    client.sendEvent("chatListUpdate", updateDto);
//                }
//            }
            //방 유저이지만 소켓 연결 안된 유저들에게 fcm 푸시 알림 전송  추후 단체 톡방 위해 MulticastMessage 방식으로 알림 전송
            fcmService.sendMessage(message, notConnectedUserIds);
        }

    }


}
