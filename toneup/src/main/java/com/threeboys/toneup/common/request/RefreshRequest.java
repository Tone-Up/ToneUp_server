package com.threeboys.toneup.common.request;

import lombok.Getter;

@Getter
public class RefreshRequest {
    String accessToken;
    String refreshToken;
}
