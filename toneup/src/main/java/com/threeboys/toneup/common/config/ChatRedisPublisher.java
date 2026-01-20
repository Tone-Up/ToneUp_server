package com.threeboys.toneup.common.config;

import com.threeboys.toneup.socketio.dto.ChatMessage;
import com.threeboys.toneup.socketio.dto.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatRedisPublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    public void publish(ChatMessageResponse message) {
        String topic = "chat-room-" + message.getChatMessage().getRoomId();
        redisTemplate.convertAndSend(topic, message);
    }
}
