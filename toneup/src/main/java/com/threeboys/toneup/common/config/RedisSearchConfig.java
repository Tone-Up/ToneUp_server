package com.threeboys.toneup.common.config;

import com.redislabs.lettusearch.RediSearchClient;
import com.redislabs.lettusearch.StatefulRediSearchConnection;
import io.lettuce.core.RedisURI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class RedisSearchConfig {

    public static final String REDISSON_HOST_PREFIX = "redis://";

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Bean
    public RediSearchClient rediSearchClient() {
        RedisURI redisUri = RedisURI.Builder
                .redis(redisHost, redisPort) // 👉 실제 redis 주소로 변경
                .build();

        return RediSearchClient.create(redisUri);
    }

    @Bean
    public StatefulRediSearchConnection<String, String> searchConnection(RediSearchClient rediSearchClient) {
        return rediSearchClient.connect();
    }
}
