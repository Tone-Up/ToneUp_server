package com.threeboys.toneup.socketio.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.threeboys.toneup.chat.domain.ChatMessages;
import com.threeboys.toneup.chat.domain.ChatRooms;
import com.threeboys.toneup.chat.domain.MessageType;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ChatMessage {
    private String roomId;
    private String content;
    private Long senderId;
    private MessageType type;
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
//    private LocalDateTime sentAt;
    private ChatMessage(Long senderId, String content){
        this.senderId = senderId;
        this.content = content;
    }
    public ChatMessages toEntity(ChatRooms chatRooms){
        return ChatMessages.builder()
                .type(type)
                .content(content)
                .room(chatRooms)
                .senderId(senderId)
                .sentAt(LocalDateTime.now())
                .build();
    }
    public static ChatMessage create(Long senderId, String content) {
        return new ChatMessage(senderId, content);
    }
}