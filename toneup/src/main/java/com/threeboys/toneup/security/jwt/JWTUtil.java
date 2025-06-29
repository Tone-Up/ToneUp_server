package com.threeboys.toneup.security.jwt;

import com.threeboys.toneup.common.repository.TokenRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JWTUtil {

    private SecretKey secretKey;
    private final TokenRepository tokenRepository;
    public JWTUtil(@Value("${spring.jwt.secret}")String secret, TokenRepository tokenRepository) {


        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
//        secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        this.tokenRepository = tokenRepository;
    }

    public String getNickname(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("nickname", String.class);
    }
    public Long getUserId(String token) {

        return Long.parseLong(Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("userId", String.class));
    }
    public String getPersonalColor(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("personalColor", String.class);
    }
    public String getRole(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    public Boolean isExpired(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }

    public String createJwt(Long userId, String nickname, String personalColor, String role, Long expiredMs) {

        return Jwts.builder()
                .claim("userId", userId)
                .claim("nickname", nickname)
                .claim("personalColor", personalColor)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshJwt(Long userId, Long expiredMs) {
        String refreshToken =  Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
        RefreshToken token = new RefreshToken(userId, refreshToken, expiredMs / 1000);
        tokenRepository.save(token);
        return refreshToken;
//        return Jwts.builder()
//                .claim("userId", userId)
//                .issuedAt(new Date(System.currentTimeMillis()))
//                .expiration(new Date(System.currentTimeMillis() + expiredMs))
//                .signWith(secretKey)
//                .compact();
    }
    public boolean isValidToken(String token) {
        try {
            // 토큰 파싱 및 서명 검증
            Jwts.parser()
                    .setSigningKey(secretKey) // 비밀키 설정 (SecretKey 객체)
                    .build()
                    .parseClaimsJws(token);

            // 파싱 성공 시 예외 없으므로 토큰은 유효함
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            // 서명 오류 또는 토큰 구조가 이상할 때
            System.out.println("Invalid JWT signature");
        } catch (ExpiredJwtException e) {
            // 토큰 만료됨
            System.out.println("Expired JWT token");
        } catch (UnsupportedJwtException e) {
            // 지원하지 않는 JWT일 때
            System.out.println("Unsupported JWT token");
        } catch (IllegalArgumentException e) {
            // 빈 토큰 등 잘못된 입력일 때
            System.out.println("JWT claims string is empty.");
        }
        return false; // 검증 실패 시 false 반환
    }
}
