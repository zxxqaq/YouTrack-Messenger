package org.example.application.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "scheduler", name = "enabled", havingValue = "true", matchIfMissing = true)
public class NotificationScheduler {

    private final NotifyIssueService notifyIssueService;

    @Value("${scheduler.top:5}")
    private int top;

    public NotificationScheduler(NotifyIssueService notifyIssueService) {
        this.notifyIssueService = notifyIssueService;
    }

    // Configurable via application properties
    @Scheduled(
            fixedDelayString = "${scheduler.fixed-delay:PT10M}",
            initialDelayString = "${scheduler.initial-delay:PT10M}"
    )
    public void pullAndBroadcast() {
        try {
            notifyIssueService.sendAllToPm(top);
        } catch (Exception e) {
            System.err.println("[Scheduler] broadcast failed: " + e.getMessage());
        }
    }
}


