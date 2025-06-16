package com.threeboys.toneup.socketio.controller;

import com.corundumstudio.socketio.SocketIOClient;
import com.threeboys.toneup.socketio.DTO.ChatMessage;
import com.threeboys.toneup.socketio.DTO.RoomRequest;
import com.threeboys.toneup.socketio.annotation.SocketController;
import com.threeboys.toneup.socketio.annotation.SocketMapping;

@SocketController
public class RoomController {

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
        // 상대방이 현재 이 방에 존재하면
            //unread_count 증가 없이 db 저장 후 메시지 전송
        //아닐경우(이 방에 존재하지 않을경우)
            //unread_count 증가 하고 db 저장 후 fcm 푸시 알림 전송


        client.getNamespace().getRoomOperations(String.valueOf(roomId))
                .sendEvent("chat", message); // room에만 전송
    }
//    @SocketMapping(endpoint = "quit",requestCls = )
//    public void quit(SocketIOClient client, )
}
