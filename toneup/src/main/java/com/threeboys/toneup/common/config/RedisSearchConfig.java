package com.threeboys.toneup.common.config;

import com.redislabs.lettusearch.RediSearchClient;
import com.redislabs.lettusearch.StatefulRediSearchConnection;
import io.lettuce.core.RedisURI;
import jakarta.annotation.PostConstruct;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import redis.clients.jedis.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
public class RedisSearchConfig {

    public static final String REDISSON_HOST_PREFIX = "redis://";

    @Value("${spring.ai.vectorstore.redis.host}")
    private String vectorRedisHost;
    @Value("${spring.ai.vectorstore.redis.port}")
    private int vectorRedisPort;

    @Value("${spring.data.redis.sentinel.password}")
    private String redisPassword;

    private final RedisSentinelProperties redisSentinelProperties;


    @Value("${spring.data.redis.sentinel.master}")
    private String masterName;

    @Value(("${spring.ai.openai.api-key}"))
    private String openApiKey;

    public RedisSearchConfig(RedisSentinelProperties redisSentinelProperties) {
        this.redisSentinelProperties = redisSentinelProperties;
    }

    @Bean
    public JedisPooled jedisPooled() {
        return new JedisPooled(vectorRedisHost, vectorRedisPort);
    }

    @Bean
    public RediSearchClient rediSearchClient() {
        List<String> nodes = redisSentinelProperties.getNodes();
        if (nodes.isEmpty()) {
            throw new IllegalArgumentException("Sentinel nodes : empty!!!");
        }

        // Sentinel 연결: 첫 번째 노드 기준, 마스터 이름 지정
        String[] firstNode = nodes.get(0).split(":");
        RedisURI.Builder builder = RedisURI.Builder.sentinel(
                firstNode[0],
                Integer.parseInt(firstNode[1]),
                masterName
        );
        // 모든 센티널 노드(EC2, 집1, 집2)를 추가
        for (int i =1; i < redisSentinelProperties.getNodes().size();i++) {
            String[] hostPort = nodes.get(i).split(":");
            builder.withSentinel(hostPort[0], Integer.parseInt(hostPort[1]));
        }

        RedisURI redisUri = builder
                .withPassword(redisPassword.toCharArray())
                .build();
        return RediSearchClient.create(redisUri);
    }

    @Bean
    public StatefulRediSearchConnection<String, String> searchConnection(RediSearchClient rediSearchClient) {
        return rediSearchClient.connect();
    }

    @Bean
    public VectorStore openClipVectorStore(JedisPooled jedisPooled, @Qualifier("openClipEmbeddingModel") EmbeddingModel embeddingModel) {
        return RedisVectorStore.builder(jedisPooled, embeddingModel) // openClip 더미 모델
                .indexName("spring-ai-index-openclip")
                .prefix("productEmbedding")
                .initializeSchema(true)
                .build();
    }

    @Bean
    public VectorStore openAiVectorStore(JedisPooled jedisPooled,  @Qualifier("openAiEmbeddingModel") EmbeddingModel embeddingModel) {

        return  RedisVectorStore.builder(jedisPooled, embeddingModel)
                .initializeSchema(true) // ← 여기 꼭 true
                .build();
    }

}
