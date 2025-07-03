package com.gigapress.domainschema.domain.analysis.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Domain attribute information")
public class DomainAttributeResponse {
    
    @Schema(description = "Attribute name", example = "email")
    private String name;
    
    @Schema(description = "Field name in code", example = "email")
    private String fieldName;
    
    @Schema(description = "Data type", example = "String")
    private String dataType;
    
    @Schema(description = "Is required", example = "true")
    private boolean required;
    
    @Schema(description = "Is unique", example = "true")
    private boolean unique;
    
    @Schema(description = "Field length")
    private Integer length;
    
    @Schema(description = "Default value")
    private String defaultValue;
    
    @Schema(description = "Validation rules")
    private String validationRules;
    
    @Schema(description = "Description")
    private String description;
}
