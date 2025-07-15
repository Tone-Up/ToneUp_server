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

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("userId", Long.class);
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
                .expiration(new Date(System.currentTimeMillis() + expiredMs*1000))
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshJwt(Long userId, Long expiredMs) {
        String refreshToken =  Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiredMs*1000))
                .signWith(secretKey)
                .compact();
        RefreshToken token = new RefreshToken(userId, refreshToken, expiredMs);
        tokenRepository.save(token);
        return refreshToken;
//        return Jwts.builder()
//                .claim("userId", userId)
//                .issuedAt(new Date(System.currentTimeMillis()))
//                .expiration(new Date(System.currentTimeMillis() + expiredMs))
//                .signWith(secretKey)
//                .compact();
    }
    public void validateToken(String token) throws ExpiredJwtException{
            // 토큰 파싱 및 서명 검증
            Jwts.parser()
                    .verifyWith(secretKey) // 비밀키 설정 (SecretKey 객체)
                    .build()
                    .parseClaimsJws(token);
    }
}
