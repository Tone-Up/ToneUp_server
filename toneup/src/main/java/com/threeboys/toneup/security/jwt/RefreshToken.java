package com.threeboys.toneup.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

//@NoArgsConstructor
@Getter
@AllArgsConstructor
@RedisHash("token")
public class RefreshToken {
    @Id
    private Long id;
    private String refreshToken;
    @TimeToLive
    private Long expiration;
}
