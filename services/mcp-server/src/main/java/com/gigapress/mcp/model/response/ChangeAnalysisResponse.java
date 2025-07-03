package com.gigapress.mcp.model.response;

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
public class ChangeAnalysisResponse {
    
    @JsonProperty("analysis_id")
    private String analysisId;
    
    @JsonProperty("project_id")
    private String projectId;
    
    @JsonProperty("impact_summary")
    private ImpactSummary impactSummary;
    
    @JsonProperty("affected_components")
    private List<AffectedComponent> affectedComponents;
    
    @JsonProperty("risk_assessment")
    private RiskAssessment riskAssessment;
    
    @JsonProperty("recommendations")
    private List<String> recommendations;
    
    @JsonProperty("estimated_effort")
    private EffortEstimate estimatedEffort;
    
    @JsonProperty("analysis_timestamp")
    @Builder.Default
    private LocalDateTime analysisTimestamp = LocalDateTime.now();
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImpactSummary {
        private int totalComponentsAffected;
        private int directImpact;
        private int indirectImpact;
        private List<String> criticalPaths;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AffectedComponent {
        private String componentId;
        private String componentName;
        private String componentType;
        private ImpactLevel impactLevel;
        private List<String> impactedFeatures;
        private Map<String, Object> changeDetails;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskAssessment {
        private RiskLevel overallRisk;
        private List<Risk> identifiedRisks;
        private Map<String, String> mitigationStrategies;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Risk {
        private String riskType;
        private RiskLevel level;
        private String description;
        private double probability;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EffortEstimate {
        private int estimatedHours;
        private int developerCount;
        private Map<String, Integer> effortByComponent;
    }
    
    public enum ImpactLevel {
        NONE, LOW, MEDIUM, HIGH, CRITICAL
    }
    
    public enum RiskLevel {
        MINIMAL, LOW, MODERATE, HIGH, SEVERE
    }
}
