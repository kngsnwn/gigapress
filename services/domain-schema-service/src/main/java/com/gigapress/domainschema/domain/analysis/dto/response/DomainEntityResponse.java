package com.gigapress.domainschema.domain.analysis.dto.response;

import com.gigapress.domainschema.domain.common.entity.EntityType;
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
@Schema(description = "Domain entity information")
public class DomainEntityResponse {
    
    @Schema(description = "Entity ID")
    private Long id;
    
    @Schema(description = "Entity name", example = "User")
    private String name;
    
    @Schema(description = "Entity description")
    private String description;
    
    @Schema(description = "Database table name", example = "users")
    private String tableName;
    
    @Schema(description = "Entity type", example = "AGGREGATE_ROOT")
    private EntityType entityType;
    
    @Schema(description = "Entity attributes")
    private List<DomainAttributeResponse> attributes;
    
    @Schema(description = "Business rules")
    private String businessRules;
}
