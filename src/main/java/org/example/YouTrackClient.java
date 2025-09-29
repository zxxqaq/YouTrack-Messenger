package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.io.ByteArrayInputStream;

public class YouTrackClient {
    private final String base;
    private final String token;
    private final OkHttpClient http = new OkHttpClient();
    private final ObjectMapper om = new ObjectMapper();

    // TODO: permanent storage
    private final Set<String> sentIds = ConcurrentHashMap.newKeySet();

    public YouTrackClient(String baseUrl, String token) {
        this.base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length()-1) : baseUrl;
        this.token = token;
    }
    // TODO: filter the already sent notifications

    // TODO: timeout/retry
    // TODO: pagination, maximum top value
    public List<Notification> fetchNotifications(int top) throws IOException {
        HttpUrl url = HttpUrl.parse(base + "/api/users/notifications")
                .newBuilder()
                .addQueryParameter("fields", "id,content,metadata,read,updated")
                .addQueryParameter("\u0024top", String.valueOf(top))
                .build();

        Request req = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + token)
                .build();

        try (Response resp = http.newCall(req).execute()) {
            if (!resp.isSuccessful()) {
                throw new IOException("YouTrack " + resp.code() + ": " + resp.message());
            }
            JsonNode arr = om.readTree(resp.body().byteStream());
            List<Notification> list = new ArrayList<>();
            if (arr.isArray()) {
                for (JsonNode n : arr) {
                    Notification x = new Notification();
                    x.id = n.path("id").asText();
                    x.content = decodeIfGzipBase64(n.path("content").asText(""));

                    // ---- decode & parse metadata ----
                    String metadataRaw = decodeIfGzipBase64(n.path("metadata").asText(""));
                    JsonNode metadata = om.readTree(metadataRaw);

                    JsonNode issue   = metadata.path("issue");
                    JsonNode fields  = issue.path("fields");
                    JsonNode change  = metadata.path("change");
                    JsonNode reason  = metadata.path("reason");

                    String issueId   = issue.path("id").asText("");
                    String summary   = issue.path("summary").asText("");
                    String state     = fieldValueByName(fields, "State");
                    String assignee  = fieldValueByName(fields, "Assignee");
                    String priority  = fieldValueByName(fields, "Priority");
                    String header    = metadata.path("header").asText("");

                    // tags: prefer change.events; fallback to reason.tagReasons
                    List<String> tags = tagsAdded(change);
                    if (tags.isEmpty()) {
                        reason.path("tagReasons").forEach(t -> tags.add(t.path("name").asText("")));
                    }

                    // comment text if present (for comment/mention notifications)
                    String comment = commentText(change);

                    // ---- assign back to notification ----
                    x.title   = summary;          // show issue.summary as title
                    x.status  = state;            // show State field as status
                    x.read    = n.path("read").asBoolean(false);
                    x.updated = "";               // skip for now

                    x.issueId  = issueId;
                    x.assignee = assignee;
                    x.priority = priority;
                    x.header   = header;
                    x.tags     = tags;
                    x.comment  = comment;
                    x.link     = base + "/issue/" + issueId;

                    list.add(x);
                }
            }
            return list;
        }
    }

    public static class Notification {
        public String id, title, content, status, updated;
        public boolean read;

        public String issueId;     // DEMO-3
        public String assignee;    // admin
        public String priority;    // Normal/Major/...
        public String header;      // Assigned / Commented / ...
        public String comment;     // comment text if any
        public String link;        // direct link to issue
        public List<String> tags;  // eg ["Star"]
    }

    // -------- helpers --------

    // get value name from fields array by name
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

    // get tag names added in change.events
    private static List<String> tagsAdded(JsonNode change) {
        List<String> out = new ArrayList<>();
        change.path("events").forEach(ev -> {
            if ("TAGS".equals(ev.path("category").asText())) {
                ev.path("addedValues").forEach(v -> out.add(v.path("name").asText("")));
            }
        });
        return out;
    }

    // get comment text from change.events if any
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

    // decode gzip + base64
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
            return input; // fallback to original
        }
    }
}
