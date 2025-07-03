package com.gigapress.domainschema.schema.design.dto.response;

import com.gigapress.domainschema.domain.common.entity.DatabaseType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Database schema design response")
public class SchemaDesignResponse {
    
    @Schema(description = "Schema design ID")
    private Long id;
    
    @Schema(description = "Project ID", example = "proj_123456")
    private String projectId;
    
    @Schema(description = "Schema name", example = "ecommerce")
    private String schemaName;
    
    @Schema(description = "Database type", example = "POSTGRESQL")
    private DatabaseType databaseType;
    
    @Schema(description = "Number of tables")
    private int tableCount;
    
    @Schema(description = "Table designs")
    private List<TableDesignResponse> tables;
    
    @Schema(description = "DDL script preview")
    private String ddlScriptPreview;
    
    @Schema(description = "Full DDL script available")
    private boolean fullDdlAvailable;
    
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
}
