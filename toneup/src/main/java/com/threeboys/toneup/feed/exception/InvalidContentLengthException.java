package com.threeboys.toneup.feed.exception;

import com.threeboys.toneup.common.response.exception.ErrorMessages;
import org.h2.api.ErrorCode;

public class InvalidContentLengthException extends RuntimeException{
    public InvalidContentLengthException() {
        super(ErrorMessages.INVALID_CONTENT_LENGTH);
    }
}
