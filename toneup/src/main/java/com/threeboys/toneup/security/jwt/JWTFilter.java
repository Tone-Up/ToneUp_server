package com.threeboys.toneup.security.jwt;

import com.threeboys.toneup.security.CustomOAuth2User;
import com.threeboys.toneup.user.dto.UserDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //Header에서 토큰 꺼내기
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7); // "Bearer " 제거

            try {
                //토큰 유효성 검사, 사용자 정보 추출
                if (jwtUtil.isValidToken(token)) {
                    Long userId = jwtUtil.getUserId(token);
                    String role = jwtUtil.getRole(token);
                    String nickname = jwtUtil.getNickname(token);

                    //userDTO를 생성하여 값 set
                    UserDTO userDTO = new UserDTO();
                    userDTO.setId(userId);
                    userDTO.setRole(role);

                    //UserDetails에 회원 정보 객체 담기(일반 로그인은UserDetails, 소셜 로그인은 OAuth2User)
                    CustomOAuth2User customOAuth2User = new CustomOAuth2User(userDTO);

                    //스프링 시큐리티 인증 토큰 생성
                    Authentication authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());
                    //세션에 사용자 등록
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }

            } catch (Exception e) {
                // 유효하지 않은 토큰일 경우 SecurityContext를 비우고 진행
                SecurityContextHolder.clearContext();
            }
        }

        // 다음 필터로 넘김
        filterChain.doFilter(request, response);
    }
}
