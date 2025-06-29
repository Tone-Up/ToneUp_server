package com.threeboys.toneup.security.exception;

import com.threeboys.toneup.common.response.exception.ErrorMessages;

public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException() {
        super(ErrorMessages.INVALID_REFRESH_TOKEN);
    }
}
