package com.threeboys.toneup.common.config;

import com.redislabs.lettusearch.RediSearchClient;
import com.redislabs.lettusearch.StatefulRediSearchConnection;
import io.lettuce.core.RedisURI;
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

    @Value(("${spring.ai.openai.api-key}"))
    private String openApiKey;

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

    @Bean
    public JedisPooled jedisPooled() {
        DefaultJedisClientConfig config = DefaultJedisClientConfig.builder()
                .password(redisPassword)   // Redis 비밀번호
                .build();
        return new JedisPooled(new HostAndPort(masterHost, masterPort), config);
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


//    @Bean
//    public EmbeddingModel embeddingModel() {
//
//        return new OpenAiEmbeddingModel(new OpenAiApi(openApiKey));
//    }


}
