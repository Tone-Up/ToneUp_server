package com.threeboys.toneup.common.config;

import jakarta.annotation.PostConstruct;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.ReadMode;
import org.redisson.config.SubscriptionMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class RedissonConfig {

    public static final String REDISSON_HOST_PREFIX = "redis://";

//    @Value("${spring.data.redis.host}")
//    private String redisHost;

//    @Value("${spring.data.redis.port}")
//    private int redisPort;

//    @Value("${spring.data.redis.master.host}")
//    private String masterHost;
//    @Value("${spring.data.redis.master.port}")
//    private int masterPort;

    @Value("${spring.data.redis.sentinel.password}")
    private String redisPassword;

    @Value("${spring.data.redis.sentinel.master}")
    private String masterName;

    private final RedisSentinelProperties redisSentinelProperties;

    public RedissonConfig(RedisSentinelProperties redisSentinelProperties) {
        this.redisSentinelProperties = redisSentinelProperties;
    }

//    @Bean(destroyMethod = "shutdown")
//    public RedissonClient redissonClient() {
//        Config config = new Config();
//        config.useMasterSlaveServers()
//                .setMasterAddress(REDISSON_HOST_PREFIX + masterHost +":"+masterPort) // 마스터
//                .addSlaveAddress(REDISSON_HOST_PREFIX + redisHost +":"+redisPort) // 슬레이브
//                .setReadMode(ReadMode.MASTER_SLAVE) //추후에 리드 모드 찾아보기
////                .setSubscriptionMode(SubscriptionMode.SLAVE) // pub/sub 사용할 때
//                .setSlaveConnectionMinimumIdleSize(0)// 슬레이브 없어도 동작하도록 설정
//                .setSlaveConnectionPoolSize(6)
//                .setPassword(redisPassword); // 필요 시
//
////        config.useSingleServer()
////                .setAddress(REDISSON_HOST_PREFIX + redisHost + ":" + redisPort);
//
//        return Redisson.create(config);
//    }

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        List<String> sentinelNodes = redisSentinelProperties.getNodes();
        // Sentinel 모드 사용
        System.out.println(sentinelNodes.getFirst());
        config.useSentinelServers()
                .setMasterName(masterName)   // sentinel.conf에서 설정한 마스터 이름
                .addSentinelAddress(
                        REDISSON_HOST_PREFIX + sentinelNodes.get(0),
                        REDISSON_HOST_PREFIX +sentinelNodes.get(1),
                        REDISSON_HOST_PREFIX +sentinelNodes.get(2)
                )
                .setPassword(redisPassword)
                .setReadMode(ReadMode.MASTER_SLAVE)         // 슬레이브 우선 읽기
                .setSlaveConnectionMinimumIdleSize(0)
                .setSlaveConnectionPoolSize(6)
                .setCheckSentinelsList(true); // 로컬에서만 false

        return Redisson.create(config);
    }

}
