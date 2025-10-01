package org.example.infrastructure.youtrack;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.example.domain.port.IssueTrackerPort;
import org.example.domain.view.NotificationView;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPInputStream;

@Component
public class YouTrackClient implements IssueTrackerPort {

    private final YouTrackProperties properties;
    private final OkHttpClient http = new OkHttpClient();
    private final ObjectMapper om = new ObjectMapper();

    public YouTrackClient(YouTrackProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<NotificationView> fetchNotifications(int top) throws IOException {
        return fetchNotificationsFromTimestamp(null, top);
    }

    @Override
    public List<NotificationView> fetchNotificationsFromTimestamp(String timestampCursor, int top) throws IOException {
        String base = normalizeBase(properties.getBaseUrl());
        HttpUrl.Builder urlBuilder = HttpUrl.parse(base + "/api/users/notifications")
                .newBuilder()
                .addQueryParameter("fields", "id,content,metadata")
                .addQueryParameter("all", "true");  // Get notifications for all users (requires admin permissions)
        
        // Add top limit if provided
        if (top > 0) {
            urlBuilder.addQueryParameter("$top", String.valueOf(top));
        }
        
        HttpUrl url = urlBuilder.build();

        Request req = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + properties.getToken())
                .build();

        try (Response resp = http.newCall(req).execute()) {
            if (!resp.isSuccessful()) {
                throw new IOException("YouTrack " + resp.code() + ": " + resp.message());
            }
            JsonNode arr = om.readTree(resp.body().byteStream());
            List<NotificationView> list = new ArrayList<>();
            if (arr.isArray()) {
                for (JsonNode n : arr) {
                    NotificationView x = new NotificationView();
                    x.id = n.path("id").asText();
                    x.content = decodeIfGzipBase64(n.path("content").asText(""));

                    String metadataRaw = decodeIfGzipBase64(n.path("metadata").asText(""));
                    JsonNode metadata = om.readTree(metadataRaw);

                    JsonNode issue = metadata.path("issue");
                    JsonNode fields = issue.path("fields");
                    JsonNode change = metadata.path("change");
                    JsonNode reason = metadata.path("reason");

                    String issueId = issue.path("id").asText("");
                    String summary = issue.path("summary").asText("");
                    String state = fieldValueByName(fields, "State");
                    String assignee = fieldValueByName(fields, "Assignee");
                    String priority = fieldValueByName(fields, "Priority");
                    String header = metadata.path("header").asText("");

                    List<String> tags = tagsAdded(change);
                    if (tags.isEmpty()) {
                        reason.path("tagReasons").forEach(t -> tags.add(t.path("name").asText("")));
                    }

                    String comment = commentText(change);

                    x.title = summary;
                    x.status = state;
                    x.read = n.path("read").asBoolean(false);
                    x.updated = n.path("updated").asText(""); // Get updated timestamp from API
                    x.issueId = issueId;
                    x.assignee = assignee;
                    x.priority = priority;
                    x.header = header;
                    x.tags = tags;
                    x.comment = comment;
                    x.link = base + "/issue/" + issueId;

                    list.add(x);
                }
            }
            return list;
        }
    }

    private static String normalizeBase(String baseUrl) {
        if (baseUrl == null) return "";
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private static String fieldValueByName(JsonNode fields, String name) {
        if (fields != null && fields.isArray()) {
            for (JsonNode f : fields) {
                if (name.equalsIgnoreCase(f.path("name").asText())) {
                    return f.path("value").asText("");
                }
            }
        }
        return "";
    }

    private static List<String> tagsAdded(JsonNode change) {
        List<String> out = new ArrayList<>();
        change.path("events").forEach(ev -> {
            if ("TAGS".equals(ev.path("category").asText())) {
                ev.path("addedValues").forEach(v -> out.add(v.path("name").asText("")));
            }
        });
        return out;
    }

    private static String commentText(JsonNode change) {
        for (JsonNode ev : change.path("events")) {
            if ("COMMENT".equals(ev.path("category").asText())) {
                JsonNode arr = ev.path("addedValues");
                if (arr.isArray() && arr.size() > 0) {
                    return arr.get(0).path("name").asText("");
                }
            }
        }
        return "";
    }

    private static String decodeIfGzipBase64(String input) {
        if (input == null || input.isBlank()) return "";
        try {
            byte[] decoded = Base64.getDecoder().decode(input);
            if (decoded.length >= 2 && decoded[0] == (byte) 0x1f && decoded[1] == (byte) 0x8b) {
                try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(decoded))) {
                    return new String(gis.readAllBytes(), StandardCharsets.UTF_8);
                }
            } else {
                return new String(decoded, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            return input;
        }
    }
}


