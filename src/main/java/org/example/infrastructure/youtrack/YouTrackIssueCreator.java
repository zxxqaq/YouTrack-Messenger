package org.example.infrastructure.youtrack;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.example.domain.port.IssueCreationPort;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class YouTrackIssueCreator implements IssueCreationPort {

    private final YouTrackProperties properties;
    private final OkHttpClient http = new OkHttpClient();
    private final ObjectMapper om = new ObjectMapper();

    public YouTrackIssueCreator(YouTrackProperties properties) {
        this.properties = properties;
    }

    @Override
    public String createIssue(String summary) throws IOException {
        return createIssue(summary, "DEMO"); // Default to DEMO project
    }

    /**
     * Create a new issue in YouTrack with specified project
     * @param summary The issue summary/title
     * @param projectId The project ID (e.g., "DEMO", "PROJ")
     * @return The created issue ID
     * @throws IOException if creation fails
     */
    public String createIssue(String summary, String projectId) throws IOException {
        String base = normalizeBase(properties.getBaseUrl());
        HttpUrl url = HttpUrl.parse(base + "/api/issues").newBuilder().build();

        // Create issue payload with Draft status
        String jsonPayload = String.format(
            "{\"summary\": \"%s\", \"project\": {\"id\": \"%s\"}, \"fields\": [{\"name\": \"State\", \"value\": \"Draft\"}]}",
            summary.replace("\"", "\\\""), // Escape quotes
            projectId.replace("\"", "\\\"") // Escape quotes
        );

        RequestBody body = RequestBody.create(
            jsonPayload,
            MediaType.get("application/json; charset=utf-8")
        );

        Request req = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + properties.getToken())
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response resp = http.newCall(req).execute()) {
            if (!resp.isSuccessful()) {
                throw new IOException("YouTrack " + resp.code() + ": " + resp.message());
            }
            
            JsonNode response = om.readTree(resp.body().byteStream());
            String issueId = response.path("id").asText();
            
            if (issueId.isEmpty()) {
                throw new IOException("Failed to get issue ID from response");
            }
            
            return issueId;
        }
    }

    @Override
    public List<String> getAvailableProjects() throws IOException {
        String base = normalizeBase(properties.getBaseUrl());
        HttpUrl url = HttpUrl.parse(base + "/api/admin/projects")
                .newBuilder()
                .addQueryParameter("fields", "id,name")
                .build();

        Request req = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + properties.getToken())
                .build();

        try (Response resp = http.newCall(req).execute()) {
            if (!resp.isSuccessful()) {
                throw new IOException("YouTrack " + resp.code() + ": " + resp.message());
            }
            
            JsonNode response = om.readTree(resp.body().byteStream());
            List<String> projects = new ArrayList<>();
            
            if (response.isArray()) {
                for (JsonNode project : response) {
                    String projectId = project.path("id").asText();
                    if (!projectId.isEmpty()) {
                        projects.add(projectId);
                    }
                }
            }
            
            return projects;
        }
    }

    private static String normalizeBase(String baseUrl) {
        if (baseUrl == null) return "";
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
