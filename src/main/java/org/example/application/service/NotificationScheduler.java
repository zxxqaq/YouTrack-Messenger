package org.example.application.service;

import org.example.domain.port.MessengerPort;
import org.example.infrastructure.scheduler.SchedulerProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Duration;

@Component
@ConditionalOnProperty(prefix = "scheduler", name = "enabled", havingValue = "true", matchIfMissing = true)
public class NotificationScheduler {

    private final NotifyIssueService notifyIssueService;
    private final MessengerPort messengerPort;
    private final SystemHealthService healthService;
    private final SchedulerProperties schedulerProperties;
    
    private volatile boolean isRunning = false; // Control flag for scheduler
    private volatile boolean isPaused = false; // Paused due to errors
    private volatile LocalDateTime pausedUntil = null; // When to automatically resume
    private volatile boolean hasSentPauseAlert = false; // Track if pause alert was sent

    @Value("${scheduler.top:1000}")
    private int top;

    public NotificationScheduler(NotifyIssueService notifyIssueService,
                                MessengerPort messengerPort,
                                SystemHealthService healthService,
                                SchedulerProperties schedulerProperties) {
        this.notifyIssueService = notifyIssueService;
        this.messengerPort = messengerPort;
        this.healthService = healthService;
        this.schedulerProperties = schedulerProperties;
    }

    // Configurable via application properties - no initial delay, starts only when user enables it
    @Scheduled(
            fixedDelayString = "${scheduler.fixed-delay:PT5S}"
    )
    public void pullAndBroadcast() {
        // Check if scheduler is enabled by user
        if (!isRunning) {
            return; // Skip execution if not enabled
        }

        // Check if auto-resume time has passed
        if (isPaused && pausedUntil != null) {
            if (LocalDateTime.now().isAfter(pausedUntil)) {
                System.out.println("[Scheduler] Auto-resuming after pause period");
                isPaused = false;
                pausedUntil = null;
                hasSentPauseAlert = false;
            } else {
                // Still in pause period, skip execution
                return;
            }
        }

        // If paused and no auto-resume time set, skip execution
        if (isPaused) {
            return;
        }

        try {
            notifyIssueService.sendAllToPm(top);

            // Record success and send recovery notification if recovering from failures
            boolean wasFaili = healthService.hasRecentFailures();
            healthService.recordSuccess();

            // Reset pause state on success
            if (isPaused) {
                System.out.println("[Scheduler] Recovery detected, resuming scheduler");
                isPaused = false;
                pausedUntil = null;
                hasSentPauseAlert = false;
            }

            if (wasFaili) {
                sendRecoveryNotification();
            }
        } catch (Exception e) {
            String errorType = determineErrorType(e);
            healthService.recordFailure(errorType, e.getMessage());

            int failures = healthService.getConsecutiveFailures();
            System.err.println("[Scheduler] Broadcast failed (attempt " + failures + "): " + e.getMessage());
            e.printStackTrace();

            SchedulerProperties.CircuitBreaker circuitBreaker = schedulerProperties.getCircuitBreaker();
            int maxFailures = circuitBreaker.getMaxConsecutiveFailures();
            boolean autoPause = circuitBreaker.isAutoPause();

            // Send alert and pause if exceeds threshold
            if (failures >= maxFailures) {
                if (!hasSentPauseAlert && circuitBreaker.isSendSingleAlert()) {
                    sendFailureAlertWithPause(errorType, e.getMessage(), failures);
                    hasSentPauseAlert = true;
                }

                // Auto-pause if enabled
                if (autoPause && !isPaused) {
                    pauseScheduler(circuitBreaker.getPauseDuration());
                }
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

    private void pauseScheduler(String pauseDuration) {
        isPaused = true;
        hasSentPauseAlert = true; // Mark as sent to prevent duplicates
        
        // Parse duration and set pause end time
        Duration duration = parseDuration(pauseDuration);
        pausedUntil = LocalDateTime.now().plus(duration);
        
        System.out.println("[Scheduler] Paused due to repeated failures. Will auto-resume at: " + pausedUntil);
    }

    private Duration parseDuration(String isoDuration) {
        // Parse ISO 8601 duration (e.g., PT1H, PT30M, PT1S)
        if (isoDuration.startsWith("PT")) {
            String timeStr = isoDuration.substring(2);
            if (timeStr.endsWith("H")) {
                int hours = Integer.parseInt(timeStr.substring(0, timeStr.length() - 1));
                return Duration.ofHours(hours);
            } else if (timeStr.endsWith("M")) {
                int minutes = Integer.parseInt(timeStr.substring(0, timeStr.length() - 1));
                return Duration.ofMinutes(minutes);
            } else if (timeStr.endsWith("S")) {
                int seconds = Integer.parseInt(timeStr.substring(0, timeStr.length() - 1));
                return Duration.ofSeconds(seconds);
            }
        }
        return Duration.ofHours(1); // Default to 1 hour
    }

    private void sendFailureAlertWithPause(String errorType, String errorMessage, int failures) {
        try {
            String escapedErrorType = escapeMarkdownV2(errorType);
            String escapedErrorMessage = escapeMarkdownV2(errorMessage);

            String alertMsg = String.format(
                "🚨 *Notification Scheduler Alert*\n\n" +
                "❌ *Status\\:* Failed after %d consecutive attempts\n" +
                "🔍 *Error Type\\:* %s\n" +
                "📝 *Details\\:* %s\n\n" +
                "⏸️ The scheduler has been paused to prevent further errors\\.\n" +
                "🔄 It will auto\\-resume after the configured period\\.\n" +
                "💡 Use `/start` to manually resume if needed\\.\n" +
                "📋 Check the application logs for more details\\.",
                failures, escapedErrorType, escapedErrorMessage
            );

            messengerPort.sendToPm(alertMsg);
            System.out.println("[Scheduler] Pause alert sent to PM");
        } catch (IOException telegramError) {
            System.err.println("[Scheduler] Failed to send pause alert to Telegram: " + telegramError.getMessage());
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
            isPaused = false;
            pausedUntil = null;
            hasSentPauseAlert = false;
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
     * Manually resume scheduler from paused state
     */
    public void resume() {
        if (isPaused) {
            isPaused = false;
            pausedUntil = null;
            hasSentPauseAlert = false;
            System.out.println("[Scheduler] Resumed by user command");
        }
    }

    /**
     * Check if scheduler is currently running
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Check if scheduler is paused due to errors
     */
    public boolean isPaused() {
        return isPaused;
    }
}


