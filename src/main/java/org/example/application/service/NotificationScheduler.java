package org.example.application.service;

import org.example.domain.port.MessengerPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@ConditionalOnProperty(prefix = "scheduler", name = "enabled", havingValue = "true", matchIfMissing = true)
public class NotificationScheduler {

    private final NotifyIssueService notifyIssueService;
    private final MessengerPort messengerPort;
    private final SystemHealthService healthService;
    private static final int MAX_FAILURES_BEFORE_ALERT = 3;
    
    private volatile boolean isRunning = false; // Control flag for scheduler

    @Value("${scheduler.top:1000S}")
    private int top;

    public NotificationScheduler(NotifyIssueService notifyIssueService, 
                                MessengerPort messengerPort,
                                SystemHealthService healthService) {
        this.notifyIssueService = notifyIssueService;
        this.messengerPort = messengerPort;
        this.healthService = healthService;
    }

    // Configurable via application properties - no initial delay, starts only when user enables it
    @Scheduled(
            fixedDelayString = "${scheduler.fixed-delay:PT10M}"
    )
    public void pullAndBroadcast() {
        // Check if scheduler is enabled by user
        if (!isRunning) {
            return; // Skip execution if not enabled
        }
        try {
            notifyIssueService.sendAllToPm(top);
            
            // Record success and send recovery notification if recovering from failures
            boolean wasFaili = healthService.hasRecentFailures();
            healthService.recordSuccess();
            
            if (wasFaili) {
                sendRecoveryNotification();
            }
        } catch (Exception e) {
            String errorType = determineErrorType(e);
            healthService.recordFailure(errorType, e.getMessage());
            
            int failures = healthService.getConsecutiveFailures();
            System.err.println("[Scheduler] Broadcast failed (attempt " + failures + "): " + e.getMessage());
            e.printStackTrace();
            
            // Send alert to PM after multiple consecutive failures
            if (failures >= MAX_FAILURES_BEFORE_ALERT) {
                sendFailureAlert(errorType, e.getMessage(), failures);
            }
        }
    }

    private String determineErrorType(Exception e) {
        String message = e.getMessage();
        if (message == null) {
            return "Unknown Error";
        }
        
        if (message.contains("YouTrack") || message.contains("HTTP")) {
            return "YouTrack Connection Error";
        } else if (message.contains("database") || message.contains("SQL") || message.contains("H2")) {
            return "Database Error";
        } else if (message.contains("Telegram") || message.contains("Bot")) {
            return "Telegram API Error";
        } else {
            return "System Error";
        }
    }

    private void sendFailureAlert(String errorType, String errorMessage, int failures) {
        try {
            String escapedErrorType = escapeMarkdownV2(errorType);
            String escapedErrorMessage = escapeMarkdownV2(errorMessage);
            
            String alertMsg = String.format(
                "🚨 *Notification Scheduler Alert*\n\n" +
                "❌ *Status\\:* Failed after %d consecutive attempts\n" +
                "🔍 *Error Type\\:* %s\n" +
                "📝 *Details\\:* %s\n\n" +
                "⚠️ The system will continue retrying automatically\\.\n" +
                "💡 Use `/status` to check current system health\\.\n" +
                "📋 Check the application logs for more details\\.",
                failures, escapedErrorType, escapedErrorMessage
            );
            
            messengerPort.sendToPm(alertMsg);
            System.out.println("[Scheduler] Failure alert sent to PM");
        } catch (IOException telegramError) {
            System.err.println("[Scheduler] Failed to send failure alert to Telegram: " + telegramError.getMessage());
        }
    }

    private void sendRecoveryNotification() {
        try {
            String recoveryMsg = 
                "✅ *Notification Scheduler Recovered*\n\n" +
                "The notification scheduler has successfully recovered and is now operating normally\\.";
            
            messengerPort.sendToPm(recoveryMsg);
            System.out.println("[Scheduler] Recovery notification sent to PM");
        } catch (IOException telegramError) {
            System.err.println("[Scheduler] Failed to send recovery notification to Telegram: " + telegramError.getMessage());
        }
    }

    private String escapeMarkdownV2(String text) {
        if (text == null) return "";
        return text
            .replace("\\", "\\\\")
            .replace("-", "\\-")
            .replace(".", "\\.")
            .replace("(", "\\(")
            .replace(")", "\\)")
            .replace("{", "\\{")
            .replace("}", "\\}")
            .replace("_", "\\_")
            .replace("*", "\\*")
            .replace("[", "\\[")
            .replace("]", "\\]")
            .replace("~", "\\~")
            .replace("`", "\\`")
            .replace(">", "\\>")
            .replace("#", "\\#")
            .replace("+", "\\+")
            .replace("=", "\\=")
            .replace("|", "\\|")
            .replace("!", "\\!")
            .replace(":", "\\:");
    }
    
    /**
     * Start the scheduler - enables notification pulling
     */
    public void start() {
        if (!isRunning) {
            isRunning = true;
            System.out.println("[Scheduler] Started by user command");
        }
    }
    
    /**
     * Stop the scheduler - disables notification pulling
     */
    public void stop() {
        if (isRunning) {
            isRunning = false;
            System.out.println("[Scheduler] Stopped by user command");
        }
    }
    
    /**
     * Check if scheduler is currently running
     */
    public boolean isRunning() {
        return isRunning;
    }
}


