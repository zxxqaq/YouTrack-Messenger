package org.example.infrastructure.storage;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sent_notifications")
public class SentNotification {
    
    @Id
    @Column(name = "notification_id", length = 255)
    private String notificationId;
    
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
    
    @Column(name = "issue_id", length = 100)
    private String issueId;
    
    @Column(name = "title", length = 500)
    private String title;

    @Column(name = "updated_timestamp", length = 50)
    private String updatedTimestamp;

    // Default constructor for JPA
    public SentNotification() {}

    public SentNotification(String notificationId, String issueId, String title, String updatedTimestamp) {
        this.notificationId = notificationId;
        this.issueId = issueId;
        this.title = title;
        this.updatedTimestamp = updatedTimestamp;
        this.sentAt = LocalDateTime.now();
    }
    
    // Getters and setters
    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }
    
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    
    public String getIssueId() { return issueId; }
    public void setIssueId(String issueId) { this.issueId = issueId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getUpdatedTimestamp() { return updatedTimestamp; }
    public void setUpdatedTimestamp(String updatedTimestamp) { this.updatedTimestamp = updatedTimestamp; }
}
