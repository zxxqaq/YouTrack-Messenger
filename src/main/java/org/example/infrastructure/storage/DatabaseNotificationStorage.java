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
    public boolean isSent(String notificationId) {
        return repository.existsByNotificationId(notificationId);
    }
    
    @Override
    @Transactional
    public void markAsSent(String notificationId) {
        if (!repository.existsByNotificationId(notificationId)) {
            SentNotification record = new SentNotification(notificationId, null, null, null);
            repository.save(record);
            System.out.println("[Storage] Marked notification " + notificationId + " as sent");
        }
    }

    @Override
    @Transactional
    public void markAsSentWithTimestamp(String notificationId, String issueId, String title, String updatedTimestamp) {
        if (!repository.existsByNotificationId(notificationId)) {
            SentNotification record = new SentNotification(notificationId, issueId, title, updatedTimestamp);
            repository.save(record);
            System.out.println("[Storage] Marked notification " + notificationId + " as sent with timestamp " + updatedTimestamp);
        }
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
    
    @Override
    @Transactional
    public void clearSentRecords() {
        repository.deleteAll();
        System.out.println("[Storage] Cleared all sent records");
    }
    
    @Override
    public String getLatestNotificationTimestamp() {
        String latestTimestamp = repository.findLatestNotificationTimestamp();
        System.out.println("[Storage] Latest notification timestamp: " + (latestTimestamp != null ? latestTimestamp : "none"));
        return latestTimestamp;
    }

    @Override
    @Transactional
    public void updateLatestTimestamp(String timestamp) {
        if (timestamp != null) {
            // Create a special record to track the latest timestamp
            // We'll use a special notification ID to identify this record
            String timestampRecordId = "LATEST_TIMESTAMP_RECORD";
            
            // Delete existing timestamp record if any
            repository.deleteById(timestampRecordId);
            
            // Create new timestamp record
            SentNotification timestampRecord = new SentNotification(timestampRecordId, null, null, timestamp);
            repository.save(timestampRecord);
            
            System.out.println("[Storage] Updated latest timestamp to: " + timestamp);
        }
    }
    
    /**
     * Clean up old records (older than specified days)
     */
    @Transactional
    public int cleanupOldRecords(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        int deletedCount = repository.deleteBySentAtBefore(cutoffDate);
        System.out.println("[Storage] Cleaned up " + deletedCount + " old records");
        return deletedCount;
    }
    
    /**
     * Get storage statistics
     */
    public StorageStats getStats() {
        long totalCount = repository.count();
        return new StorageStats(totalCount);
    }
    
    public static class StorageStats {
        private final long totalRecords;
        
        public StorageStats(long totalRecords) {
            this.totalRecords = totalRecords;
        }
        
        public long getTotalRecords() { return totalRecords; }
        
        @Override
        public String toString() {
            return "StorageStats{totalRecords=" + totalRecords + "}";
        }
    }
}
