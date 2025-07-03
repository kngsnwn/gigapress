package com.gigapress.domainschema.schema.mapping.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to generate JPA entities")
public class GenerateEntitiesRequest {
    
    @NotBlank(message = "Project ID is required")
    @Schema(description = "ID of the project", example = "proj_123456")
    private String projectId;
    
    @Schema(description = "Package name for entities", example = "com.example.entities")
    @Builder.Default
    private String packageName = "com.generated.entities";
    
    @Schema(description = "Use Lombok annotations", example = "true")
    @Builder.Default
    private boolean useLombok = true;
    
    @Schema(description = "Generate repository interfaces", example = "true")
    @Builder.Default
    private boolean generateRepositories = true;
    
    @Schema(description = "Include validation annotations", example = "true")
    @Builder.Default
    private boolean includeValidation = true;
    
    @Schema(description = "Additional generation options")
    private Map<String, Object> generationOptions;
}
