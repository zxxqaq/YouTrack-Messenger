package org.example;

import org.example.application.service.Formatter;
import org.example.application.service.NotifyIssueService;
import org.example.domain.port.IssueTrackerPort;
import org.example.domain.port.MessengerPort;
import org.example.domain.port.NotificationStoragePort;
import org.example.domain.view.NotificationView;
import org.example.infrastructure.scheduler.SchedulerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Real core business logic tests
 * Tests actual service classes using Mockito
 */
class CoreBusinessLogicTest {

    @Mock
    private IssueTrackerPort issueTrackerPort;

    @Mock
    private MessengerPort messengerPort;

    @Mock
    private NotificationStoragePort storagePort;

    @Mock
    private SchedulerProperties schedulerProperties;

    private NotifyIssueService notifyIssueService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        SchedulerProperties.Pagination pagination = new SchedulerProperties.Pagination();
        pagination.setEnabled(false);
        pagination.setPageSize(1);
        pagination.setDelayBetweenMessages("PT1S");

        when(schedulerProperties.getPagination()).thenReturn(pagination);

        notifyIssueService = new NotifyIssueService(
            issueTrackerPort,
            messengerPort,
            storagePort,
            schedulerProperties
        );
    }

    @Test
    void test1_shouldDeduplicateNotifications() throws IOException {
        List<NotificationView> allNotifications = Arrays.asList(
            createNotification("516-1", "BUG-1", "Test issue 1"),
            createNotification("516-2", "BUG-2", "Test issue 2")
        );

        when(storagePort.getAllSentIds()).thenReturn(Set.of("516-1"));
        when(issueTrackerPort.fetchNotifications(anyInt())).thenReturn(allNotifications);

        notifyIssueService.sendAllToPm(10);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(messengerPort, times(1)).sendToPm(messageCaptor.capture());

        String sentMessage = messageCaptor.getValue();
        assertTrue(sentMessage.contains("BUG-2"), "Only unsent notification BUG-2 should be sent");

        verify(storagePort, times(1)).markAsSent(anySet());
    }

    @Test
    void test2_shouldFormatTelegramMessage() {
        NotificationView notification = new NotificationView();
        notification.issueId = "DEMO-123";
        notification.title = "Fix login bug";
        notification.status = "Submitted";
        notification.priority = "Normal";
        notification.assignee = "Unassigned";
        notification.tags = Arrays.asList("Star");
        notification.link = "https://example.com/issue/DEMO-123";

        String formattedMessage = Formatter.toTelegramMarkdown(notification);

        assertNotNull(formattedMessage, "Formatted message should not be null");
        assertTrue(formattedMessage.contains("*DEMO\\-123*"), "Should contain escaped issue ID");
        assertTrue(formattedMessage.contains("_Fix login bug_"), "Should contain italic title");
        assertTrue(formattedMessage.contains("Status: `Submitted`"), "Should contain status");
        assertTrue(formattedMessage.contains("Priority: `Normal`"), "Should contain priority");
        assertTrue(formattedMessage.contains("Assignee: `Unassigned`"), "Should contain assignee");
        assertTrue(formattedMessage.contains("Tags: `Star`"), "Should contain tags");
    }

    @Test
    void test3_shouldSendAllNotificationsWhenNoneAreSent() throws IOException {
        List<NotificationView> allNotifications = Arrays.asList(
            createNotification("516-1", "BUG-1", "Issue 1"),
            createNotification("516-2", "BUG-2", "Issue 2")
        );

        when(storagePort.getAllSentIds()).thenReturn(Collections.emptySet());
        when(issueTrackerPort.fetchNotifications(anyInt())).thenReturn(allNotifications);

        notifyIssueService.sendAllToPm(10);

        verify(messengerPort, times(2)).sendToPm(anyString());

        verify(storagePort, times(1)).markAsSent(anySet());
    }

    @Test
    void test4_shouldSkipAlreadySentNotifications() throws IOException {
        List<NotificationView> allNotifications = Arrays.asList(
            createNotification("516-1", "BUG-1", "Issue 1"),
            createNotification("516-2", "BUG-2", "Issue 2")
        );

        when(storagePort.getAllSentIds()).thenReturn(Set.of("516-1", "516-2"));
        when(issueTrackerPort.fetchNotifications(anyInt())).thenReturn(allNotifications);

        notifyIssueService.sendAllToPm(10);

        verify(messengerPort, never()).sendToPm(anyString());

        verify(storagePort, never()).markAsSent(anySet());
    }

    @Test
    void test5_shouldHandleEmptyNotificationsList() throws IOException {
        when(storagePort.getAllSentIds()).thenReturn(Collections.emptySet());
        when(issueTrackerPort.fetchNotifications(anyInt())).thenReturn(Collections.emptyList());

        notifyIssueService.sendAllToPm(10);

        verify(messengerPort, never()).sendToPm(anyString());
        verify(storagePort, never()).markAsSent(anySet());
    }

    @Test
    void test6_shouldFormatNotificationWithSpecialCharacters() {
        NotificationView notification = new NotificationView();
        notification.issueId = "TEST-123";
        notification.title = "Fix [urgent] bug (critical)";
        notification.status = "In Progress";
        notification.priority = "High";
        notification.comment = "```code block```";
        notification.link = "https://example.com/test(123)";

        String formattedMessage = Formatter.toTelegramMarkdown(notification);

        assertTrue(formattedMessage.contains("Fix \\[urgent\\] bug \\(critical\\)"));
        assertTrue(formattedMessage.contains("```"));
        assertTrue(formattedMessage.contains("``\\`"));
    }

    @Test
    void test7_shouldHandlePaginationWhenEnabled() throws IOException {
        SchedulerProperties.Pagination pagination = new SchedulerProperties.Pagination();
        pagination.setEnabled(true);
        pagination.setPageSize(2);
        pagination.setDelayBetweenMessages("PT1S");

        when(schedulerProperties.getPagination()).thenReturn(pagination);

        notifyIssueService = new NotifyIssueService(
            issueTrackerPort,
            messengerPort,
            storagePort,
            schedulerProperties
        );

        List<NotificationView> allNotifications = Arrays.asList(
            createNotification("516-1", "BUG-1", "Issue 1"),
            createNotification("516-2", "BUG-2", "Issue 2"),
            createNotification("516-3", "BUG-3", "Issue 3")
        );

        when(storagePort.getAllSentIds()).thenReturn(Collections.emptySet());
        when(issueTrackerPort.fetchNotifications(anyInt())).thenReturn(allNotifications);

        notifyIssueService.sendAllToPm(10);

        verify(messengerPort, times(3)).sendToPm(anyString());
    }


    private NotificationView createNotification(String id, String issueId, String title) {
        NotificationView notification = new NotificationView();
        notification.id = id;
        notification.issueId = issueId;
        notification.title = title;
        notification.status = "Submitted";
        notification.priority = "Normal";
        notification.assignee = "Unassigned";
        notification.link = "https://example.com/" + issueId;
        return notification;
    }
}
