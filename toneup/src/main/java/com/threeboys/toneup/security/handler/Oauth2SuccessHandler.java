package com.threeboys.toneup.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.threeboys.toneup.common.response.StandardResponse;
import com.threeboys.toneup.security.CustomOAuth2User;
import com.threeboys.toneup.security.jwt.JWTUtil;
import com.threeboys.toneup.security.response.OAuthLoginResponseDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
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

    public Oauth2SuccessHandler(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
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

        OAuthLoginResponseDTO data = new OAuthLoginResponseDTO();
        data.setProvider(privider);
        data.setPersonal(false); // 필요에 따라 변경
        data.setSignedUp(true);  // 필요에 따라 변경
        data.setNickname(nickname);
        data.setUserId(userId);
        data.setAccessToken(accessToken);
        data.setRefreshToken(refreshToken);

        StandardResponse<OAuthLoginResponseDTO> responseBody = new StandardResponse<>(true, 0, "Ok", data);

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(response.getWriter(), responseBody);

    }

}
