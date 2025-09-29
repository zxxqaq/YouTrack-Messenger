package org.example.application.service;

import org.example.domain.port.IssueTrackerPort;
import org.example.domain.port.MessengerPort;
import org.example.domain.view.NotificationView;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class NotifyIssueService {

    private final IssueTrackerPort issueTrackerPort;
    private final MessengerPort messengerPort;

    public NotifyIssueService(IssueTrackerPort issueTrackerPort, MessengerPort messengerPort) {
        this.issueTrackerPort = issueTrackerPort;
        this.messengerPort = messengerPort;
    }

    public List<NotificationView> fetch(int top) throws IOException {
        return issueTrackerPort.fetchNotifications(top);
    }

    public void sendAllToGroup(int top) throws IOException {
        System.out.println("Starting to fetch notifications, top=" + top);
        List<NotificationView> list = fetch(top);
        System.out.println("Fetched " + list.size() + " notifications");
        for (NotificationView n : list) {
            String msg = formatForTelegram(n);
            System.out.println("Sending message: " + msg);
            messengerPort.sendToGroup(msg);
        }
        System.out.println("All notifications sent successfully");
    }

    private String formatForTelegram(NotificationView n) {
        return Formatter.toTelegramMarkdown(n);
    }

}


