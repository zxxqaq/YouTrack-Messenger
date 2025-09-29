package org.example;

public class TelegramClient {

    public void sendMarkdownV2(String text) {
        // HTTP POST 到 https://api.telegram.org/bot<token>/sendMessage
        // 参数：chat_id=..., text=<msg>, parse_mode=MarkdownV2, disable_web_page_preview可选
        // parse_mode 必须是 "MarkdownV2"
    }
}
