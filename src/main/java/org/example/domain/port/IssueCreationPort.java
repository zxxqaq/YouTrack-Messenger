package org.example.domain.port;

import org.example.domain.model.ProjectInfo;
import java.io.IOException;
import java.util.List;

public interface IssueCreationPort {

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
     * @return List of project information (ID and name)
     * @throws IOException if fetching fails
     */
    List<ProjectInfo> getAvailableProjects() throws IOException;
}
