package com.threeboys.toneup.controller;

import com.threeboys.toneup.common.response.StandardResponse;
import com.threeboys.toneup.security.jwt.JWTUtil;
import com.threeboys.toneup.security.provider.ProviderType;
import com.threeboys.toneup.security.response.OAuth2LoginRequest;
import com.threeboys.toneup.security.response.OAuthLoginResponseDTO;
import com.threeboys.toneup.security.service.OAuthLoginService;
import com.threeboys.toneup.security.service.OAuthLoginServiceFactory;
import com.threeboys.toneup.user.entity.UserEntity;
import com.threeboys.toneup.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class AuthController {

    private final OAuthLoginServiceFactory loginServiceFactory;


    @PostMapping("/app/authorization")
    public ResponseEntity<?> loginWithGoogle(@RequestBody OAuth2LoginRequest request) {
        ProviderType type = ProviderType.valueOf(request.getProvider().toUpperCase()); // 딱 여기까지만 책임
        OAuthLoginService service = loginServiceFactory.getService(type);
        OAuthLoginResponseDTO oAuthLoginResponseDTO = service.login(request);
        return ResponseEntity.ok(new StandardResponse<>(true, 0, "Ok", oAuthLoginResponseDTO));

    }
    // Dev 전용 엔드포인트 (실서비스 배포 시 삭제)
    private final UserRepository userRepository;
    private final JWTUtil jwtProvider;

    @PostMapping("/app/dev/auth-token")
    public String getTestToken() {
        UserEntity testUser = userRepository.findByEmail("kimsy980311@gmail.com")
                .orElseThrow();
        return jwtProvider.createJwt(testUser.getId(),testUser.getNickname(),"null",testUser.getRole(), 60*60*24*14L);
    }
}