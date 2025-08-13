package com.threeboys.toneup.socketio.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomRequest {
    private String room;

    public RoomRequest(String room) {
        this.room = room;
    }
}
