package com.gigapress.mcp.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
public class ComponentUpdateRequest {
    
    @NotBlank(message = "Project ID is required")
    @JsonProperty("project_id")
    private String projectId;
    
    @NotEmpty(message = "At least one component update is required")
    @JsonProperty("updates")
    private List<ComponentUpdate> updates;
    
    @JsonProperty("update_strategy")
    @Builder.Default
    private UpdateStrategy updateStrategy = UpdateStrategy.INCREMENTAL;
    
    @JsonProperty("rollback_on_error")
    @Builder.Default
    private boolean rollbackOnError = true;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComponentUpdate {
        
        @NotBlank(message = "Component ID is required")
        @JsonProperty("component_id")
        private String componentId;
        
        @NotNull(message = "Update type is required")
        @JsonProperty("update_type")
        private UpdateType updateType;
        
        @JsonProperty("update_content")
        private Map<String, Object> updateContent;
        
        @JsonProperty("dependencies")
        private List<String> dependencies;
        
        @JsonProperty("version")
        private String version;
    }
    
    public enum UpdateType {
        CREATE,
        MODIFY,
        DELETE,
        RENAME,
        MOVE,
        REFACTOR
    }
    
    public enum UpdateStrategy {
        INCREMENTAL,    // Update one by one
        BATCH,          // Update all at once
        PARALLEL        // Update in parallel where possible
    }
}
