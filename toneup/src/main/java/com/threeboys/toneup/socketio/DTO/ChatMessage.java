package com.threeboys.toneup.socketio.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessage {
    private String room;
    private String message;
}