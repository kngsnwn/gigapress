package com.gigapress.domainschema.schema.mapping.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "JPA entity mapping response")
public class EntityMappingResponse {
    
    @Schema(description = "Project ID", example = "proj_123456")
    private String projectId;
    
    @Schema(description = "Number of entities generated")
    private int entityCount;
    
    @Schema(description = "Number of repositories generated")
    private int repositoryCount;
    
    @Schema(description = "Generated entity files")
    private List<GeneratedFileResponse> entityFiles;
    
    @Schema(description = "Generated repository files")
    private List<GeneratedFileResponse> repositoryFiles;
    
    @Schema(description = "Generation summary")
    private Map<String, Object> summary;
}
