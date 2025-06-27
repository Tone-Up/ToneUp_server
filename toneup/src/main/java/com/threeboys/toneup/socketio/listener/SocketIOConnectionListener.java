package com.threeboys.toneup.socketio.listener;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
            Map<String, List<String>> params = client.getHandshakeData().getUrlParams();
//            log.info("connect:" + params.toString());
            String userId = Optional.ofNullable(params.get("userId"))
                    .flatMap(list -> list.stream().findFirst())
                    .orElse(null);

            if (userId != null) {
                client.set("userId", userId);
                log.info("User connected: userId={}, sessionId={}", userId, client.getSessionId());
            } else {
                log.warn("Missing userId on connect");
            }
        };
    }

    /**
     * 클라이언트 연결 해제 리스너
     */
    public DisconnectListener listenDisconnected() {
        return client -> {
            String sessionId = client.getSessionId().toString();
            log.info("disconnect: " + sessionId);
            client.disconnect();
        };
    }
}
