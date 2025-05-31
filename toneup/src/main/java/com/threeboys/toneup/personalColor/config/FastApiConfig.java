package com.threeboys.toneup.personalColor.config;

import com.threeboys.toneup.personalColor.infra.FastApiClient;
import com.threeboys.toneup.personalColor.infra.FastApiClientImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;


@Configuration
@EnableConfigurationProperties(FastApiProperties.class)
public class FastApiConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    @Bean
    public FastApiClient fastApiClient(RestTemplate restTemplate, FastApiProperties props) {
        return new FastApiClientImpl(restTemplate, props.getUrl());
    }
}
