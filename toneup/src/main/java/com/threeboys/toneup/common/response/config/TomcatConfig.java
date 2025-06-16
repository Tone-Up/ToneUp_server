//package com.threeboys.toneup.common.response.config;
//
//
//import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
//import org.springframework.boot.web.server.WebServerFactoryCustomizer;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.concurrent.Executors;
//
//@Configuration
//public class TomcatConfig {
//
//    @Bean
//    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> virtualThreadTomcatCustomizer() {
//        return factory -> factory.addConnectorCustomizers(connector -> {
//            connector.getProtocolHandler()
//                    .setExecutor(Executors.newVirtualThreadPerTaskExecutor());
//        });
//    }
//}