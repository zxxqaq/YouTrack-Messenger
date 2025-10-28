package org.example.infrastructure.storage;

import org.example.domain.port.NotificationStoragePort;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DatabaseNotificationStorage implements NotificationStoragePort {

    private final SentNotificationRepository repository;

    public DatabaseNotificationStorage(SentNotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void markAsSent(Set<String> notificationIds) {
        // Filter out already existing ones
        List<String> existingIds = repository.findByNotificationIdIn(notificationIds)
                .stream()
                .map(SentNotification::getNotificationId)
                .collect(Collectors.toList());

        Set<String> newIds = new HashSet<>(notificationIds);
        newIds.removeAll(existingIds);

        if (!newIds.isEmpty()) {
            List<SentNotification> records = newIds.stream()
                    .map(id -> new SentNotification(id, null, null, null))
                    .collect(Collectors.toList());

            repository.saveAll(records);
            System.out.println("[Storage] Marked " + newIds.size() + " notifications as sent");
        }
    }

    @Override
    public Set<String> getAllSentIds() {
        return repository.findAll()
                .stream()
                .map(SentNotification::getNotificationId)
                .collect(Collectors.toSet());
    }

}
