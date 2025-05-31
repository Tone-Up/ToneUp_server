package com.threeboys.toneup.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.threeboys.toneup.common.response.StandardResponse;
import com.threeboys.toneup.security.CustomOAuth2User;
import com.threeboys.toneup.security.jwt.JWTUtil;
import com.threeboys.toneup.security.response.OAuthLoginResponseDTO;
import com.threeboys.toneup.user.entity.UserEntity;
import com.threeboys.toneup.user.repository.UserRepository;
import com.threeboys.toneup.user.service.Userservice;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@Component
public class Oauth2SuccessHandler implements AuthenticationSuccessHandler {
    private final JWTUtil jwtUtil;
    private final Userservice userservice;
    private final UserRepository userRepository;

    public Oauth2SuccessHandler(JWTUtil jwtUtil, Userservice userservice, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userservice = userservice;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User customUserDetails  = (CustomOAuth2User) authentication.getPrincipal();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();
        Long userId = customUserDetails.getId();
        String nickname = customUserDetails.getNickname();
        String personalColor = customUserDetails.getPersonalColor();
        String privider = customUserDetails.getProvider();

        String accessToken = jwtUtil.createJwt(userId,nickname, personalColor, role, 60*60*60L);
        String refreshToken = jwtUtil.createRefreshJwt(userId, 60*60*24*14L);
        UserEntity socialUser = userRepository.findById(userId).orElseThrow();
        OAuthLoginResponseDTO data = OAuthLoginResponseDTO.builder()
                .provider(privider)
                .nickname(nickname)
                .userId(userId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .isSignedUp(userservice.isSignedUp(socialUser))
                .isPersonal(userservice.isPersonal(socialUser))
                .build();

        StandardResponse<OAuthLoginResponseDTO> responseBody = new StandardResponse<>(true, 0, "Ok", data);

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(response.getWriter(), responseBody);

    }

}
