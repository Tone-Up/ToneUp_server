package com.threeboys.toneup.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.threeboys.toneup.common.repository.TokenRepository;
import com.threeboys.toneup.personalColor.domain.PersonalColor;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // 시큐리티 필터 무시 (순수 컨트롤러 로직만 테스트)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OAuthLoginServiceFactory loginServiceFactory;

    @MockitoBean
    private TokenRepository tokenRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JWTUtil jwtProvider;

    @Test
    @DisplayName("소셜 로그인 API 성공 테스트")
    void loginWithGoogleTest() throws Exception {
        // given
        String requestJson = "{\"provider\":\"google\", \"token\":\"test_oauth_token\", \"name\":\"testName\", \"email\":\"test@email.com\"}";

        OAuthLoginService mockService = mock(OAuthLoginService.class);
        OAuthLoginResponseDTO responseDTO = OAuthLoginResponseDTO.builder()
                .provider("google")
                .isPersonal(true)
                .isSignedUp(true)
                .nickname("testName")
                .userId(1L)
                .accessToken("test_access_token")
                .refreshToken("test_refresh_token")
                .build();

        given(loginServiceFactory.getService(ProviderType.GOOGLE)).willReturn(mockService);
        given(mockService.login(any(OAuth2LoginRequest.class))).willReturn(responseDTO);

        // when & then
        mockMvc.perform(post("/api/app/authorization")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("test_access_token"))
                .andExpect(jsonPath("$.data.refreshToken").value("test_refresh_token"))
                .andExpect(jsonPath("$.data.userId").value(1L));
    }

    @Test
    @DisplayName("토큰 리프레시 API 성공 테스트")
    void getRefreshSuccessTest() throws Exception {
        // given
        String requestRefreshToken = "valid_refresh_token";
        String newAccessToken = "new_access_token";
        String newRefreshToken = "new_refresh_token";
        Long userId = 1L;

        String requestJson = "{\"refreshToken\":\"" + requestRefreshToken + "\"}";

        UserEntity mockUser = mock(UserEntity.class);
        given(mockUser.getNickname()).willReturn("testNickname");
        given(mockUser.getRole()).willReturn("ROLE_USER");

        PersonalColor mockColor = mock(PersonalColor.class);
        given(mockColor.toString()).willReturn("SPRING_WARM");
        given(mockUser.getPersonalColor()).willReturn(mockColor);

        // DB (Redis)에 저장되어 있는 리프레시 토큰이라고 가정
        RefreshToken dbRefreshToken = new RefreshToken(userId, requestRefreshToken,
                JwtConstants.REFRESH_TOKEN_EXPIRATION);

        given(jwtProvider.getRefreshUserId(requestRefreshToken)).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(tokenRepository.findById(userId)).willReturn(Optional.of(dbRefreshToken));

        given(jwtProvider.createJwt(eq(userId), eq("testNickname"), eq("SPRING_WARM"), eq("ROLE_USER"),
                eq(JwtConstants.ACCESS_TOKEN_EXPIRATION)))
                .willReturn(newAccessToken);
        given(jwtProvider.createRefreshJwt(eq(userId), eq(JwtConstants.REFRESH_TOKEN_EXPIRATION)))
                .willReturn(newRefreshToken);

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value(newAccessToken))
                .andExpect(jsonPath("$.data.refreshToken").value(newRefreshToken));
    }

    @Test
    @DisplayName("토큰 리프레시 API 실패 테스트 - DB에 저장된 토큰과 요청한 토큰이 일치하지 않는 경우")
    void getRefreshFailDueToMismatchTest() throws Exception {
        // given
        String requestRefreshToken = "invalid_refresh_token";
        Long userId = 1L;

        String requestJson = "{\"refreshToken\":\"" + requestRefreshToken + "\"}";

        UserEntity mockUser = mock(UserEntity.class);
        given(mockUser.getNickname()).willReturn("testNickname");
        given(mockUser.getRole()).willReturn("ROLE_USER");

        PersonalColor mockColor = mock(PersonalColor.class);
        given(mockColor.toString()).willReturn("SPRING_WARM");
        given(mockUser.getPersonalColor()).willReturn(mockColor);

        // 요청한 token과 다른 valid한 token이 DB에 있다고 설정
        RefreshToken dbRefreshToken = new RefreshToken(userId, "different_valid_refresh_token",
                JwtConstants.REFRESH_TOKEN_EXPIRATION);

        given(jwtProvider.getRefreshUserId(requestRefreshToken)).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(tokenRepository.findById(userId)).willReturn(Optional.of(dbRefreshToken));

        // when & then
        // InvalidRefreshTokenException이 GlobalExceptionHandler에서 처리되어 401 응답을 반환함
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("토큰 리프레시 API 실패 테스트 - 유저를 찾을 수 없는 경우")
    void getRefreshFailDueToUserNotFoundTest() throws Exception {
        // given
        String requestRefreshToken = "valid_refresh_token";
        Long userId = 99L;

        String requestJson = "{\"refreshToken\":\"" + requestRefreshToken + "\"}";

        given(jwtProvider.getRefreshUserId(requestRefreshToken)).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.empty()); // DB에 유저가 없음

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
            mockMvc.perform(post("/api/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson));
        });
    }
}
