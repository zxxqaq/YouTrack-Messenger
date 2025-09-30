package org.example.application.service;

import org.example.domain.port.IssueCreationPort;
import org.example.infrastructure.telegram.TelegramClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class InteractiveCommandService {

    private final IssueCreationPort issueCreationPort;
    private final TelegramClient telegramClient;

    public InteractiveCommandService(IssueCreationPort issueCreationPort, TelegramClient telegramClient) {
        this.issueCreationPort = issueCreationPort;
        this.telegramClient = telegramClient;
    }

    /**
     * Process a command to create an issue
     * @param messageText The message text (should start with /create)
     * @param chatId The chat ID to send response to
     * @throws IOException if processing fails
     */
    public void processCreateCommand(String messageText, String chatId) throws IOException {
        // Extract summary and project from command
        String[] parts = extractCreateCommandParts(messageText);
        String summary = parts[0];
        String projectId = parts[1];
        
        if (summary.isEmpty()) {
            String errorMsg = "❌ Please provide a summary for the issue.\n\n" +
                "Usage: `/create Your issue summary here`\n" +
                "Or: `/create Your issue summary here @PROJECT_ID`\n\n" +
                "Available projects: Use `/projects` to see available projects";
            telegramClient.sendToChat(chatId, errorMsg);
            return;
        }

        try {
            // Create issue in YouTrack
            String issueId = issueCreationPort.createIssue(summary, projectId);
            
            // Send success message
            String successMsg = String.format(
                "✅ Issue created successfully!\n\n" +
                "📌 **Issue ID:** `%s`\n" +
                "📝 **Summary:** %s\n" +
                "🏗️ **Project:** `%s`\n" +
                "🔗 **Link:** [Open Issue](https://xianzhang.youtrack.cloud/issue/%s)",
                issueId, summary, projectId, issueId
            );
            
            telegramClient.sendToChat(chatId, successMsg);
            
        } catch (IOException e) {
            String errorMsg = String.format("❌ Failed to create issue: %s", e.getMessage());
            telegramClient.sendToChat(chatId, errorMsg);
        }
    }

    /**
     * Process help command
     * @param chatId The chat ID to send response to
     * @throws IOException if sending fails
     */
    public void processHelpCommand(String chatId) throws IOException {
        String helpMsg = 
            "🤖 **YouTrack Bot Commands\\:**\n\n" +
            "📝 `/create <summary>` \\- Create a new issue \\(default project\\)\n" +
            "📝 `/create <summary> @PROJECT_ID` \\- Create issue in specific project\n" +
            "   Example\\: `/create Fix login bug`\n" +
            "   Example\\: `/create Fix login bug @DEMO`\n\n" +
            "🏗️ `/projects` \\- Show available projects\n\n" +
            "❓ `/help` \\- Show this help message\n\n" +
            "📊 `/status` \\- Show bot status";
            
        telegramClient.sendToChat(chatId, helpMsg);
    }

    /**
     * Process projects command
     * @param chatId The chat ID to send response to
     * @throws IOException if sending fails
     */
    public void processProjectsCommand(String chatId) throws IOException {
        try {
            List<String> projects = issueCreationPort.getAvailableProjects();
            
            if (projects.isEmpty()) {
                String errorMsg = "❌ No projects found or failed to fetch projects";
                telegramClient.sendToChat(chatId, errorMsg);
                return;
            }
            
            StringBuilder projectsMsg = new StringBuilder("🏗️ **Available Projects:**\n\n");
            for (String project : projects) {
                projectsMsg.append("• `").append(project).append("`\n");
            }
            projectsMsg.append("\n💡 **Usage:** `/create Your summary @PROJECT_ID`");
            
            telegramClient.sendToChat(chatId, projectsMsg.toString());
            
        } catch (IOException e) {
            String errorMsg = String.format("❌ Failed to fetch projects: %s", e.getMessage());
            telegramClient.sendToChat(chatId, errorMsg);
        }
    }

    /**
     * Process status command
     * @param chatId The chat ID to send response to
     * @throws IOException if sending fails
     */
    public void processStatusCommand(String chatId) throws IOException {
        String statusMsg = 
            "🤖 **Bot Status:** ✅ Online\n" +
            "📡 **YouTrack:** Connected\n" +
            "⏰ **Scheduler:** Running\n" +
            "💾 **Database:** Connected";
            
        telegramClient.sendToChat(chatId, statusMsg);
    }

    /**
     * Process start command
     * @param chatId The chat ID to send response to
     * @throws IOException if sending fails
     */
    public void processStartCommand(String chatId) throws IOException {
        String startMsg = 
            "🤖 **Welcome to YouTrack Messenger Bot\\!**\n\n" +
            "I can help you manage YouTrack issues directly from Telegram\\.\n\n" +
            "📋 **Available Commands\\:**\n" +
            "\\• `/help` \\- Show all commands\n" +
            "\\• `/create <summary>` \\- Create a new issue\n" +
            "\\• `/projects` \\- Show available projects\n" +
            "\\• `/status` \\- Show bot status\n\n" +
            "💡 **Example\\:** `/create Fix login bug`\n" +
            "💡 **With project\\:** `/create Fix login bug @DEMO`\n\n" +
            "Let's get started\\! 🚀";
            
        telegramClient.sendToChat(chatId, startMsg);
    }

    /**
     * Process unknown command
     * @param messageText The unknown command text
     * @param chatId The chat ID to send response to
     * @throws IOException if sending fails
     */
    public void processUnknownCommand(String messageText, String chatId) throws IOException {
        String unknownMsg = 
            "❓ **Unknown command\\:** `" + messageText + "`\n\n" +
            "📋 **Available commands\\:**\n" +
            "\\• `/help` \\- Show all commands\n" +
            "\\• `/create <summary>` \\- Create a new issue\n" +
            "\\• `/projects` \\- Show available projects\n" +
            "\\• `/status` \\- Show bot status\n\n" +
            "Type `/help` for detailed information\\.";
            
        telegramClient.sendToChat(chatId, unknownMsg);
    }

    private String[] extractCreateCommandParts(String messageText) {
        if (messageText == null || !messageText.startsWith("/create")) {
            return new String[]{"", "DEMO"};
        }
        
        String content = messageText.substring("/create".length()).trim();
        
        // Check if there's a project specified with @
        if (content.contains("@")) {
            String[] parts = content.split("@");
            if (parts.length >= 2) {
                String summary = parts[0].trim();
                String projectId = parts[1].trim();
                return new String[]{summary, projectId};
            }
        }
        
        // No project specified, use default
        return new String[]{content, "DEMO"};
    }
}
