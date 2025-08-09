package com.threeboys.toneup.socketio.dto;

import com.threeboys.toneup.chat.domain.ChatMessages;
import com.threeboys.toneup.chat.domain.ChatRooms;
import com.threeboys.toneup.chat.domain.MessageType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatMessage {
    private String roomId;
    private String content;
    private Long senderId;
    private MessageType type;
    private ChatMessage(Long senderId, String content){
        this.senderId = senderId;
        this.content = content;
    }
    public ChatMessages toEntity(ChatRooms chatRooms){
        return ChatMessages.builder()
                .type(type)
                .content(content)
                .roomId(chatRooms)
                .senderId(senderId)
                .build();
    }
    public static ChatMessage create(Long senderId, String content) {
        return new ChatMessage(senderId, content);
    }
}