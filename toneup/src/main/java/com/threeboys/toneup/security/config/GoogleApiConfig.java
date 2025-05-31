package com.threeboys.toneup.security.config;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleApiConfig {

    @Bean
    public NetHttpTransport netHttpTransport() {
        return new NetHttpTransport();
    }
    @Bean
    public JsonFactory jsonFactory() {
        return GsonFactory.getDefaultInstance();
    }
}
