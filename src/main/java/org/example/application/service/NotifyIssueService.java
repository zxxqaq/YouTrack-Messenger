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
        // 简化格式，避免复杂的MarkdownV2转义
        String title = n.title != null ? n.title : "No title";
        String status = n.status != null ? n.status : "Unknown";
        String link = n.link != null ? n.link : "";
        String header = n.header != null ? n.header : "";
        String comment = n.comment != null && !n.comment.isBlank() ? n.comment : "";
        
        StringBuilder sb = new StringBuilder();
        sb.append("📋 ").append(title).append("\n");
        sb.append("📊 Status: ").append(status).append("\n");
        if (!header.isBlank()) {
            sb.append("📝 ").append(header).append("\n");
        }
        if (!comment.isBlank()) {
            sb.append("💬 ").append(comment).append("\n");
        }
        if (!link.isBlank()) {
            sb.append("🔗 ").append(link);
        }
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        // Minimal MarkdownV2 escaping for Telegram
        return s.replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace(">", "\\>")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace("=", "\\=")
                .replace("|", "\\|")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace(".", "\\.")
                .replace("!", "\\!");
    }
}


