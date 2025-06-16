package com.threeboys.toneup.socketio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.corundumstudio.socketio.SocketIOServer;


@Configuration
public class SocketIoConfig {

    @Value("${socketio.server.hostname}")
    private String hostname;

    @Value("${socketio.server.port}")
    private int port;

    /**
     * Tomcat 서버와 별도로 돌아가는 netty 서버를 생성
     */
    @Bean
    public SocketIOServer socketIoServer() {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname(hostname);
        config.setPort(port);
//        config.setPingTimeout();//ping 후 60(default)초 안에 클라이언트의 pong 응답이 없으면 연결 끊음
//        config.setPingInterval();//ping 보내는 주기(25 초 default)
        return new SocketIOServer(config);
    }
}