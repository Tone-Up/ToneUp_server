package com.threeboys.toneup.controller;

import com.threeboys.toneup.common.repository.TokenRepository;
import com.threeboys.toneup.common.request.RefreshRequest;
import com.threeboys.toneup.common.response.RefreshTokenResponse;
import com.threeboys.toneup.common.response.StandardResponse;
import com.threeboys.toneup.security.CustomOAuth2User;
import com.threeboys.toneup.security.exception.InvalidRefreshTokenException;
import com.threeboys.toneup.security.jwt.JWTUtil;
import com.threeboys.toneup.security.jwt.JwtConstants;
import com.threeboys.toneup.security.jwt.RefreshToken;
import com.threeboys.toneup.security.provider.ProviderType;
import com.threeboys.toneup.security.response.OAuth2LoginRequest;
import com.threeboys.toneup.security.response.OAuthLoginResponseDTO;
import com.threeboys.toneup.security.service.OAuthLoginService;
import com.threeboys.toneup.security.service.OAuthLoginServiceFactory;
import com.threeboys.toneup.user.entity.UserEntity;
import com.threeboys.toneup.user.exception.UserNotFoundException;
import com.threeboys.toneup.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class AuthController {

    private final OAuthLoginServiceFactory loginServiceFactory;
    private final TokenRepository tokenRepository;

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


    @PostMapping("/auth/refresh")
    public ResponseEntity<?> getRefresh(@RequestBody RefreshRequest request, @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        Long userId = oAuth2User.getId();
        String nickname = oAuth2User.getNickname();
        String personalColor = oAuth2User.getPersonalColor();
        String role = oAuth2User.getRole();
        RefreshToken redisRefreshToken = tokenRepository.findById(userId).orElseThrow(()-> new UserNotFoundException(userId));
        if (!request.getRefreshToken().equals(redisRefreshToken.getRefreshToken())) {
            throw new InvalidRefreshTokenException(); // 예외 던짐
        }
        String accessToken = jwtProvider.createJwt(userId, nickname, personalColor, role, JwtConstants.ACCESS_TOKEN_EXPIRATION);
        String refreshToken = jwtProvider.createRefreshJwt(userId, JwtConstants.REFRESH_TOKEN_EXPIRATION);
        RefreshTokenResponse refreshTokenResponse = new RefreshTokenResponse(accessToken, refreshToken);

        return ResponseEntity.ok(new StandardResponse<>(true, 0, "Ok",refreshTokenResponse));
    }


}