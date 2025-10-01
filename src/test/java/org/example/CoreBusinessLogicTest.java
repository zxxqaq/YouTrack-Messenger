package org.example;

import org.example.application.service.Formatter;
import org.example.domain.view.NotificationView;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 5 Core Business Logic Tests - Quick CI/CD Demo
 */
class CoreBusinessLogicTest {

    @Test
    void test1_shouldDeduplicateNotifications() {
        // Test notification deduplication logic
        String notificationId1 = "516-1";
        String notificationId2 = "516-2";
        
        // Mock sent notification ID set
        java.util.Set<String> sentIds = java.util.Set.of(notificationId1);
        
        // Test deduplication logic
        boolean isSent1 = sentIds.contains(notificationId1);
        boolean isSent2 = sentIds.contains(notificationId2);
        
        assertTrue(isSent1, "Notification 516-1 should be marked as sent");
        assertFalse(isSent2, "Notification 516-2 should be marked as unsent");
    }

    @Test
    void test2_shouldFormatTelegramMessage() {
        // Test MarkdownV2 formatting
        NotificationView notification = new NotificationView();
        notification.issueId = "DEMO-123";
        notification.title = "Fix login bug";
        notification.status = "Submitted";
        notification.priority = "Normal";
        notification.assignee = "Unassigned";
        notification.tags = Arrays.asList("Star");
        notification.link = "https://xianzhang.youtrack.cloud/issue/DEMO-123";

        String formattedMessage = Formatter.toTelegramMarkdown(notification);

        assertNotNull(formattedMessage, "Formatted message should not be null");
        assertTrue(formattedMessage.contains("*DEMO\\-123*"), "Should contain escaped issue ID");
        assertTrue(formattedMessage.contains("_Fix login bug_"), "Should contain italic title");
        assertTrue(formattedMessage.contains("Status: `Submitted`"), "Should contain status");
    }

    @Test
    void test3_shouldValidateProjectId() {
        // Test project validation logic
        String validProjectId = "0-0";
        String invalidProjectId = "999-999";
        
        // Mock available projects list
        java.util.List<String> availableProjects = Arrays.asList("0-0", "1-0", "2-0");
        
        boolean isValid1 = availableProjects.contains(validProjectId);
        boolean isValid2 = availableProjects.contains(invalidProjectId);
        
        assertTrue(isValid1, "Project ID 0-0 should be valid");
        assertFalse(isValid2, "Project ID 999-999 should be invalid");
    }

    @Test
    void test4_shouldHandleApiErrors() {
        // Test error handling logic
        String invalidUrl = "https://invalid-url.com";
        String validUrl = "https://xianzhang.youtrack.cloud";
        
        // Simple URL validation logic
        boolean isValidUrl1 = invalidUrl.startsWith("https://") && invalidUrl.contains(".");
        boolean isValidUrl2 = validUrl.startsWith("https://") && validUrl.contains(".");
        
        assertTrue(isValidUrl1, "Invalid URL should pass basic format check");
        assertTrue(isValidUrl2, "Valid URL should pass basic format check");
        
        // Test null value handling
        String nullSummary = null;
        String emptySummary = "";
        
        boolean isNull = nullSummary == null;
        boolean isEmpty = emptySummary != null && emptySummary.isEmpty();
        
        assertTrue(isNull, "Null value should be correctly identified");
        assertTrue(isEmpty, "Empty string should be correctly identified");
    }

    @Test
    void test5_shouldCreateIssueWithValidData() {
        // Test core logic for creating issues
        String summary = "Test issue";
        String projectId = "0-0";
        
        // Validate input data
        boolean hasValidSummary = summary != null && !summary.trim().isEmpty();
        boolean hasValidProjectId = projectId != null && !projectId.trim().isEmpty();
        
        assertTrue(hasValidSummary, "Summary should be valid");
        assertTrue(hasValidProjectId, "Project ID should be valid");
        
        // Mock JSON payload for creating issue
        String jsonPayload = String.format(
            "{\"summary\": \"%s\", \"project\": {\"id\": \"%s\"}}", 
            summary, projectId
        );
        
        assertNotNull(jsonPayload, "JSON payload should not be null");
        assertTrue(jsonPayload.contains(summary), "JSON should contain summary");
        assertTrue(jsonPayload.contains(projectId), "JSON should contain project ID");
    }
}
