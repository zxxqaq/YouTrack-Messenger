package org.example.domain.port;

import org.example.domain.view.NotificationView;

import java.io.IOException;
import java.util.List;

public interface IssueTrackerPort {
    List<NotificationView> fetchNotifications(int top) throws IOException;
}


