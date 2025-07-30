package com.threeboys.toneup.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.ReadMode;
import org.redisson.config.SubscriptionMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

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


    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useMasterSlaveServers()
                .setMasterAddress(REDISSON_HOST_PREFIX + masterHost +":"+masterPort) // 마스터
                .addSlaveAddress(REDISSON_HOST_PREFIX + redisHost +":"+redisPort) // 슬레이브
                .setReadMode(ReadMode.MASTER_SLAVE) //추후에 리드 모드 찾아보기
//                .setSubscriptionMode(SubscriptionMode.SLAVE) // pub/sub 사용할 때
                .setSlaveConnectionMinimumIdleSize(0)// 슬레이브 없어도 동작하도록 설정
                .setSlaveConnectionPoolSize(6)
                .setPassword(redisPassword); // 필요 시

//        config.useSingleServer()
//                .setAddress(REDISSON_HOST_PREFIX + redisHost + ":" + redisPort);

        return Redisson.create(config);
    }
}
