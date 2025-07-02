package com.threeboys.toneup.security.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.threeboys.toneup.common.response.exception.ErrorMessages;
import com.threeboys.toneup.common.response.exception.InvalidTokenException;
import com.threeboys.toneup.security.jwt.JWTUtil;
import com.threeboys.toneup.security.jwt.JwtConstants;
import com.threeboys.toneup.security.provider.ProviderType;
import com.threeboys.toneup.security.response.OAuth2LoginRequest;
import com.threeboys.toneup.security.response.OAuthLoginResponseDTO;
import com.threeboys.toneup.user.entity.UserEntity;
import com.threeboys.toneup.user.repository.UserRepository;
import com.threeboys.toneup.user.service.Userservice;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;
@Service
public class GoogleLoginService implements OAuthLoginService{
    private final Userservice userservice;
    private final JWTUtil jwtUtil;
    private final GoogleIdTokenVerifier verifier;

    public GoogleLoginService(@Value("${google.client-id}") String clientId, UserRepository userRepository, Userservice userservice, JWTUtil jwtUtil,
                              NetHttpTransport transport, JsonFactory jsonFactory) {
        this.userservice = userservice;
//        this.clientId = clientId;
        this.jwtUtil = jwtUtil;
        // Google ID 토큰 검증기 생성
        this.verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(clientId))
                .build();
    }


    @Override
    public OAuthLoginResponseDTO login(OAuth2LoginRequest request) {


        try {
            // 2. ID 토큰 검증
            GoogleIdToken idToken = Optional.ofNullable(verifier.verify(request.getToken()))
                    .orElseThrow(() -> new InvalidTokenException(ErrorMessages.INVALID_SOCIAL_TOKEN));

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            ProviderType providerType = ProviderType.valueOf(request.getProvider().toUpperCase());
            String providerId =  payload.getSubject();
            String nickname = name+"_"+providerId;

            // 3. (예시) 회원 가입 또는 로그인 처리 (생략 가능)
            UserEntity socialUser = userservice.registerUser(name,nickname, email, providerType, providerId);

            String accessToken = jwtUtil.createJwt(socialUser.getId(), socialUser.getNickname(), socialUser.getPersonalColor().getPersonalColorType().toString(), socialUser.getRole(), JwtConstants.ACCESS_TOKEN_EXPIRATION);
            String refreshToken = jwtUtil.createRefreshJwt(socialUser.getId(), JwtConstants.REFRESH_TOKEN_EXPIRATION);

            OAuthLoginResponseDTO dto =OAuthLoginResponseDTO.builder()
                    .provider(providerType.name())
                    .nickname(nickname)
                    .userId(socialUser.getId())
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .isSignedUp(userservice.isSignedUp(socialUser))
                    .isPersonal(userservice.isPersonal(socialUser))
                    .build();
            return dto;
        } catch (GeneralSecurityException | IOException e) {
            throw new InvalidTokenException("INVALID_SOCIAL_TOKEN");
        }
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.GOOGLE;
    }
}
