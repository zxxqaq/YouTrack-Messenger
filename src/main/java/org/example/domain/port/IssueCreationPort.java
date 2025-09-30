package org.example.domain.port;

import java.io.IOException;
import java.util.List;

public interface IssueCreationPort {
    /**
     * Create a new issue in YouTrack
     * @param summary The issue summary/title
     * @return The created issue ID
     * @throws IOException if creation fails
     */
    String createIssue(String summary) throws IOException;

    /**
     * Create a new issue in YouTrack with specified project
     * @param summary The issue summary/title
     * @param projectId The project ID
     * @return The created issue ID
     * @throws IOException if creation fails
     */
    String createIssue(String summary, String projectId) throws IOException;

    /**
     * Get list of available projects
     * @return List of project IDs
     * @throws IOException if fetching fails
     */
    List<String> getAvailableProjects() throws IOException;
}
