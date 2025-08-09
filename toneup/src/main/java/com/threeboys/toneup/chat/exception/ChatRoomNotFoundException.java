package com.threeboys.toneup.chat.exception;

import com.threeboys.toneup.common.response.exception.ErrorMessages;

public class ChatRoomNotFoundException extends RuntimeException {
    public ChatRoomNotFoundException() {
        super(String.format(ErrorMessages.ROOM_NOT_FOUND));
    }
}