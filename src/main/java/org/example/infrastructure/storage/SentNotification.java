package org.example.infrastructure.storage;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sent_notifications")
public class SentNotification {

    @Id
    @Column(name = "notification_id")
    private String notificationId;


    // Default constructor for JPA
    public SentNotification() {}

    public SentNotification(String notificationId) {
        this.notificationId = notificationId;
    }

    // Getters and setters
    public String getNotificationId() { return notificationId; }
}
