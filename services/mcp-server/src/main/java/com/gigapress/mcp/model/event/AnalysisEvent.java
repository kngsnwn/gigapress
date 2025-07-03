package com.gigapress.mcp.model.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisEvent {
    
    @JsonProperty("analysis_id")
    private String analysisId;
    
    @JsonProperty("project_id")
    private String projectId;
    
    @JsonProperty("analysis_type")
    private AnalysisType analysisType;
    
    @JsonProperty("trigger_source")
    private String triggerSource;
    
    @JsonProperty("affected_components")
    private List<String> affectedComponents;
    
    @JsonProperty("analysis_results")
    private Map<String, Object> analysisResults;
    
    @JsonProperty("recommendations")
    private List<String> recommendations;
    
    @JsonProperty("timestamp")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    public enum AnalysisType {
        CHANGE_IMPACT,
        DEPENDENCY_ANALYSIS,
        RISK_ASSESSMENT,
        PERFORMANCE_ANALYSIS,
        SECURITY_SCAN
    }
}
