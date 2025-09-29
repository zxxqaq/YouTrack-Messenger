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
                    String metadataRaw = decodeIfGzipBase64(n.path("metadata").asText(""));
                    JsonNode metadata = om.readTree(metadataRaw);
                    x.title = metadata.path("title").asText("");
                    x.status = metadata.path("status").asText("");
//                    x.title = n.path("metadata").path("title").asText("");
//                    x.status = n.path("metadata").path("status").asText("");
                    x.read = n.path("read").asBoolean(false);
                    x.updated = n.path("updated").asText("");
                    list.add(x);
                }
            }
            return list;
        }
    }

    public static class Notification {
        public String id, title, content, status, updated;
        public boolean read;
    }

    // decode gzip + base 64
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
