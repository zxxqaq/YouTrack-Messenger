package org.example;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class YouTrackClientTest {

    @Test
    void realFetchFromYouTrack() throws Exception {
        // Load .env file
        Dotenv dotenv = Dotenv.load();

        String base = dotenv.get("YT_BASE_URL");
        String token = dotenv.get("YT_TOKEN");

        assertNotNull(base, "YT_BASE_URL must be set in .env");
        assertNotNull(token, "YT_TOKEN must be set in .env");

        YouTrackClient client = new YouTrackClient(base, token);
        List<YouTrackClient.Notification> list = client.fetchNotifications(10);

        System.out.println("fetched notifications num: " + list.size());
        for (var n : list) {
            System.out.println("ID: " + n.id);
            System.out.println("Title: " + n.title);
            System.out.println("Status: " + n.status);
            System.out.println("Content: " + n.content);
            System.out.println("Updated: " + n.updated);
            System.out.println("Read: " + n.read);
            System.out.println("----");
        }
    }
}
