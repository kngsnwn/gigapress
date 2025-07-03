package com.gigapress.domainschema.domain.analysis.dto.response;

import com.gigapress.domainschema.domain.common.entity.RelationshipType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Domain relationship information")
public class DomainRelationshipResponse {
    
    @Schema(description = "Source entity name", example = "User")
    private String sourceEntity;
    
    @Schema(description = "Target entity name", example = "Order")
    private String targetEntity;
    
    @Schema(description = "Relationship type", example = "ONE_TO_MANY")
    private RelationshipType relationshipType;
    
    @Schema(description = "Source field name", example = "orders")
    private String sourceFieldName;
    
    @Schema(description = "Target field name", example = "user")
    private String targetFieldName;
    
    @Schema(description = "Is bidirectional", example = "true")
    private boolean bidirectional;
    
    @Schema(description = "Cascade type", example = "ALL")
    private String cascadeType;
    
    @Schema(description = "Fetch type", example = "LAZY")
    private String fetchType;
    
    @Schema(description = "Description")
    private String description;
}
