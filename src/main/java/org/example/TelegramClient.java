package org.example;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class TelegramClient {
    private final String token;
    private final String groupChatId; // e.g., -4908402163
    private final String pmChatId;    // e.g., 8003925771

    public TelegramClient(String token, String groupChatId, String pmChatId) {
        this.token = token;
        this.groupChatId = groupChatId;
        this.pmChatId = pmChatId;
    }

    /** Send MarkdownV2 message to the configured group chat */
    public void sendToGroup(String text) throws IOException {
        if (groupChatId == null) throw new IllegalStateException("TG_GROUP_CHAT_ID is null");
        sendMarkdownV2(groupChatId, text);
    }

    /** Send MarkdownV2 message to the configured PM chat */
    public void sendToPm(String text) throws IOException {
        if (pmChatId == null) throw new IllegalStateException("TG_PM_CHAT_ID is null");
        sendMarkdownV2(pmChatId, text);
    }

    /** Send MarkdownV2 message to a specific chat id */
    public void sendMarkdownV2(String chatId, String text) throws IOException {
        String endpoint = "https://api.telegram.org/bot" + token + "/sendMessage";
        String body = formEncode(
                "chat_id", chatId,
                "text", text,
                "parse_mode", "MarkdownV2",
                "disable_web_page_preview", "true",
                "allow_sending_without_reply", "true"
        );
        httpPost(endpoint, body, "application/x-www-form-urlencoded");
    }

    // ---------- internals ----------

    private static String formEncode(String... kv) {
        if (kv.length % 2 != 0) throw new IllegalArgumentException("key/value pairs required");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < kv.length; i += 2) {
            if (i > 0) sb.append('&');
            sb.append(urlEncode(kv[i])).append('=').append(urlEncode(kv[i + 1]));
        }
        return sb.toString();
    }

    private static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            // should not happen
            return "";
        }
    }

    private static void httpPost(String endpoint, String body, String contentType) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(15_000);
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", contentType);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        if (code >= 200 && code < 300) {
            try (InputStream is = conn.getInputStream()) { drain(is); }
        } else {
            String err = readStream(conn.getErrorStream());
            throw new IOException("Telegram API error: HTTP " + code + " " + err);
        }
    }

    private static void drain(InputStream is) throws IOException {
        if (is == null) return;
        byte[] buf = new byte[1024];
        while (is.read(buf) != -1) { /* discard */ }
    }

    private static String readStream(InputStream is) throws IOException {
        if (is == null) return "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line; while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }
}
