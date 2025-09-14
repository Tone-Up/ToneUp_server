package com.threeboys.toneup.socketio.listener;

import com.corundumstudio.socketio.SocketIOServer;
import com.threeboys.toneup.socketio.dto.JoinRoomResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RPatternTopic;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisEventSubscriber {

    private final RedissonClient redissonClient;
    private final SocketIOServer socketIOServer;

    @PostConstruct
    public void subscribeRoomEvents() {
        RPatternTopic patternTopic = redissonClient.getPatternTopic("room:*");
        patternTopic.addListener(JoinRoomResponse.class, (pattern, channel, msg) -> {
            String roomId = msg.getRoomId();
            socketIOServer.getRoomOperations(roomId).sendEvent("joinRoom", msg);
            log.info("[Redis Sync] Broadcast joinRoom to room={} user={}", msg.getRoomId(), msg.getOpponentId());
        });
    }
}
