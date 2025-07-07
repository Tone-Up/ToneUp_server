package com.threeboys.toneup.common.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse<T> {
    private boolean success;
    private int code;
    private String errorCode;
    private String message;

    public ErrorResponse(int code, String errorCode, String message) {
        this.success = false;
        this.code = code;
        this.errorCode = errorCode;
        this.message = message;
    }
}
