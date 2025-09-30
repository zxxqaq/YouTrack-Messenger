package org.example.application.service;

import org.example.domain.port.IssueTrackerPort;
import org.example.domain.port.MessengerPort;
import org.example.domain.port.NotificationStoragePort;
import org.example.domain.view.NotificationView;
import org.example.infrastructure.scheduler.SchedulerProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NotifyIssueService {

    private final IssueTrackerPort issueTrackerPort;
    private final MessengerPort messengerPort;
    private final NotificationStoragePort storagePort;
    private final SchedulerProperties schedulerProperties;

    public NotifyIssueService(IssueTrackerPort issueTrackerPort, MessengerPort messengerPort, 
                             NotificationStoragePort storagePort, SchedulerProperties schedulerProperties) {
        this.issueTrackerPort = issueTrackerPort;
        this.messengerPort = messengerPort;
        this.storagePort = storagePort;
        this.schedulerProperties = schedulerProperties;
    }

    public List<NotificationView> fetch(int top) throws IOException {
        return issueTrackerPort.fetchNotifications(top);
    }


    public void sendAllToPm(int top) throws IOException {
        System.out.println("Starting to fetch notifications and send to PM, top=" + top);

        // Fetch notifications with specified top parameter
        List<NotificationView> allNotifications = fetch(top);
        System.out.println("Fetched " + allNotifications.size() + " total notifications");

        // Debug: Print all notification IDs
        for (NotificationView n : allNotifications) {
            System.out.println("Notification ID: " + n.id + ", Issue ID: " + n.issueId + ", Title: " + n.title);
        }

        // Get all sent notification IDs from storage
        Set<String> sentIds = storagePort.getAllSentIds();
        System.out.println("Already sent " + sentIds.size() + " notifications");

        // Filter out already sent notifications
        List<NotificationView> newNotifications = allNotifications.stream()
                .filter(n -> !sentIds.contains(n.id))
                .collect(Collectors.toList());

        System.out.println("Found " + newNotifications.size() + " new notifications (after deduplication)");

        if (newNotifications.isEmpty()) {
            System.out.println("No new notifications to send");
            return;
        }

        // Send notifications with pagination
        sendNotificationsWithPaginationToPm(newNotifications);

        // Mark new notifications as sent
        Set<String> newSentIds = newNotifications.stream()
                .map(n -> n.id)
                .collect(Collectors.toSet());
        storagePort.markAsSent(newSentIds);
        System.out.println("All new notifications sent to PM successfully and marked as sent");
    }
    


    private void sendNotificationsWithPaginationToPm(List<NotificationView> notifications) throws IOException {
        SchedulerProperties.Pagination pagination = schedulerProperties.getPagination();
        
        if (pagination.isEnabled()) {
            // Send with pagination
            int pageSize = pagination.getPageSize();
            int delayMs = parseDurationToMs(pagination.getDelayBetweenMessages());
            
            System.out.println("Sending to PM with pagination: pageSize=" + pageSize + ", delay=" + delayMs + "ms");
            
            for (int i = 0; i < notifications.size(); i += pageSize) {
                int endIndex = Math.min(i + pageSize, notifications.size());
                List<NotificationView> page = notifications.subList(i, endIndex);
                
                System.out.println("Sending page " + (i/pageSize + 1) + " (" + page.size() + " notifications) to PM");
                
                for (NotificationView n : page) {
                    String msg = formatForTelegram(n);
                    System.out.println("Sending message: " + msg);
                    messengerPort.sendToPm(msg);
                    
                    if (delayMs > 0) {
                        try {
                            Thread.sleep(delayMs);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
        } else {
            // Send all at once
            for (NotificationView n : notifications) {
                String msg = formatForTelegram(n);
                System.out.println("Sending message: " + msg);
                messengerPort.sendToPm(msg);
            }
        }
    }
    
    private int parseDurationToMs(String duration) {
        // Simple parser for PT1S, PT2S, etc.
        if (duration.startsWith("PT") && duration.endsWith("S")) {
            try {
                String seconds = duration.substring(2, duration.length() - 1);
                return Integer.parseInt(seconds) * 1000;
            } catch (NumberFormatException e) {
                return 1000; // Default 1 second
            }
        }
        return 1000; // Default 1 second
    }
    

    private String formatForTelegram(NotificationView n) {
        return Formatter.toTelegramMarkdown(n);
    }

}


