package com.threeboys.toneup.socketio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JoinRoomResponse {
    private String opponentId;
    private String roomId;
}
