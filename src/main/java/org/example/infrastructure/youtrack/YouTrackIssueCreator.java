package org.example.infrastructure.youtrack;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.example.domain.model.ProjectInfo;
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

    /**
     * Create a new issue in YouTrack with specified project
     * @param summary The issue summary/title
     * @param projectId The project ID (e.g., "0-0")
     * @return The created issue ID
     * @throws IOException if creation fails
     */
    public String createIssue(String summary, String projectId) throws IOException {
        String base = normalizeBase(properties.getBaseUrl());
        HttpUrl url = HttpUrl.parse(base + "/api/issues").newBuilder().build();

        // Create issue payload
        String jsonPayload = String.format(
            "{\"summary\": \"%s\", \"project\": {\"id\": \"%s\"}}",
            summary.replace("\"", "\\\""), // Escape quotes
            projectId.replace("\"", "\\\"") // Escape quotes
        );

        System.out.println("DEBUG: Creating YouTrack issue with payload: " + jsonPayload);
        System.out.println("DEBUG: YouTrack URL: " + url);
        System.out.println("DEBUG: YouTrack Token: " + properties.getToken().substring(0, 10) + "...");

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
                String errorBody = resp.body() != null ? resp.body().string() : "No error body";
                throw new IOException("YouTrack " + resp.code() + ": " + resp.message() + " - " + errorBody);
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
    public List<ProjectInfo> getAvailableProjects() throws IOException {
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
            List<ProjectInfo> projects = new ArrayList<>();

            if (response.isArray()) {
                for (JsonNode project : response) {
                    String projectId = project.path("id").asText();
                    String projectName = project.path("name").asText();
                    if (!projectId.isEmpty()) {
                        projects.add(new ProjectInfo(projectId, projectName.isEmpty() ? projectId : projectName));
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
