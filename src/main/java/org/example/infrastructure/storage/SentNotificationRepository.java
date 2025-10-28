package org.example.infrastructure.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
public interface SentNotificationRepository extends JpaRepository<SentNotification, String> {

    /**
     * Count total records
     */
    long count();

    /**
     * Find notifications by IDs
     */
    List<SentNotification> findByNotificationIdIn(Set<String> notificationIds);

}
