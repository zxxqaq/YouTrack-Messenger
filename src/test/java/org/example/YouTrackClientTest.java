package org.example;

import org.example.infrastructure.youtrack.YouTrackClient;
import org.example.infrastructure.youtrack.YouTrackProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class YouTrackClientTest {

    @Test
    void realFetchFromYouTrack_shouldDecodeAndMapFields() throws Exception {
        // 使用环境变量或默认值
        String base = System.getenv("YT_BASE_URL");
        String token = System.getenv("YT_TOKEN");
        
        // 如果环境变量不存在，使用默认值（用于测试）
        if (base == null) base = "https://xianzhang.youtrack.cloud";
        if (token == null) token = "perm-YWRtaW4=.NDQtMA==.FcKqV8V4PBbzDicI3WRnw0NOG7boGd";

        Assumptions.assumeTrue(base != null && !base.isBlank(), "skip: no YT_BASE_URL");
        Assumptions.assumeTrue(token != null && !token.isBlank(), "skip: no YT_TOKEN");

        YouTrackProperties properties = new YouTrackProperties();
        properties.setBaseUrl(base);
        properties.setToken(token);
        
        YouTrackClient client = new YouTrackClient(properties);
        List<org.example.domain.view.NotificationView> list = client.fetchNotifications(20);

        System.out.println("fetched notifications num: " + list.size());
        for (var n : list) {
            System.out.println(n.issueId + " " + n.title);
            if (n.comment != null && !n.comment.isBlank()) {
                System.out.println(n.comment); // comment may be null or empty
            }

            // if tags is not null and contains "Star"
            boolean hasStar = n.tags != null && n.tags.stream().anyMatch("Star"::equalsIgnoreCase);
            if (hasStar) System.out.println("star: ⭐");

            if (n.assignee != null && !n.assignee.isBlank()) {
                System.out.println("Assignee: " + n.assignee);
            }
            System.out.println("Link: " + n.link);
            System.out.println("----");
        }

        for (var n : list) {
            assertNotNull(n.id);
            assertNotNull(n.title);
            assertNotNull(n.content);
            assertNotNull(n.status);
            assertNotNull(n.issueId);
            assertNotNull(n.link);
            assertNotNull(n.read);
        }

    }
}
