package com.threeboys.toneup.security.jwt;

public class JwtConstants {
    public static final long ACCESS_TOKEN_EXPIRATION = 60 * 60 * 60L; // 60시간
    public static final long REFRESH_TOKEN_EXPIRATION = 60 * 60 * 24 * 30L; // 30일
}
