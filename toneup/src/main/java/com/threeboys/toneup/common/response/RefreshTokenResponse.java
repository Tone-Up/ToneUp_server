package com.threeboys.toneup.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RefreshTokenResponse {
    String accessToken;
    String refreshToken;

}
