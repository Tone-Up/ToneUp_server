package com.threeboys.toneup.common.config;

import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.threeboys.toneup.socketio.dto.ChatMessage;
import com.threeboys.toneup.socketio.dto.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatRedisSubscriber implements MessageListener {
    private final SocketIOServer socketIOServer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            ChatMessageResponse chatMessageResponse = objectMapper.readValue(message.getBody(), ChatMessageResponse.class);

            String roomId = chatMessageResponse.getChatMessage().getRoomId();
            socketIOServer.getNamespace("/")
                    .getRoomOperations(roomId)
                    .sendEvent("chat", chatMessageResponse);

            log.info("Redis 구독 → Room {} 으로 메시지 전송: {}", roomId, chatMessageResponse.getChatMessage().getContent());

        } catch (Exception e) {
            log.error("Redis 메시지 처리 실패", e);
        }
    }
}
