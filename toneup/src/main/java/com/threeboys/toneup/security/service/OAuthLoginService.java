package com.threeboys.toneup.security.service;

import com.threeboys.toneup.security.provider.ProviderType;
import com.threeboys.toneup.security.response.OAuth2LoginRequest;
import com.threeboys.toneup.security.response.OAuthLoginResponseDTO;

public interface OAuthLoginService {
    OAuthLoginResponseDTO login(OAuth2LoginRequest request);
    ProviderType getProviderType();

}
