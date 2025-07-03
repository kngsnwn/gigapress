package com.gigapress.domainschema.domain.analysis.dto.request;

import com.gigapress.domainschema.domain.common.entity.ProjectType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new project")
public class CreateProjectRequest {
    
    @NotBlank(message = "Project name is required")
    @Size(min = 3, max = 100, message = "Project name must be between 3 and 100 characters")
    @Schema(description = "Name of the project", example = "E-Commerce Platform")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Schema(description = "Detailed description of the project", example = "A modern e-commerce platform with user authentication, product catalog, and payment processing")
    private String description;
    
    @NotNull(message = "Project type is required")
    @Schema(description = "Type of the project", example = "WEB_APPLICATION")
    private ProjectType projectType;
}
