package org.example.app;

import org.example.infrastructure.telegram.TelegramProperties;
import org.example.infrastructure.youtrack.YouTrackProperties;
import org.example.infrastructure.scheduler.SchedulerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "org.example")
@EnableConfigurationProperties({ YouTrackProperties.class, TelegramProperties.class, SchedulerProperties.class })
@EnableScheduling
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}


