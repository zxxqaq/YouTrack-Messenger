package org.example.infrastructure.telegram;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.application.service.InteractiveCommandService;
import org.example.domain.port.TelegramWebhookPort;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TelegramWebhookHandler implements TelegramWebhookPort {

    private final InteractiveCommandService commandService;
    private final TelegramClient telegramClient;
    private final ObjectMapper om = new ObjectMapper();

    public TelegramWebhookHandler(InteractiveCommandService commandService, TelegramClient telegramClient) {
        this.commandService = commandService;
        this.telegramClient = telegramClient;
    }

    @Override
    public void processMessage(String messageText, String chatId, String userId) throws IOException {
        if (messageText == null || messageText.trim().isEmpty()) {
            return;
        }

        try {
            String command = messageText.trim().toLowerCase();

            if (command.startsWith("/create")) {
                commandService.processCreateCommand(messageText, chatId);
            } else if (command.equals("/projects")) {
                commandService.processProjectsCommand(chatId);
            } else if (command.equals("/status")) {
                commandService.processStatusCommand(chatId);
            } else if (command.equals("/start")) {
                commandService.processStartCommand(chatId);
            } else if (command.equals("/pull")) {
                commandService.processPullCommand(chatId);
            } else if (command.equals("/stop")) {
                commandService.processStopCommand(chatId);
            } else if (command.startsWith("/")) {
                // Unknown command
                commandService.processUnknownCommand(messageText, chatId);
            }
        } catch (Exception e) {
            // Send error message to user
            String errorMsg = "❌ *Error processing command\\:*\n\n" +
                "*Command\\:* `" + messageText + "`\n" +
                "*Error\\:* " + e.getMessage().replace("-", "\\-").replace(".", "\\.").replace("(", "\\(").replace(")", "\\)") + "\n\n" +
                "Please try again or use `/help` for available commands\\.";
            
            try {
                telegramClient.sendToChat(chatId, errorMsg);
            } catch (IOException sendError) {
                System.err.println("Failed to send error message to Telegram: " + sendError.getMessage());
                sendError.printStackTrace();
            }
            
            // Log the original error
            System.err.println("Error processing Telegram command: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Process incoming webhook payload
     * @param payload The webhook payload JSON
     * @throws IOException if processing fails
     */
    public void processWebhook(String payload) throws IOException {
        try {
            JsonNode root = om.readTree(payload);
            
            // Check if it's a message
            JsonNode message = root.path("message");
            if (message.isMissingNode()) {
                return;
            }

            String messageText = message.path("text").asText();
            String chatId = message.path("chat").path("id").asText();
            String userId = message.path("from").path("id").asText();

            // Only process text messages
            if (!messageText.isEmpty()) {
                processMessage(messageText, chatId, userId);
            }
        } catch (Exception e) {
            System.err.println("Error processing webhook payload: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to process webhook: " + e.getMessage(), e);
        }
    }
}
