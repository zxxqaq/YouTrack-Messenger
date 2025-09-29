package org.example;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class YouTrackClientTest {

    @Test
    void realFetchFromYouTrack_shouldDecodeAndMapFields() throws Exception {
        Dotenv dotenv = Dotenv.load();
        String base = dotenv.get("YT_BASE_URL");
        String token = dotenv.get("YT_TOKEN");

        Assumptions.assumeTrue(base != null && !base.isBlank(), "skip: no YT_BASE_URL");
        Assumptions.assumeTrue(token != null && !token.isBlank(), "skip: no YT_TOKEN");


        YouTrackClient client = new YouTrackClient(base, token);
        List<YouTrackClient.Notification> list = client.fetchNotifications(20);

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
