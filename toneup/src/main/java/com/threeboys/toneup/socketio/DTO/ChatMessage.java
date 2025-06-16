package com.threeboys.toneup.socketio.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessage {
    private Long roomId;
    private String content;
    private Long senderId;

    private ChatMessage(Long senderId, String content){
        this.senderId = senderId;
        this.content = content;
    }
    public static ChatMessage create(Long senderId, String content) {
        return new ChatMessage(senderId, content);
    }
}