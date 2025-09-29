package org.example.infrastructure.telegram;

import org.example.domain.port.MessengerPort;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class TelegramClient implements MessengerPort {
    private final TelegramProperties properties;

    public TelegramClient(TelegramProperties properties) {
        this.properties = properties;
    }

    @Override
    public void sendToGroup(String text) throws IOException {
        if (properties.getGroupChatId() == null) throw new IllegalStateException("telegram.group-chat-id is null");
        sendMarkdownV2(properties.getGroupChatId(), text);
    }

    @Override
    public void sendToPm(String text) throws IOException {
        if (properties.getPmChatId() == null) throw new IllegalStateException("telegram.pm-chat-id is null");
        sendMarkdownV2(properties.getPmChatId(), text);
    }

    private void sendMarkdownV2(String chatId, String text) throws IOException {
        String endpoint = "https://api.telegram.org/bot" + properties.getBotToken() + "/sendMessage";
        String body = formEncode(
                "chat_id", chatId,
                "text", text,
                "disable_web_page_preview", "true",
                "allow_sending_without_reply", "true"
        );
        httpPost(endpoint, body, "application/x-www-form-urlencoded");
    }

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


