package com.gigapress.mcp.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    
    private String projectId;
    private String projectName;
    private String description;
    private ProjectType type;
    private ProjectStatus status;
    private String version;
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;
    private Map<String, Object> metadata;
    private List<Component> components;
    private DependencyGraph dependencyGraph;
    
    public enum ProjectType {
        WEB_APPLICATION,
        MOBILE_APP,
        API_SERVICE,
        MICROSERVICES,
        DESKTOP_APP,
        CLI_TOOL,
        LIBRARY
    }
    
    public enum ProjectStatus {
        PLANNING,
        IN_DEVELOPMENT,
        TESTING,
        DEPLOYED,
        MAINTENANCE,
        ARCHIVED
    }
}
