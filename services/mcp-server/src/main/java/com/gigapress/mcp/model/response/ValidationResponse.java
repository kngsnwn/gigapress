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
public class ValidationResponse {
    
    @JsonProperty("validation_id")
    private String validationId;
    
    @JsonProperty("project_id")
    private String projectId;
    
    @JsonProperty("validation_status")
    private ValidationStatus validationStatus;
    
    @JsonProperty("validation_results")
    private List<ValidationResult> validationResults;
    
    @JsonProperty("auto_fixes_applied")
    private List<AutoFix> autoFixesApplied;
    
    @JsonProperty("validation_summary")
    private ValidationSummary validationSummary;
    
    @JsonProperty("validation_timestamp")
    @Builder.Default
    private LocalDateTime validationTimestamp = LocalDateTime.now();
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationResult {
        private String validationType;
        private ValidationStatus status;
        private List<Issue> issues;
        private Map<String, Object> metrics;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Issue {
        private String issueId;
        private Severity severity;
        private String category;
        private String component;
        private String file;
        private Integer line;
        private String description;
        private String suggestion;
        private boolean autoFixable;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AutoFix {
        private String issueId;
        private String fixDescription;
        private List<String> modifiedFiles;
        private boolean successful;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationSummary {
        private int totalIssues;
        private Map<String, Integer> issuesBySeverity;
        private Map<String, Integer> issuesByType;
        private int autoFixableCount;
        private int autoFixedCount;
    }
    
    public enum ValidationStatus {
        PASSED,
        PASSED_WITH_WARNINGS,
        FAILED,
        ERROR
    }
    
    public enum Severity {
        INFO,
        WARNING,
        ERROR,
        CRITICAL
    }
}
