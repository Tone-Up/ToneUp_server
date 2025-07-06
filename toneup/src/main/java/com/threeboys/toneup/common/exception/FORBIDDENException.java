package com.threeboys.toneup.common.exception;

import com.threeboys.toneup.common.response.exception.ErrorMessages;

public class FORBIDDENException extends RuntimeException {
    public FORBIDDENException() {
        super(ErrorMessages.FORBIDDEN);
    }
}
