package com.threeboys.toneup.personalColor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.Executors;

@Configuration
public class RestClientConfig {

    @Value("${external.fastapi.url}")
    private String baseUrl;

    @Value("${spring.threads.virtual.enabled}")
    private boolean isVirtualThreadEnabled;

//    @Bean
//    public RestClient restClient() {
//        var builder = RestClient.builder()
//                .baseUrl(baseUrl);
//
//        if (isVirtualThreadEnabled) {
//            builder = builder.requestFactory(new JdkClientHttpRequestFactory(
//                    HttpClient.newBuilder()
//                            .executor(Executors.newVirtualThreadPerTaskExecutor())
//                            .build()
//            ));
//        }
//
//        return builder.build();
//    }
    @Bean
    public RestClient restClient(HttpClient httpClient) {
        return RestClient.builder()
                .requestFactory(new HttpComponentsClientHttpRequestFactory())
                .requestInterceptor((request, body, execution) -> {
                    System.out.println("Headers: " + request.getHeaders());
                    return execution.execute(request, body);
                })
                .build();
    }
    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder()
//                .executor(Executors.newVirtualThreadPerTaskExecutor()) // Thread pinning 발생
                .connectTimeout(Duration.ofSeconds(15))
//                .version(HttpClient.Version.HTTP_2)
                .build();
    }
    // Thread pinning 발생
//    @Bean
//    public RestClient restClient() {
//        return RestClient.builder()
//                .baseUrl(baseUrl)
//                .build();
//    }
}