package org.example.infrastructure.telegram;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "telegram")
public class TelegramProperties {
    private String botToken;
    private String groupChatId;
    private String pmChatId;

    public String getBotToken() { return botToken; }
    public void setBotToken(String botToken) { this.botToken = botToken; }

    public String getGroupChatId() { return groupChatId; }
    public void setGroupChatId(String groupChatId) { this.groupChatId = groupChatId; }

    public String getPmChatId() { return pmChatId; }
    public void setPmChatId(String pmChatId) { this.pmChatId = pmChatId; }
}


