package com.gigapress.domainschema.domain.analysis.dto.response;

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
@Schema(description = "Requirements analysis result")
public class AnalysisResultResponse {
    
    @Schema(description = "Project ID", example = "proj_123456")
    private String projectId;
    
    @Schema(description = "Analysis summary")
    private String summary;
    
    @Schema(description = "Extracted requirements")
    private List<RequirementResponse> requirements;
    
    @Schema(description = "Identified entities")
    private List<String> identifiedEntities;
    
    @Schema(description = "Suggested relationships")
    private List<String> suggestedRelationships;
    
    @Schema(description = "Technology recommendations")
    private Map<String, String> technologyRecommendations;
    
    @Schema(description = "Analysis confidence score", example = "0.95")
    private Double confidenceScore;
}
