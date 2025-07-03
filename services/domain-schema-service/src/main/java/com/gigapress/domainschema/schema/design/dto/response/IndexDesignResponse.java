package com.gigapress.domainschema.schema.design.dto.response;

import com.gigapress.domainschema.domain.common.entity.IndexType;
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
@Schema(description = "Index design information")
public class IndexDesignResponse {
    
    @Schema(description = "Index name", example = "idx_users_email")
    private String indexName;
    
    @Schema(description = "Index type", example = "BTREE")
    private IndexType indexType;
    
    @Schema(description = "Is unique index", example = "true")
    private boolean unique;
    
    @Schema(description = "Indexed columns", example = "email")
    private List<String> columns;
    
    @Schema(description = "Where clause for partial index")
    private String whereClause;
}
