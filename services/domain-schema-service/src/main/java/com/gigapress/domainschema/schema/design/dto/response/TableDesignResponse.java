package com.gigapress.domainschema.schema.design.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Table design information")
public class TableDesignResponse {
    
    @Schema(description = "Table name", example = "users")
    private String tableName;
    
    @Schema(description = "Table description")
    private String description;
    
    @Schema(description = "Column count")
    private int columnCount;
    
    @Schema(description = "Index count")
    private int indexCount;
    
    @Schema(description = "Column designs")
    private List<ColumnDesignResponse> columns;
    
    @Schema(description = "Index designs")
    private List<IndexDesignResponse> indexes;
}
