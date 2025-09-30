package org.example.interfaces.rest;

import org.example.infrastructure.telegram.TelegramWebhookHandler;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/telegram")
public class TelegramWebhookController {

    private final TelegramWebhookHandler webhookHandler;

    public TelegramWebhookController(TelegramWebhookHandler webhookHandler) {
        this.webhookHandler = webhookHandler;
    }

    @PostMapping("/webhook")
    public void webhook(@RequestBody String payload) {
        try {
            webhookHandler.processWebhook(payload);
        } catch (Exception e) {
            System.err.println("Error processing webhook: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @GetMapping("/webhook")
    public String webhookGet() {
        return "Telegram webhook endpoint is active";
    }

    @PostMapping("/test")
    public String testWebhook(@RequestBody String payload) {
        try {
            webhookHandler.processWebhook(payload);
            return "Webhook processed successfully";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
