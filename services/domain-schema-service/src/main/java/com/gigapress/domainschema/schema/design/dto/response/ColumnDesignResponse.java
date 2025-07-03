package com.gigapress.domainschema.schema.design.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Column design information")
public class ColumnDesignResponse {
    
    @Schema(description = "Column name", example = "user_id")
    private String columnName;
    
    @Schema(description = "Data type", example = "BIGINT")
    private String dataType;
    
    @Schema(description = "Column length")
    private Integer length;
    
    @Schema(description = "Is nullable", example = "false")
    private boolean nullable;
    
    @Schema(description = "Is primary key", example = "true")
    private boolean primaryKey;
    
    @Schema(description = "Is unique", example = "true")
    private boolean unique;
    
    @Schema(description = "Default value")
    private String defaultValue;
    
    @Schema(description = "Column comment")
    private String comment;
}
