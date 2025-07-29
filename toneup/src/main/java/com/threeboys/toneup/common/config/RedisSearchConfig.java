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

    @Value("${spring.data.redis.master.host}")
    private String masterHost;
    @Value("${spring.data.redis.master.port}")
    private int masterPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Bean
    public RediSearchClient rediSearchClient() {
        RedisURI redisUri = RedisURI.Builder
                .redis(masterHost, masterPort)
                .withPassword(redisPassword.toCharArray())
                .build();

        return RediSearchClient.create(redisUri);
    }

    @Bean
    public StatefulRediSearchConnection<String, String> searchConnection(RediSearchClient rediSearchClient) {
        return rediSearchClient.connect();
    }
}
