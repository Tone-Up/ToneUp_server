package com.threeboys.toneup.security.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuthLoginResponseDTO {
    private String provider;
    private boolean isPersonal;
    private boolean isSignedUp;
    private String nickname;
    private Long userId;
    private String accessToken;
    private String refreshToken;

}