package org.example.application.service;

import org.example.domain.view.NotificationView;

import java.util.List;
import java.util.stream.Collectors;

public final class Formatter {
    private Formatter() {}
    
    /** Escapes triple backticks to avoid breaking code blocks */
    private static String sanitizeForTripleBacktick(String s) {
        if (s == null) return "";
        return s.replace("```", "``\\`");
    }

    /** Formats a YouTrack notification into a Telegram MarkdownV2-compatible string */
    public static String toTelegramMarkdown(org.example.domain.view.NotificationView n) {
        StringBuilder sb = new StringBuilder();

        String displayId = firstNonBlank(n.issueId, n.id);
        sb.append("📌 *").append(escapeV2(displayId)).append("*");
        if (notBlank(n.title)) {
            sb.append(" — _").append(escapeV2(n.title)).append("_");
        }
        sb.append("\n");

        if (notBlank(n.comment)) {
            sb.append("```\n").append(sanitizeForTripleBacktick(n.comment)).append("\n```\n");
        }

        // if (hasStar(n)) {
        //     sb.append("star: ⭐\n");
        // }

        if (notBlank(n.status)) {
            sb.append("Status: `").append(codeV2(n.status)).append("`\n");
        }
        if (notBlank(n.priority)) {
            sb.append("Priority: `").append(codeV2(n.priority)).append("`\n");
        }
        if (notBlank(n.assignee)) {
            sb.append("Assignee: `").append(codeV2(n.assignee)).append("`\n");
        }

        if (n.tags != null && !n.tags.isEmpty()) {
            String tags = n.tags.stream()
                    .filter(Formatter::notBlank)
                    .map(Formatter::codeV2)
                    .collect(Collectors.joining("`, `"));
            if (!tags.isEmpty()) {
                sb.append("Tags: `").append(tags).append("`\n");
            }
        }

        if (notBlank(n.link)) {
            sb.append("Link: [Open](").append(escapeUrlV2(n.link)).append(")");
        }

        return sb.toString().trim();
    }

    // --------- helpers ---------

    private static boolean hasStar(org.example.domain.view.NotificationView n) {
        if (n.tags == null) return false;
        for (String t : n.tags) {
            if ("Star".equalsIgnoreCase(t)) return true;
        }
        return false;
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private static String firstNonBlank(String a, String b) {
        return notBlank(a) ? a : (b == null ? "" : b);
    }

    // Escapes all special characters for Telegram MarkdownV2 formatting.
    private static String escapeV2(String text) {
        if (text == null) return "";
        String s = text;
        s = s.replace("\\", "\\\\");
        s = s.replace("_", "\\_");
        s = s.replace("*", "\\*");
        s = s.replace("[", "\\[");
        s = s.replace("]", "\\]");
        s = s.replace("(", "\\(");
        s = s.replace(")", "\\)");
        s = s.replace("~", "\\~");
        s = s.replace("`", "\\`");
        s = s.replace(">", "\\>");
        s = s.replace("#", "\\#");
        s = s.replace("+", "\\+");
        s = s.replace("-", "\\-");
        s = s.replace("=", "\\=");
        s = s.replace("|", "\\|");
        s = s.replace("{", "\\{");
        s = s.replace("}", "\\}");
        s = s.replace(".", "\\.");
        s = s.replace("!", "\\!");
        return s;
    }

    // Escapes special characters for inline code in Telegram MarkdownV2.
    private static String codeV2(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("`", "\\`");
    }

    // Escapes URL parentheses for use in [text](url) in MarkdownV2.
    private static String escapeUrlV2(String url) {
        if (url == null) return "";
        url = url.replace(" ", "%20");
        return url.replace("(", "\\(").replace(")", "\\)");
    }
}
