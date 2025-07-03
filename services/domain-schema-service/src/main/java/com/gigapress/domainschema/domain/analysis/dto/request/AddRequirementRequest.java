package com.gigapress.domainschema.domain.analysis.dto.request;

import com.gigapress.domainschema.domain.common.entity.RequirementPriority;
import com.gigapress.domainschema.domain.common.entity.RequirementType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to add a new requirement")
public class AddRequirementRequest {
    
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    @Schema(description = "Requirement title", example = "User Authentication")
    private String title;
    
    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    @Schema(description = "Detailed requirement description", example = "Users must be able to register with email and password")
    private String description;
    
    @NotNull(message = "Requirement type is required")
    @Schema(description = "Type of requirement", example = "FUNCTIONAL")
    private RequirementType type;
    
    @NotNull(message = "Priority is required")
    @Schema(description = "Requirement priority", example = "HIGH")
    private RequirementPriority priority;
    
    @Schema(description = "Additional metadata")
    private Map<String, String> metadata;
}
