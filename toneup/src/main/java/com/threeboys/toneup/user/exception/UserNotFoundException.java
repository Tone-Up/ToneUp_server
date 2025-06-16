package com.threeboys.toneup.user.exception;

import com.threeboys.toneup.common.response.exception.ErrorMessages;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long userId) {
        super(String.format(ErrorMessages.USER_NOT_FOUND, userId));
    }
}
