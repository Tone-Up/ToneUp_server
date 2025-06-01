package com.threeboys.toneup.controller;

import com.threeboys.toneup.common.response.StandardResponse;
import com.threeboys.toneup.security.provider.ProviderType;
import com.threeboys.toneup.security.response.OAuth2LoginRequest;
import com.threeboys.toneup.security.response.OAuthLoginResponseDTO;
import com.threeboys.toneup.security.service.OAuthLoginService;
import com.threeboys.toneup.security.service.OAuthLoginServiceFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final OAuthLoginServiceFactory loginServiceFactory;

    public AuthController(OAuthLoginServiceFactory loginServiceFactory) {
        this.loginServiceFactory = loginServiceFactory;
    }


    @PostMapping("/app/authorization")
    public ResponseEntity<?> loginWithGoogle(@RequestBody OAuth2LoginRequest request) {
        ProviderType type = ProviderType.valueOf(request.getProvider().toUpperCase()); // 딱 여기까지만 책임
        OAuthLoginService service = loginServiceFactory.getService(type);
        OAuthLoginResponseDTO oAuthLoginResponseDTO = service.login(request);
        return ResponseEntity.ok(new StandardResponse<>(true, 0, "Ok", oAuthLoginResponseDTO));

    }
}