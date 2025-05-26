package com.threeboys.toneup.common.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StandardResponse<T> {
    private boolean success;
    private int code;
    private String message;
    private T data;

    public StandardResponse(boolean success, int code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
