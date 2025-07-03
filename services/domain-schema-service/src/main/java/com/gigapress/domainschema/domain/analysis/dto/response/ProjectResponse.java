package com.gigapress.domainschema.domain.analysis.dto.response;

import com.gigapress.domainschema.domain.common.entity.ProjectStatus;
import com.gigapress.domainschema.domain.common.entity.ProjectType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Project information response")
public class ProjectResponse {
    
    @Schema(description = "Project ID", example = "proj_123456")
    private String projectId;
    
    @Schema(description = "Project name", example = "E-Commerce Platform")
    private String name;
    
    @Schema(description = "Project description")
    private String description;
    
    @Schema(description = "Project type", example = "WEB_APPLICATION")
    private ProjectType projectType;
    
    @Schema(description = "Current project status", example = "ANALYZING")
    private ProjectStatus status;
    
    @Schema(description = "Number of requirements")
    private int requirementCount;
    
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
    
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
