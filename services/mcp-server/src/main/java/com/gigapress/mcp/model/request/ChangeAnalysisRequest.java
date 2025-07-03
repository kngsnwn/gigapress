package com.gigapress.mcp.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeAnalysisRequest {
    
    @NotBlank(message = "Project ID is required")
    @JsonProperty("project_id")
    private String projectId;
    
    @NotBlank(message = "Change description is required")
    @JsonProperty("change_description")
    private String changeDescription;
    
    @JsonProperty("change_type")
    private ChangeType changeType;
    
    @JsonProperty("target_components")
    private String[] targetComponents;
    
    @JsonProperty("user_context")
    private Map<String, Object> userContext;
    
    @JsonProperty("analysis_depth")
    @Builder.Default
    private AnalysisDepth analysisDepth = AnalysisDepth.NORMAL;
    
    public enum ChangeType {
        FEATURE_ADD,
        FEATURE_MODIFY,
        FEATURE_REMOVE,
        REFACTOR,
        BUG_FIX,
        PERFORMANCE,
        SECURITY,
        DEPENDENCY_UPDATE
    }
    
    public enum AnalysisDepth {
        SHALLOW,    // Direct dependencies only
        NORMAL,     // Up to 2 levels
        DEEP        // Full dependency tree
    }
}
