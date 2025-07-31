package com.threeboys.toneup.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.threeboys.toneup.common.response.ErrorResponse;
import com.threeboys.toneup.common.response.exception.ErrorMessages;
import com.threeboys.toneup.security.CustomOAuth2User;
import com.threeboys.toneup.user.dto.UserDTO;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (path.equals("/api/auth/refresh")) {
            // 리프레시 토큰 요청은 토큰 만료 검사를 건너뜀
            filterChain.doFilter(request, response);
            return;
        }
        //Header에서 토큰 꺼내기
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7); // "Bearer " 제거

            try {
                //토큰 유효성 검사, 사용자 정보 추출
                jwtUtil.validateToken(token);

                Long userId = jwtUtil.getUserId(token);
                String role = jwtUtil.getRole(token);
                String nickname = jwtUtil.getNickname(token);
                String personalColor = jwtUtil.getPersonalColor(token);
                //userDTO를 생성하여 값 set
                UserDTO userDTO = new UserDTO();
                userDTO.setId(userId);
                userDTO.setRole(role);
                userDTO.setPersonalColor(personalColor);
                userDTO.setNickname(nickname);

                //UserDetails에 회원 정보 객체 담기(일반 로그인은UserDetails, 소셜 로그인은 OAuth2User)
                CustomOAuth2User customOAuth2User = new CustomOAuth2User(userDTO);

                //스프링 시큐리티 인증 토큰 생성
                Authentication authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());
                //세션에 사용자 등록
                SecurityContextHolder.getContext().setAuthentication(authToken);


            } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
                SecurityContextHolder.clearContext();
                // 서명 오류 또는 토큰 구조가 이상할 때
                log.error("Invalid JWT signature");
                throw e;
            } catch (ExpiredJwtException e) {
                SecurityContextHolder.clearContext();
                log.error("Expired JWT token");

                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setContentType("application/json;charset=UTF-8");
                httpResponse.getWriter().write(
                        new ObjectMapper().writeValueAsString(
                                new ErrorResponse<>(
                                        401, "TOKEN_EXPIRED", ErrorMessages.TOKEN_EXPIRED)
                                )
                );
                return;
            } catch (UnsupportedJwtException e) {
                SecurityContextHolder.clearContext();
                // 지원하지 않는 JWT일 때
                log.error("Unsupported JWT token");
                throw e;
            } catch (IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
                // 빈 토큰 등 잘못된 입력일 때
                log.error("JWT claims string is empty.");
                throw e;
            }
        }

        // 다음 필터로 넘김
        filterChain.doFilter(request, response);
    }
}
