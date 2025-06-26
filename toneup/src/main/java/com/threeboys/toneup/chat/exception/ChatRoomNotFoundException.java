package com.threeboys.toneup.chat.exception;

import com.threeboys.toneup.common.response.exception.ErrorMessages;

public class ChatRoomNotFoundException extends RuntimeException {
    public ChatRoomNotFoundException(Long roomId) {
        super(String.format(ErrorMessages.ROOM_NOT_FOUND, roomId));
    }
}