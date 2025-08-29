package com.threeboys.toneup.socketio.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-key.path}")
    private String SERVICE_KEY_PATH;


    @Bean
    public FirebaseApp firebaseApp() {
        try {
//            InputStream serviceAccount =new ClassPathResource(SERVICE_KEY_PATH).getInputStream();

            FileInputStream serviceAccount =
                    new FileInputStream("src/main/resources/firebase/service-key.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(
                            GoogleCredentials.fromStream(serviceAccount)
                    )
                    .build();

            log.info("Successfully initialized firebase app!!");
            System.out.println(options.getProjectId());
            System.out.println(options.getServiceAccountId());
            System.out.println(options.getHttpTransport());

            return FirebaseApp.initializeApp(options);

        } catch (IOException exception) {
            log.error("Fail to initialize firebase app{}", exception.getMessage());
            return null;
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
