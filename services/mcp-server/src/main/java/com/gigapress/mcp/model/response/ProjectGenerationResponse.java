package com.gigapress.mcp.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class ProjectGenerationResponse {
    
    @JsonProperty("project_id")
    private String projectId;
    
    @JsonProperty("project_name")
    private String projectName;
    
    @JsonProperty("generation_status")
    private GenerationStatus generationStatus;
    
    @JsonProperty("project_structure")
    private ProjectStructure projectStructure;
    
    @JsonProperty("generated_components")
    private List<GeneratedComponent> generatedComponents;
    
    @JsonProperty("setup_instructions")
    private SetupInstructions setupInstructions;
    
    @JsonProperty("generation_timestamp")
    @Builder.Default
    private LocalDateTime generationTimestamp = LocalDateTime.now();
    
    @JsonProperty("generation_duration_ms")
    private long generationDurationMs;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectStructure {
        private String rootPath;
        private Map<String, List<String>> directoryStructure;
        private List<String> configFiles;
        private Map<String, String> mainEntryPoints;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneratedComponent {
        private String componentId;
        private String componentName;
        private String componentType;
        private String location;
        private List<String> files;
        private Map<String, Object> configuration;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SetupInstructions {
        private List<String> prerequisites;
        private List<String> installationSteps;
        private Map<String, String> environmentVariables;
        private String runCommand;
        private String testCommand;
    }
    
    public enum GenerationStatus {
        SUCCESS,
        PARTIAL_SUCCESS,
        FAILED,
        IN_PROGRESS
    }
}
