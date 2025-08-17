package com.threeboys.toneup.socketio.listener;

import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class SocketIOConnectionListener {
    private final SocketIOServer server;

    /**
     * 소켓 이벤트 리스너 등록
     */
    public SocketIOConnectionListener(SocketIOServer server) {
        this.server = server;

        // 소켓 이벤트 리스너 등록
        server.addConnectListener(listenConnected());
        server.addDisconnectListener(listenDisconnected());
        //이벤트 리스너는 WebSocketAddMappingSupporter를 이용해 따로 분리
    }

    /**
     * 클라이언트 연결 리스너
     */
    public ConnectListener listenConnected() {
        return client -> {
//            Map<String, List<String>> params = client.getHandshakeData().getUrlParams();
////            log.info("connect:" + params.toString());
//            String userId = Optional.ofNullable(params.get("userId"))
//                    .flatMap(list -> list.stream().findFirst())
//                    .orElse(null);
//            if (userId != null) {
//                client.set("userId", userId);
//                log.info("User connected: userId={}, sessionId={}", userId, client.getSessionId());
//            } else {
//                log.warn("Missing userId on connect");
//            }

            String userId = client.getHandshakeData().getSingleUrlParam("userId");
            String roomId = client.getHandshakeData().getSingleUrlParam("roomId");
            String nickname = client.getHandshakeData().getSingleUrlParam("nickname");

            if (userId != null && roomId != null) {
                client.set("userId", userId);
                client.set("roomId", roomId);
                client.set("nickname", nickname);
                client.joinRoom(roomId); // 연결과 동시에 방 입장
                log.info(client.getNamespace().getName() + " : client getNamespace 확인용////////////////////////////");

                server.getNamespace("/chatmessage").getRoomOperations(roomId).sendEvent("joinRoom", "유저 : " + nickname + "이 입장했습니다.");
                client.getNamespace().getRoomOperations(roomId).sendEvent("joinRoom", "유저 : " + nickname + "이 입장했습니다.");
                server.getNamespace("").getRoomOperations(roomId).sendEvent("joinRoom", "유저 : " + nickname + "이 입장했습니다.");


                log.info("client{}가 방 : {} 에 입장했습니다.", client.getSessionId(), roomId);
                log.info("User {} joined room {} sessionId {}", userId, roomId, client.getSessionId());
            } else {
                log.warn("Missing userId on connect");
            }
            Collection<SocketIONamespace> namespaces = server.getAllNamespaces();
            for (SocketIONamespace ns : namespaces) {
                log.info("Namespace: '{}'", ns.getName());
            }
        };
    }

    /**
     * 클라이언트 연결 해제 리스너
     */
    public DisconnectListener listenDisconnected() {
        return client -> {
            String sessionId = client.getSessionId().toString();
            Set<String> rooms =  client.getAllRooms();
            String roomId = String.valueOf(rooms.stream().findFirst());
            client.leaveRoom(roomId);

//            String nickname = client.getHandshakeData().getSingleUrlParam("nickname");
//            client.getNamespace().getRoomOperations(roomId).sendEvent("leaveRoom", "유저 : " + nickname + "이 나갔습니다.");

            log.info("disconnect: " + sessionId);
            client.disconnect();
        };
    }
}
