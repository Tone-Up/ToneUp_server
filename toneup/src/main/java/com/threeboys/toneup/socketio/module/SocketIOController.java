package com.threeboys.toneup.socketio.module;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DisconnectListener;

import lombok.extern.slf4j.Slf4j;

/**
 * SocketIOController.
 */
@Component
@Slf4j
public class SocketIOController {
    private final SocketIOServer server;

    /**
     * 소켓 이벤트 리스너 등록
     */
    public SocketIOController(SocketIOServer server) {
        this.server = server;

        // 소켓 이벤트 리스너 등록
        server.addConnectListener(listenConnected());
        server.addDisconnectListener(listenDisconnected());
//        server.addEventListener("chat", String.class, (client, data, ackSender) -> {
//            log.info("메시지 수신: {}", data);
//            client.sendEvent("chat", "서버로부터 응답: " + data);
//            server.getBroadcastOperations().sendEvent("chat", data);
//        });
    }

    /**
     * 클라이언트 연결 리스너
     */
    public ConnectListener listenConnected() {
        return (client) -> {
            Map<String, List<String>> params = client.getHandshakeData().getUrlParams();
            log.info("connect:" + params.toString());
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