package com.threeboys.toneup.socketio;

import com.corundumstudio.socketio.SocketIOClient;
import com.threeboys.toneup.socketio.DTO.ChatMessage;
import com.threeboys.toneup.socketio.DTO.RoomRequest;

@SocketController
public class RoomController {

    @SocketMapping(endpoint = "joinRoom", requestCls = RoomRequest.class)
    public void joinRoom(SocketIOClient client, RoomRequest request) {
        String room = request.getRoom();
        client.joinRoom(room);
        client.sendEvent("joinRoom", "방 '" + room + "'에 입장했습니다.");
    }

    @SocketMapping(endpoint = "chat", requestCls = ChatMessage.class)
    public void chat(SocketIOClient client, ChatMessage message) {
        String room = message.getRoom();
        String content = message.getMessage();

        client.getNamespace().getRoomOperations(room)
                .sendEvent("chat", message); // room에만 전송
    }
}
