package org.example.domain.port;

import java.util.Set;

public interface NotificationStoragePort {
    /**
     * Mark multiple notification IDs as sent
     */
    void markAsSent(Set<String> notificationIds);

    /**
     * Get all sent notification IDs
     */
    Set<String> getAllSentIds();
}
