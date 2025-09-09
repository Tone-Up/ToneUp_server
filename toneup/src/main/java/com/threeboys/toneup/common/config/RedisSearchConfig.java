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

//    @Value("${spring.data.redis.host}")
//    private String redisHost;
//
//    @Value("${spring.data.redis.port}")
//    private int redisPort;
//
//    @Value("${spring.data.redis.master.host}")
//    private String masterHost;
//    @Value("${spring.data.redis.master.port}")
//    private int masterPort;

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

    //
//    @Bean
//    public RediSearchClient rediSearchClient() {
//        RedisURI redisUri = RedisURI.Builder
//                .redis(masterHost, masterPort)
//                .withPassword(redisPassword.toCharArray())
//                .build();
//
//        return RediSearchClient.create(redisUri);
//    }
//
//    @Bean
//    public StatefulRediSearchConnection<String, String> searchConnection(RediSearchClient rediSearchClient) {
//        return rediSearchClient.connect();
//    }
//

//    @Bean
//    public JedisPooled jedisPooled() {
//        DefaultJedisClientConfig config = DefaultJedisClientConfig.builder()
//                .password(redisPassword)   // Redis 비밀번호
//                .build();
//        String[] firstNode = redisSentinelProperties.getNodes().getFirst().split(":");
//
//        return new JedisPooled(new HostAndPort(firstNode[0], Integer.parseInt(firstNode[1])), config);
//    }
    @Bean
    public JedisPooled jedisPooled() {
        Set<String> sentinels = new HashSet<>(redisSentinelProperties.getNodes()); // "host:port" 형태
        String masterName = redisSentinelProperties.getMaster();

        // JedisSentinelPool용 클라이언트 설정
//        DefaultJedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
//                .password(redisPassword)   // Redis 비밀번호
//                .build();

        // JedisSentinelPool 생성 (5.x 버전)
        JedisSentinelPool pool = new JedisSentinelPool(masterName, sentinels, redisPassword);
        JedisClientConfig config = DefaultJedisClientConfig.builder()
                .password(redisPassword) // Redis 비밀번호
                .build();
        // JedisPooled로 래핑
//        HostAndPort hostAndPort = new HostAndPort("localhost",6379);
        return new JedisPooled(pool.getCurrentHostMaster(), config);
    }

    @Bean
    public RediSearchClient rediSearchClient() {
        // Sentinel 연결: 첫 번째 노드 기준, 마스터 이름 지정
        String[] firstNode = redisSentinelProperties.getNodes().getFirst().split(":");
        RedisURI redisUri = RedisURI.Builder
                .sentinel(firstNode[0], Integer.parseInt(firstNode[1]), masterName)
                .withPassword(redisPassword.toCharArray())
                .build();

        return RediSearchClient.create(redisUri);
    }

    @Bean
    public StatefulRediSearchConnection<String, String> searchConnection(RediSearchClient rediSearchClient) {
        return rediSearchClient.connect();
    }

//    @Bean
//    public JedisSentinelPool jedisPooled() {
//        // "ip:port" 형식 그대로
//        Set<String> sentinels = new HashSet<>(sentinelNodes);
//
//        JedisSentinelPool sentinelPool = new JedisSentinelPool(
//                masterName,
//                sentinels,
//                redisPassword
//        );
//
//        return new JedisPooled(sentinelPool);
//    }





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
