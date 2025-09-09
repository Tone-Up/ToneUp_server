package com.threeboys.toneup.common.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Data
@ConfigurationProperties(prefix = "spring.data.redis.sentinel")
public class RedisSentinelProperties {
    private String master;
    private final List<String> nodes = new ArrayList<>();
    private String password;

    // getter, setter
}

