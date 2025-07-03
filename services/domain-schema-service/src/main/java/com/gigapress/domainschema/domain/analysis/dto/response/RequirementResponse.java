package com.gigapress.domainschema.domain.analysis.dto.response;

import com.gigapress.domainschema.domain.common.entity.RequirementPriority;
import com.gigapress.domainschema.domain.common.entity.RequirementStatus;
import com.gigapress.domainschema.domain.common.entity.RequirementType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requirement information response")
public class RequirementResponse {
    
    @Schema(description = "Requirement ID")
    private Long id;
    
    @Schema(description = "Requirement title", example = "User Authentication")
    private String title;
    
    @Schema(description = "Detailed description")
    private String description;
    
    @Schema(description = "Requirement type", example = "FUNCTIONAL")
    private RequirementType type;
    
    @Schema(description = "Priority level", example = "HIGH")
    private RequirementPriority priority;
    
    @Schema(description = "Current status", example = "ANALYZED")
    private RequirementStatus status;
    
    @Schema(description = "Additional metadata")
    private Map<String, String> metadata;
    
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
}
