package com.threeboys.toneup.personalColor.config;

import com.threeboys.toneup.personalColor.infra.FastApiClient;
import com.threeboys.toneup.personalColor.infra.FastApiClientImpl;


import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.redisson.api.RedissonClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
//import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;


@Configuration
@EnableConfigurationProperties(FastApiProperties.class)
public class FastApiConfig {
//    @Bean
//    public RestTemplate restTemplate() {
//        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
////        connManager.setMaxTotal(12); // 최대 커넥션 수
////        connManager.setDefaultMaxPerRoute(20); // 라우트당 최대 커넥션 수
//
//        HttpClient httpClient = HttpClients.custom()
//                .setConnectionManager(connManager)
////                .evictIdleConnections(30, TimeUnit.SECONDS) // idle 커넥션 정리
//                .build();
//
////        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
//        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
////        factory.setConnectTimeout(30000);
////        factory.setReadTimeout(30000);
//
//        return new RestTemplate(factory);
////        return new RestTemplate();
//
//    }
    @Bean
    public RestTemplate restTemplate() {
        // 1. 커넥션 매니저 설정 (부하 테스트를 위해 풀 넉넉히)
        PoolingHttpClientConnectionManager connManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(100)      // 전체 최대 커넥션
                .setMaxConnPerRoute(50)    // 목적지(FastAPI)당 최대 커넥션
                .build();

        // 2. HttpClient 5.4.x 빌드 (Classic 버전은 기본이 HTTP/1.1)
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connManager)
//                .evictIdleConnections(TimeValue.ofSeconds(30)) // 유휴 커넥션 정리
                .build();

        // 3. RequestFactory 설정
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);


        return new RestTemplate(factory);
    }





//    HttpClient httpClient = HttpClient.create(ConnectionProvider.builder("custom")
//            .maxConnections(10)          // 동시에 유지할 최대 커넥션 수
//            .pendingAcquireMaxCount(500)  // 커넥션 풀에 여유가 없을 때 대기할 요청 수
//            .pendingAcquireTimeout(Duration.ofSeconds(30))
//            .build());

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8000") // 실제 주소로 수정
//                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
    @Bean
    public FastApiClient fastApiClient(RestTemplate restTemplate, FastApiProperties props, WebClient webClient, RestClient restClient, RedissonClient redissonClient) {
        return new FastApiClientImpl(restTemplate, props.getUrl(),redissonClient, restClient,webClient );
    }
}
