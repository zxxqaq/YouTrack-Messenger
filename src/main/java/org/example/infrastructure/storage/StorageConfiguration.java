package org.example.infrastructure.storage;

import org.example.domain.port.NotificationStoragePort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfiguration {
    
    @Bean
    public NotificationStoragePort databaseNotificationStorage(SentNotificationRepository repository) {
        return new DatabaseNotificationStorage(repository);
    }
}
