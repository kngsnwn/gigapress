package com.gigapress.domainschema.domain.analysis.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to analyze project requirements")
public class AnalyzeRequirementsRequest {
    
    @NotBlank(message = "Project ID is required")
    @Schema(description = "ID of the project", example = "proj_123456")
    private String projectId;
    
    @NotBlank(message = "Natural language requirements are required")
    @Size(min = 10, max = 10000, message = "Requirements must be between 10 and 10000 characters")
    @Schema(description = "Natural language description of requirements", 
            example = "Users should be able to register and login. Products should have categories and reviews.")
    private String naturalLanguageRequirements;
    
    @Schema(description = "Additional context or constraints", example = "Must support mobile devices , Need to handle 1000 concurrent users")
    private List<String> constraints;
    
    @Schema(description = "Technology preferences", example = "frontend:React, backend:Spring Boot , database :PostgreSQL")
    private java.util.Map<String, String> technologyPreferences;
}
