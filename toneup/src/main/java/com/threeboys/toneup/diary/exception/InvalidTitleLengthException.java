package com.threeboys.toneup.diary.exception;

import com.threeboys.toneup.common.response.exception.ErrorMessages;

public class InvalidTitleLengthException extends RuntimeException {
    public InvalidTitleLengthException() {
        super(ErrorMessages.INVALID_TITLE_LENGTH);
    }
}
