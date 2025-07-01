package com.threeboys.toneup.feed.exception;

import com.threeboys.toneup.common.response.exception.ErrorMessages;

public class InvalidImageCountException extends RuntimeException {
    public InvalidImageCountException() {
        super(ErrorMessages.INVALID_IMAGE_COUNT);
    }
}
