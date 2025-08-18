package com.threeboys.toneup.socketio.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    @JsonUnwrapped
    private ChatMessage chatMessage;
    private int unreadCount;
}
