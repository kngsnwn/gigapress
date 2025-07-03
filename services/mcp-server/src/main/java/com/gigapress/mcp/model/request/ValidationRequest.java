package com.gigapress.mcp.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationRequest {
    
    @NotBlank(message = "Project ID is required")
    @JsonProperty("project_id")
    private String projectId;
    
    @NotEmpty(message = "At least one validation type is required")
    @JsonProperty("validation_types")
    private List<ValidationType> validationTypes;
    
    @JsonProperty("include_warnings")
    @Builder.Default
    private boolean includeWarnings = true;
    
    @JsonProperty("auto_fix")
    @Builder.Default
    private boolean autoFix = false;
    
    public enum ValidationType {
        DEPENDENCY_CONSISTENCY,
        CODE_QUALITY,
        SECURITY_SCAN,
        PERFORMANCE_CHECK,
        ARCHITECTURE_COMPLIANCE,
        API_CONTRACT,
        DATABASE_SCHEMA,
        CONFIGURATION
    }
}
