package org.example.domain.port;

import java.io.IOException;

public interface TelegramWebhookPort {
    /**
     * Process incoming Telegram message
     * @param messageText The message text
     * @param chatId The chat ID
     * @param userId The user ID
     * @throws IOException if processing fails
     */
    void processMessage(String messageText, String chatId, String userId) throws IOException;
}
