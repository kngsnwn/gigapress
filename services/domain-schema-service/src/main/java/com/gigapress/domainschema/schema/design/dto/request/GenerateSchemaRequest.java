package com.gigapress.domainschema.schema.design.dto.request;

import com.gigapress.domainschema.domain.common.entity.DatabaseType;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request to generate database schema")
public class GenerateSchemaRequest {
    
    @NotBlank(message = "Project ID is required")
    @Schema(description = "ID of the project", example = "proj_123456")
    private String projectId;
    
    @NotNull(message = "Database type is required")
    @Schema(description = "Target database type", example = "POSTGRESQL")
    private DatabaseType databaseType;
    
    @Schema(description = "Schema name", example = "ecommerce")
    private String schemaName;
    
    @Schema(description = "Database-specific options")
    private Map<String, Object> databaseOptions;
    
    @Schema(description = "Include audit columns", example = "true")
    @Builder.Default
    private boolean includeAuditColumns = true;
    
    @Schema(description = "Generate indexes", example = "true")
    @Builder.Default
    private boolean generateIndexes = true;
}
