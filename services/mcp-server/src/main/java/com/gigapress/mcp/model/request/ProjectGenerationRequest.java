package com.gigapress.mcp.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectGenerationRequest {
    
    @NotBlank(message = "Project name is required")
    @JsonProperty("project_name")
    private String projectName;
    
    @NotBlank(message = "Project description is required")
    @JsonProperty("project_description")
    private String projectDescription;
    
    @NotNull(message = "Project type is required")
    @JsonProperty("project_type")
    private ProjectType projectType;
    
    @JsonProperty("technology_stack")
    private TechnologyStack technologyStack;
    
    @JsonProperty("features")
    private List<String> features;
    
    @JsonProperty("constraints")
    private ProjectConstraints constraints;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
    
    public enum ProjectType {
        WEB_APPLICATION,
        MOBILE_APP,
        API_SERVICE,
        MICROSERVICES,
        DESKTOP_APP,
        CLI_TOOL,
        LIBRARY
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TechnologyStack {
        private String frontend;
        private String backend;
        private String database;
        private String deployment;
        private List<String> additionalTools;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectConstraints {
        private String budget;
        private String timeline;
        private List<String> regulations;
        private List<String> integrations;
    }
}
