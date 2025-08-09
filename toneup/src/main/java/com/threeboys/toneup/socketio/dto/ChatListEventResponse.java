package com.threeboys.toneup.socketio.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatListEventResponse {
    ChatMessage chatMessage;
    int unreadCount;

    public ChatListEventResponse(ChatMessage chatMessage, int unreadCount) {
        this.chatMessage = chatMessage;
        this.unreadCount = unreadCount;
    }
}
