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
    private final ObjectMapper om = new ObjectMapper();

    public TelegramWebhookHandler(InteractiveCommandService commandService) {
        this.commandService = commandService;
    }

    @Override
    public void processMessage(String messageText, String chatId, String userId) throws IOException {
        if (messageText == null || messageText.trim().isEmpty()) {
            return;
        }

        String command = messageText.trim().toLowerCase();

        if (command.startsWith("/create")) {
            commandService.processCreateCommand(messageText, chatId);
        } else if (command.equals("/projects")) {
            commandService.processProjectsCommand(chatId);
        } else if (command.equals("/help")) {
            commandService.processHelpCommand(chatId);
        } else if (command.equals("/status")) {
            commandService.processStatusCommand(chatId);
        } else if (command.equals("/start")) {
            commandService.processStartCommand(chatId);
        } else if (command.startsWith("/")) {
            // Unknown command
            commandService.processUnknownCommand(messageText, chatId);
        }
    }

    /**
     * Process incoming webhook payload
     * @param payload The webhook payload JSON
     * @throws IOException if processing fails
     */
    public void processWebhook(String payload) throws IOException {
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
    }
}
