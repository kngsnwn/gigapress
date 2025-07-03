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
public class ComponentUpdateResponse {
    
    @JsonProperty("update_id")
    private String updateId;
    
    @JsonProperty("project_id")
    private String projectId;
    
    @JsonProperty("overall_status")
    private UpdateStatus overallStatus;
    
    @JsonProperty("update_results")
    private List<UpdateResult> updateResults;
    
    @JsonProperty("rollback_available")
    private boolean rollbackAvailable;
    
    @JsonProperty("update_summary")
    private UpdateSummary updateSummary;
    
    @JsonProperty("update_timestamp")
    @Builder.Default
    private LocalDateTime updateTimestamp = LocalDateTime.now();
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateResult {
        private String componentId;
        private String componentName;
        private UpdateStatus status;
        private List<String> modifiedFiles;
        private List<String> warnings;
        private String errorMessage;
        private Map<String, Object> metadata;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateSummary {
        private int totalComponents;
        private int successfulUpdates;
        private int failedUpdates;
        private int warningCount;
        private long totalDurationMs;
    }
    
    public enum UpdateStatus {
        SUCCESS,
        FAILED,
        PARTIAL,
        SKIPPED,
        ROLLED_BACK
    }
}
