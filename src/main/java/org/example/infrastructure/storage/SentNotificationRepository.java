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
     * Check if notification ID exists
     */
    boolean existsByNotificationId(String notificationId);
    
    /**
     * Find notifications by issue ID
     */
    List<SentNotification> findByIssueId(String issueId);
    
    /**
     * Delete old records (for maintenance)
     */
    @Modifying
    @Query("DELETE FROM SentNotification s WHERE s.sentAt < :cutoffDate")
    int deleteBySentAtBefore(LocalDateTime cutoffDate);
    
    /**
     * Count total records
     */
    long count();
    
    /**
     * Find notifications by IDs
     */
    List<SentNotification> findByNotificationIdIn(Set<String> notificationIds);
    
    /**
     * Get the latest notification timestamp (for cursor-based fetching)
     */
    @Query("SELECT s.updatedTimestamp FROM SentNotification s WHERE s.updatedTimestamp IS NOT NULL ORDER BY s.updatedTimestamp DESC LIMIT 1")
    String findLatestNotificationTimestamp();
}
