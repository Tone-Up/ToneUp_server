package com.threeboys.toneup.socketio.controller;

import com.corundumstudio.socketio.SocketIOClient;
import com.threeboys.toneup.chat.service.ChatMessagesService;
import com.threeboys.toneup.socketio.DTO.ChatMessage;
import com.threeboys.toneup.socketio.DTO.RoomRequest;
import com.threeboys.toneup.socketio.annotation.SocketController;
import com.threeboys.toneup.socketio.annotation.SocketMapping;
import com.threeboys.toneup.socketio.service.FcmService;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Objects;
@RequiredArgsConstructor
@SocketController
public class RoomController {

    private final ChatMessagesService chatMessagesService;
    private final FcmService fcmService;
    @SocketMapping(endpoint = "joinRoom", requestCls = RoomRequest.class)
    public void joinRoom(SocketIOClient client, RoomRequest request) {
        String room = request.getRoom();
        client.joinRoom(room);
        // db 에서 안읽은 메시지 읽음 처리
        // 읽은 메시지 db에서 가져오기
        // 클라한테 읽은 메시지들 이벤트로 보내기
        //이후 상대방에게 읽음 알림? 보내기??

        client.sendEvent("joinRoom", "방 '" + room + "'에 입장했습니다.");
    }

    @SocketMapping(endpoint = "chat", requestCls = ChatMessage.class)
    public void chat(SocketIOClient client, ChatMessage message) {
        Long roomId = message.getRoomId();
        String content = message.getContent();
        Long senderId = message.getSenderId();
        // 상대방이 현재 이 방에 존재하면
        Collection<SocketIOClient> clients = client.getNamespace().getRoomOperations(roomId.toString()).getClients();
        int roomSize = clients.size();
        boolean isReceiverInRoom = clients.stream()
                .anyMatch(c -> !Objects.equals(c.get("userId"), senderId));
        if(isReceiverInRoom){
            //unread_count 증가 없이(unread_count default 0으로 설정) db 저장 후
            chatMessagesService.saveMessage(message, isReceiverInRoom, roomSize);
            //소켓 통신 메시지 전송
            client.getNamespace().getRoomOperations(roomId.toString())
                    .sendEvent("chat", message); // room에만 전송
        }//아닐경우(이 방에 존재하지 않을경우)
        else{
            //unread_count 증가 하고(방 인원 수 -1 현재 방 인원수 2명 기준) db 저장 후
            chatMessagesService.saveMessage(message, isReceiverInRoom, roomSize);
            // fcm 푸시 알림 전송  추후 단체 톡방 위해 MulticastMessage으로 알림 전송
            fcmService.sendMessage(message);
        }



    }
//    @SocketMapping(endpoint = "quit",requestCls = )
//    public void quit(SocketIOClient client, )
}
