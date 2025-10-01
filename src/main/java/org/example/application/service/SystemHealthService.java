package org.example.application.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service to track system health and scheduler status
 */
@Service
public class SystemHealthService {
    
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private final AtomicReference<LocalDateTime> lastSuccessTime = new AtomicReference<>(null);
    private final AtomicReference<LocalDateTime> lastFailureTime = new AtomicReference<>(null);
    private final AtomicReference<String> lastErrorMessage = new AtomicReference<>(null);
    private final AtomicReference<String> lastErrorType = new AtomicReference<>(null);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public void recordSuccess() {
        consecutiveFailures.set(0);
        lastSuccessTime.set(LocalDateTime.now());
        lastErrorMessage.set(null);
        lastErrorType.set(null);
    }
    
    public void recordFailure(String errorType, String errorMessage) {
        consecutiveFailures.incrementAndGet();
        lastFailureTime.set(LocalDateTime.now());
        lastErrorMessage.set(errorMessage);
        lastErrorType.set(errorType);
    }
    
    public int getConsecutiveFailures() {
        return consecutiveFailures.get();
    }
    
    public String getLastSuccessTime() {
        LocalDateTime time = lastSuccessTime.get();
        return time != null ? time.format(formatter) : "Never";
    }
    
    public String getLastFailureTime() {
        LocalDateTime time = lastFailureTime.get();
        return time != null ? time.format(formatter) : "Never";
    }
    
    public String getLastErrorMessage() {
        return lastErrorMessage.get();
    }
    
    public String getLastErrorType() {
        return lastErrorType.get();
    }
    
    public boolean hasRecentFailures() {
        return consecutiveFailures.get() > 0;
    }
    
    public String getSchedulerStatus() {
        int failures = consecutiveFailures.get();
        if (failures == 0) {
            return "✅ Healthy";
        } else if (failures < 3) {
            return "⚠️ Warning \\(" + failures + " failure\\(s\\)\\)";
        } else {
            return "❌ Failing \\(" + failures + " consecutive failures\\)";
        }
    }
}

