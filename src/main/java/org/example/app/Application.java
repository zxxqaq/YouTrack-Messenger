package org.example.app;

import org.example.infrastructure.telegram.TelegramProperties;
import org.example.infrastructure.youtrack.YouTrackProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = "org.example")
@EnableConfigurationProperties({ YouTrackProperties.class, TelegramProperties.class })
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}


