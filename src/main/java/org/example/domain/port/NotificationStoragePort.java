package org.example.domain.port;

import java.util.Set;

public interface NotificationStoragePort {
    /**
     * Check if notification ID has been sent before
     */
    boolean isSent(String notificationId);
    
    /**
     * Mark notification ID as sent
     */
    void markAsSent(String notificationId);

    /**
     * Mark notification ID as sent with timestamp information
     */
    void markAsSentWithTimestamp(String notificationId, String issueId, String title, String updatedTimestamp);

    /**
     * Mark multiple notification IDs as sent
     */
    void markAsSent(Set<String> notificationIds);
    
    /**
     * Get all sent notification IDs
     */
    Set<String> getAllSentIds();
    
    /**
     * Clear sent records (optional, for maintenance)
     */
    void clearSentRecords();
    
    /**
     * Get the latest notification timestamp for cursor-based fetching
     */
    String getLatestNotificationTimestamp();

    /**
     * Update the latest notification timestamp
     */
    void updateLatestTimestamp(String timestamp);
}
