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
    private final SystemHealthService healthService;
    private final NotificationScheduler notificationScheduler;
    private final org.example.domain.port.NotificationStoragePort storagePort;

    public InteractiveCommandService(IssueCreationPort issueCreationPort, 
                                    TelegramClient telegramClient,
                                    SystemHealthService healthService,
                                    NotificationScheduler notificationScheduler,
                                    org.example.domain.port.NotificationStoragePort storagePort) {
        this.issueCreationPort = issueCreationPort;
        this.telegramClient = telegramClient;
        this.healthService = healthService;
        this.notificationScheduler = notificationScheduler;
        this.storagePort = storagePort;
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
                "⚠️ *Required\\:* You must specify a project ID\\!\n" +
                "Use `/projects` to see available projects";
            telegramClient.sendToChat(targetChatId, errorMsg);
            return;
        }
        
        if (projectId.isEmpty()) {
            String errorMsg = "❌ Please specify a project ID\\.\n\n" +
                "Usage\\: `/create Your issue summary @PROJECT_ID`\n\n" +
                "⚠️ *Required\\:* You must specify a project ID\\!\n" +
                "Use `/projects` to see available projects";
            telegramClient.sendToChat(targetChatId, errorMsg);
            return;
        }

        try {
            // Validate project ID exists
            List<ProjectInfo> projects = issueCreationPort.getAvailableProjects();
            boolean projectExists = projects.stream().anyMatch(p -> p.getId().equals(projectId));
            
            if (!projectExists) {
                StringBuilder errorMsg = new StringBuilder();
                errorMsg.append("❌ *Invalid Project ID\\:* `").append(escapeMarkdownV2(projectId)).append("`\n\n");
                errorMsg.append("This project does not exist or you don\\'t have access to it\\.\n\n");
                errorMsg.append("💡 *Available projects\\:*\n");
                
                for (ProjectInfo p : projects) {
                    errorMsg.append("• *").append(p.getName()).append("* \\- `").append(p.getId()).append("`\n");
                }
        
                
                telegramClient.sendToChat(targetChatId, errorMsg.toString());
                return;
            }
            
            // Create issue in YouTrack
            String issueId = issueCreationPort.createIssue(summary, projectId);
            
            // Get project name for display (reuse the projects list we already fetched)
            String projectName = projectId; // Default to projectId if not found
            for (ProjectInfo project : projects) {
                if (project.getId().equals(projectId)) {
                    projectName = project.getName();
                    break;
                }
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
                " *Issue ID\\:* `%s`\n" +
                " *Summary\\:* %s\n" +
                " *Project\\:* %s\n" +
                " *Link\\:* [Open Issue](https://xianzhang\\.youtrack\\.cloud/issue/%s)",
                escapedIssueId, escapedSummary, escapedProjectName, escapedIssueId
            );
            
            telegramClient.sendToChat(targetChatId, successMsg);
            
        } catch (IOException e) {
            // Log detailed error to console/logs
            System.err.println("[InteractiveCommand] Failed to create issue: " + e.getMessage());
            e.printStackTrace();
            
            // Determine error type and create user-friendly message
            String errorType = determineErrorType(e);
            String escapedErrorType = escapeMarkdownV2(errorType);
            String escapedErrorMessage = escapeMarkdownV2(e.getMessage());
            
            String errorMsg = String.format(
                "❌ *Failed to create issue*\n\n" +
                "🔍 *Error Type\\:* %s\n" +
                "📝 *Details\\:* %s\n\n" +
                "💡 *Suggestions\\:*\n" +
                " \\- Check if YouTrack is accessible\n" +
                " \\- Verify your project ID with `/projects`\n" +
                " \\- Try again in a few moments\n" +
                " \\- Use `/status` to check system health",
                escapedErrorType, escapedErrorMessage
            );
            
            try {
                telegramClient.sendToChat(targetChatId, errorMsg);
            } catch (IOException sendError) {
                System.err.println("[InteractiveCommand] Failed to send error message to Telegram: " + sendError.getMessage());
            }
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
            
            StringBuilder projectsMsg = new StringBuilder("🏗️ *Available Projects\\:*\n\n");
            for (ProjectInfo project : projects) {
                projectsMsg.append("• *").append(project.getName()).append("* \\- `").append(project.getId()).append("`\n");
            }

            telegramClient.sendToChat(targetChatId, projectsMsg.toString());
            
        } catch (IOException e) {
            // Log detailed error to console/logs
            System.err.println("[InteractiveCommand] Failed to fetch projects: " + e.getMessage());
            e.printStackTrace();
            
            String errorType = determineErrorType(e);
            String escapedErrorType = escapeMarkdownV2(errorType);
            String escapedErrorMessage = escapeMarkdownV2(e.getMessage());
            
            String errorMsg = String.format(
                "❌ *Failed to fetch projects*\n\n" +
                "🔍 *Error Type\\:* %s\n" +
                "📝 *Details\\:* %s\n\n" +
                "💡 *Suggestions\\:*\n" +
                " \\- Check if YouTrack is accessible\n" +
                " \\- Try again in a few moments\n" +
                " \\- Use `/status` to check system health",
                escapedErrorType, escapedErrorMessage
            );
            
            try {
                telegramClient.sendToChat(targetChatId, errorMsg);
            } catch (IOException sendError) {
                System.err.println("[InteractiveCommand] Failed to send error message to Telegram: " + sendError.getMessage());
            }
        }
    }

    /**
     * Process status command - displays comprehensive system health
     * @param chatId The chat ID to send response to
     * @throws IOException if sending fails
     */
    public void processStatusCommand(String chatId) throws IOException {
        String targetChatId = chatId;
        StringBuilder statusMsg = new StringBuilder();
        
        // Header
        statusMsg.append("🔍 *System Health Status*\n\n");
        
        // Bot Status
        statusMsg.append("🤖 *Bot\\:* ✅ Online\n");
        statusMsg.append("🌐 *Webhook\\:* ✅ Active\n");
        
        // Polling Status
        boolean isPolling = notificationScheduler.isRunning();
        String pollingStatus = isPolling ? "🟢 Running" : "⏸️ Paused";
        statusMsg.append("📡 *Polling\\:* ").append(pollingStatus);
        if (!isPolling) {
            statusMsg.append(" \\(Use `/pull` to start\\)");
        }
        statusMsg.append("\n");
        
        // Scheduler Status (health)
        String schedulerStatus = healthService.getSchedulerStatus();
        statusMsg.append("⏰ *Scheduler Health\\:* ").append(schedulerStatus).append("\n");
        
        // Last successful run
        // String lastSuccess = escapeMarkdownV2(healthService.getLastSuccessTime());
        // statusMsg.append(" └─ Last Success\\: `").append(lastSuccess).append("`\n");
        
        // If there are recent failures, show details
        // if (healthService.hasRecentFailures()) {
        //     String lastFailure = escapeMarkdownV2(healthService.getLastFailureTime());
        //     String errorType = escapeMarkdownV2(healthService.getLastErrorType());
        //     String errorMsg = escapeMarkdownV2(healthService.getLastErrorMessage());
            
        //     statusMsg.append(" └─ Last Failure\\: `").append(lastFailure).append("`\n");
        //     statusMsg.append(" └─ Error Type\\: ").append(errorType).append("\n");
        //     statusMsg.append(" └─ Error\\: ").append(errorMsg != null && errorMsg.length() > 100 ? errorMsg.substring(0, 100) + "\\.\\.\\." : errorMsg).append("\n");
        // }
        
        // statusMsg.append("\n");
        
        // Test YouTrack connection
        try {
            List<ProjectInfo> projects = issueCreationPort.getAvailableProjects();
            if (projects.isEmpty()) {
                statusMsg.append("📡 *YouTrack\\:* ⚠️ Connected \\(No projects found\\)\n");
            } else {
                statusMsg.append("📡 *YouTrack\\:* ✅ Connected\n");
                statusMsg.append(" └─ Projects\\: ").append(projects.size()).append(" available\n");
            }
        } catch (Exception e) {
            statusMsg.append("📡 *YouTrack\\:* ❌ Connection Failed\n");
            // String errorMsg = escapeMarkdownV2(e.getMessage());
            // statusMsg.append(" └─ Error\\: ").append(errorMsg != null && errorMsg.length() > 80 ? errorMsg.substring(0, 80) + "\\.\\.\\." : errorMsg).append("\n");
        }
        
        statusMsg.append("\n");
        
        // Test Database connection
        try {
            // Try to access database by getting all sent IDs
            storagePort.getAllSentIds();
            statusMsg.append("💾 *Database\\:* ✅ Connected\n");
        } catch (Exception e) {
            statusMsg.append("💾 *Database\\:* ❌ Connection Failed\n");
            String errorMsg = escapeMarkdownV2(e.getMessage());
            statusMsg.append(" └─ Error\\: ").append(errorMsg != null && errorMsg.length() > 80 ? errorMsg.substring(0, 80) + "\\.\\.\\." : errorMsg).append("\n");
        }
        
        
        try {
            telegramClient.sendToChat(targetChatId, statusMsg.toString());
        } catch (IOException sendError) {
            System.err.println("[InteractiveCommand] Failed to send status message: " + sendError.getMessage());
            throw sendError;
        }
    }

    /**
     * Process start command
     * @param chatId The chat ID to send response to (ignored, uses configured pmChatId)
     * @throws IOException if sending fails
     */
    public void processStartCommand(String chatId) throws IOException {
        String targetChatId = chatId; // Reply to the same chat where command was sent
        
        boolean isPolling = notificationScheduler.isRunning();
        String pollingStatus = isPolling ? "🟢 Running" : "⏸️ Paused";
        
        String startMsg = 
            "🤖 *Welcome to YouTrack Messenger Bot\\!*\n\n" +
            "I can help you manage YouTrack issues directly from Telegram\\.\n\n" +
            "📋 *Available Commands\\:*\n\n" +
            "*Issue Management\\:*\n" +
            " `/create <summary> @PROJECT_ID` \\- Create a new issue\n" +
            " `/projects` \\- Show available projects\n\n" +
            "*Notification Control\\:*\n" +
            " `/pull` \\- Start notification polling\n" +
            " `/stop` \\- Stop notification polling\n" +
            " `/status` \\- Show bot status\n\n" +
            "📊 *Current Polling Status\\:* " + pollingStatus + "\n\n" +
            "Let's get started\\! 🚀";
            
        telegramClient.sendToChat(targetChatId, startMsg);
    }

    /**
     * Process pull command - start the notification scheduler
     * @param chatId The chat ID to send response to
     * @throws IOException if sending fails
     */
    public void processPullCommand(String chatId) throws IOException {
        String targetChatId = chatId;
        
        if (notificationScheduler.isRunning()) {
            String msg = "⚠️ *Notification polling is already running\\!*\n\n" +
                "The bot is actively pulling YouTrack notifications\\.\n" +
                "Use `/stop` to pause polling\\.";
            telegramClient.sendToChat(targetChatId, msg);
        } else {
            notificationScheduler.start();
            String msg = "✅ *Notification polling started\\!*\n\n" +
                "The bot will now automatically pull YouTrack notifications\\." ;

            telegramClient.sendToChat(targetChatId, msg);
        }
    }
    
    /**
     * Process stop command - stop the notification scheduler
     * @param chatId The chat ID to send response to
     * @throws IOException if sending fails
     */
    public void processStopCommand(String chatId) throws IOException {
        String targetChatId = chatId;
        
        if (!notificationScheduler.isRunning()) {
            String msg = "⚠️ *Notification polling is not running\\!*\n\n" +
                "The bot is currently paused\\.\n" +
                "Use `/pull` to start polling\\.";
            telegramClient.sendToChat(targetChatId, msg);
        } else {
            notificationScheduler.stop();
            String msg = "⏸️ *Notification polling stopped\\!*\n\n" +
                "The bot has paused pulling YouTrack notifications\\.\n" +
                "💡 *Tip\\:* Use `/pull` to resume polling";
            telegramClient.sendToChat(targetChatId, msg);
        }
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
            "❓ *Unknown command\\:* `" + messageText + "`\n\n" +
            "📋 *Available commands\\:*\n" +
            " `/create <summary> @PROJECT_ID` \\- Create a new issue\n" +
            " `/projects` \\- Show available projects\n" +
            " `/status` \\- Show bot status\n" +
            " `/pull` \\- Start notification polling\n" +
            " `/stop` \\- Stop notification polling\n\n" +
            "💡 *Tip\\:* Use `/projects` first to see available project IDs\\!";
            
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

    private String determineErrorType(Exception e) {
        String message = e.getMessage();
        if (message == null) {
            return "Unknown Error";
        }
        
        if (message.contains("YouTrack") || message.contains("HTTP") || message.contains("400") || message.contains("401") || message.contains("403") || message.contains("404") || message.contains("500")) {
            return "YouTrack API Error";
        } else if (message.contains("Connection") || message.contains("timeout") || message.contains("Timeout")) {
            return "Connection Error";
        } else if (message.contains("Unauthorized") || message.contains("Forbidden") || message.contains("token")) {
            return "Authentication Error";
        } else {
            return "System Error";
        }
    }

    private String escapeMarkdownV2(String text) {
        if (text == null) return "";
        return text
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
            .replace("|", "\\|")
            .replace("!", "\\!")
            .replace(":", "\\:");
    }
}
