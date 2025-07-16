package com.threeboys.toneup.user.exception;

import com.threeboys.toneup.common.response.exception.ErrorMessages;

public class DuplicateNicknameException extends RuntimeException {
    public DuplicateNicknameException() {
        super(ErrorMessages.NICKNAME_ALREADY_EXISTS);
    }
}
