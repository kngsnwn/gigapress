package com.gigapress.domainschema.domain.analysis.dto.response;

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
@Schema(description = "Domain model response")
public class DomainModelResponse {
    
    @Schema(description = "Domain model ID")
    private Long id;
    
    @Schema(description = "Project ID", example = "proj_123456")
    private String projectId;
    
    @Schema(description = "Model name")
    private String name;
    
    @Schema(description = "Model description")
    private String description;
    
    @Schema(description = "Number of entities")
    private int entityCount;
    
    @Schema(description = "Number of relationships")
    private int relationshipCount;
    
    @Schema(description = "Domain entities")
    private List<DomainEntityResponse> entities;
    
    @Schema(description = "Domain relationships")
    private List<DomainRelationshipResponse> relationships;
    
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
}
