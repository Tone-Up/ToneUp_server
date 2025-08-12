package com.threeboys.toneup.security.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuth2LoginRequest {
    private String provider;
    private String token;
    private String fcmToken;
}