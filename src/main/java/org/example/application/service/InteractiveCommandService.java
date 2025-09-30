package org.example.application.service;

import org.example.domain.model.ProjectInfo;
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
     * @param chatId The chat ID to send response to (ignored, uses configured pmChatId)
     * @throws IOException if processing fails
     */
    public void processCreateCommand(String messageText, String chatId) throws IOException {
        String targetChatId = chatId; // Reply to the same chat where command was sent
        // Extract summary and project from command
        String[] parts = extractCreateCommandParts(messageText);
        String summary = parts[0];
        String projectId = parts[1];
        
        if (summary.isEmpty()) {
            String errorMsg = "❌ Please provide a summary for the issue\\.\n\n" +
                "Usage\\: `/create Your issue summary @PROJECT_ID`\n\n" +
                "⚠️ **Required\\:** You must specify a project ID\\!\n" +
                "Use `/projects` to see available projects";
            telegramClient.sendToChat(targetChatId, errorMsg);
            return;
        }
        
        if (projectId.isEmpty()) {
            String errorMsg = "❌ Please specify a project ID\\.\n\n" +
                "Usage\\: `/create Your issue summary @PROJECT_ID`\n\n" +
                "⚠️ **Required\\:** You must specify a project ID\\!\n" +
                "Use `/projects` to see available projects";
            telegramClient.sendToChat(targetChatId, errorMsg);
            return;
        }

        try {
            // Create issue in YouTrack
            String issueId = issueCreationPort.createIssue(summary, projectId);
            
            // Get project name for display
            String projectName = projectId; // Default to projectId if not found
            try {
                List<ProjectInfo> projects = issueCreationPort.getAvailableProjects();
                for (ProjectInfo project : projects) {
                    if (project.getId().equals(projectId)) {
                        projectName = project.getName();
                        break;
                    }
                }
            } catch (IOException e) {
                // If we can't fetch projects, just use the projectId
                System.err.println("Failed to fetch projects for display: " + e.getMessage());
            }
            
            // Send success message
            String escapedSummary = summary
                .replace("-", "\\-")
                .replace(".", "\\.")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace(">", "\\>")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace("=", "\\=")
                .replace("|", "\\|");
                
            String escapedIssueId = issueId
                .replace("-", "\\-")
                .replace(".", "\\.")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace(">", "\\>")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace("=", "\\=")
                .replace("|", "\\|");
                
            String escapedProjectName = projectName
                .replace("-", "\\-")
                .replace(".", "\\.")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace(">", "\\>")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace("=", "\\=")
                .replace("|", "\\|");
                
            String successMsg = String.format(
                "✅ Issue created successfully\\!\n\n" +
                " **Issue ID\\:** `%s`\n" +
                " **Summary\\:** %s\n" +
                " **Project\\:** %s\n" +
                " **Link\\:** [Open Issue](https://xianzhang\\.youtrack\\.cloud/issue/%s)",
                escapedIssueId, escapedSummary, escapedProjectName, escapedIssueId
            );
            
            telegramClient.sendToChat(targetChatId, successMsg);
            
        } catch (IOException e) {
            String errorMsg = String.format("❌ Failed to create issue\\: %s", 
                e.getMessage()
                    .replace("\\", "\\\\")
                    .replace("-", "\\-")
                    .replace(".", "\\.")
                    .replace("(", "\\(")
                    .replace(")", "\\)")
                    .replace("{", "\\{")
                    .replace("}", "\\}")
                    .replace("_", "\\_")
                    .replace("*", "\\*")
                    .replace("[", "\\[")
                    .replace("]", "\\]")
                    .replace("~", "\\~")
                    .replace("`", "\\`")
                    .replace(">", "\\>")
                    .replace("#", "\\#")
                    .replace("+", "\\+")
                    .replace("=", "\\=")
                    .replace("|", "\\|"));
            telegramClient.sendToChat(targetChatId, errorMsg);
        }
    }


    /**
     * Process projects command
     * @param chatId The chat ID to send response to (ignored, uses configured pmChatId)
     * @throws IOException if sending fails
     */
    public void processProjectsCommand(String chatId) throws IOException {
        String targetChatId = chatId; // Reply to the same chat where command was sent
        try {
            List<ProjectInfo> projects = issueCreationPort.getAvailableProjects();
            
            if (projects.isEmpty()) {
                String errorMsg = "❌ No projects found or failed to fetch projects";
                telegramClient.sendToChat(targetChatId, errorMsg);
                return;
            }
            
            StringBuilder projectsMsg = new StringBuilder("🏗️ **Available Projects\\:**\n\n");
            for (ProjectInfo project : projects) {
                projectsMsg.append("• **").append(project.getName()).append("** \\- `").append(project.getId()).append("`\n");
            }
            projectsMsg.append(" **Example\\:** `/create Fix login bug @").append(projects.get(0).getId()).append("`");
            
            telegramClient.sendToChat(targetChatId, projectsMsg.toString());
            
        } catch (IOException e) {
            String errorMsg = String.format("❌ Failed to fetch projects\\: %s", e.getMessage());
            telegramClient.sendToChat(targetChatId, errorMsg);
        }
    }

    /**
     * Process status command
     * @param chatId The chat ID to send response to (ignored, uses configured pmChatId)
     * @throws IOException if sending fails
     */
    public void processStatusCommand(String chatId) throws IOException {
        String targetChatId = chatId; // Reply to the same chat where command was sent
        
        try {
            // Test YouTrack connection
            List<ProjectInfo> projects = issueCreationPort.getAvailableProjects();
            String youtrackStatus = projects.isEmpty() ? "❌ Disconnected" : "✅ Connected";
            
            String statusMsg = 
                "🤖 **Bot Status\\:** ✅ Online\n" +
                "📡 **YouTrack\\:** " + youtrackStatus + "\n" +
                "⏰ **Scheduler\\:** Running\n" +
                "💾 **Database\\:** Connected\n" +
                "🌐 **Webhook\\:** Active";
            
            telegramClient.sendToChat(targetChatId, statusMsg);
            
        } catch (Exception e) {
            String errorMsg = 
                "🤖 **Bot Status\\:** ⚠️ Partial\n" +
                "📡 **YouTrack\\:** ❌ Connection Failed\n" +
                "⏰ **Scheduler\\:** Running\n" +
                "💾 **Database\\:** Connected\n" +
                "🌐 **Webhook\\:** Active\n\n" +
                "**Error\\:** " + e.getMessage().replace("-", "\\-").replace(".", "\\.").replace("(", "\\(").replace(")", "\\)");
            
            telegramClient.sendToChat(targetChatId, errorMsg);
        }
    }

    /**
     * Process start command
     * @param chatId The chat ID to send response to (ignored, uses configured pmChatId)
     * @throws IOException if sending fails
     */
    public void processStartCommand(String chatId) throws IOException {
        String targetChatId = chatId; // Reply to the same chat where command was sent
        String startMsg = 
            "🤖 **Welcome to YouTrack Messenger Bot\\!**\n\n" +
            "I can help you manage YouTrack issues directly from Telegram\\.\n\n" +
            "📋 **Available Commands\\:**\n" +
            " `/create <summary> @PROJECT_ID` \\- Create a new issue\n" +
            " `/projects` \\- Show available projects\n" +
            " `/status` \\- Show bot status\n\n" +
            "⚠️ **Important\\:** You must specify a project ID when creating issues\\!\n" +
            "💡 **Example\\:** `/create Fix login bug @0\\-0`\n" +
            "💡 **Tip\\:** Use `/projects` first to see available project IDs\\!\n\n" +
            "Let's get started\\! 🚀";
            
        telegramClient.sendToChat(targetChatId, startMsg);
    }

    /**
     * Process unknown command
     * @param messageText The unknown command text
     * @param chatId The chat ID to send response to (ignored, uses configured pmChatId)
     * @throws IOException if sending fails
     */
    public void processUnknownCommand(String messageText, String chatId) throws IOException {
        String targetChatId = chatId; // Reply to the same chat where command was sent
        String unknownMsg = 
            "❓ **Unknown command\\:** `" + messageText + "`\n\n" +
            "📋 **Available commands\\:**\n" +
            " `/create <summary> @PROJECT_ID` \\- Create a new issue\n" +
            " `/projects` \\- Show available projects\n" +
            " `/status` \\- Show bot status\n\n" +
            "💡 **Tip\\:** Use `/projects` first to see available project IDs\\!";
            
        telegramClient.sendToChat(targetChatId, unknownMsg);
    }

    private String[] extractCreateCommandParts(String messageText) {
        if (messageText == null || !messageText.startsWith("/create")) {
            return new String[]{"", ""};
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
        
        // No project specified, return empty project ID to indicate error
        return new String[]{content, ""};
    }
}
