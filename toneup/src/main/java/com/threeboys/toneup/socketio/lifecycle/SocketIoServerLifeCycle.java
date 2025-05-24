package com.threeboys.toneup.socketio.lifecycle;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.corundumstudio.socketio.SocketIOServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;


@Component
@RequiredArgsConstructor
public class SocketIoServerLifeCycle {
    private final SocketIOServer server;

    @PostConstruct
    public void start() {
        server.start();
    }


    @PreDestroy
    public void stop() {
        server.stop();
    }
}