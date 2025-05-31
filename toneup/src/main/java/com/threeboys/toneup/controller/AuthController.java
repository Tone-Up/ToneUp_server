package com.threeboys.toneup.controller;

import com.threeboys.toneup.common.response.StandardResponse;
import com.threeboys.toneup.security.jwt.JWTUtil;
import com.threeboys.toneup.security.provider.ProviderType;
import com.threeboys.toneup.security.response.OAuth2LoginRequest;
import com.threeboys.toneup.security.response.OAuthLoginResponseDTO;
import com.threeboys.toneup.security.service.GoogleLoginService;
import com.threeboys.toneup.user.entity.UserEntity;
import com.threeboys.toneup.user.service.Userservice;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

import java.util.Collections;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final GoogleLoginService googleLoginService;

    public AuthController(Userservice userservice, JWTUtil jwtUtil, @Value("${google.client-id}") String clientId, GoogleLoginService googleLoginService) {
        this.googleLoginService = googleLoginService;
    }

    @PostMapping("/oauth2/authorization")
    public ResponseEntity<?> loginWithGoogle(@RequestBody OAuth2LoginRequest request) {
        OAuthLoginResponseDTO oAuthLoginResponseDTO = googleLoginService.login(request);
        return ResponseEntity.ok(new StandardResponse<>(true, 0, "Ok", oAuthLoginResponseDTO));
    }
}