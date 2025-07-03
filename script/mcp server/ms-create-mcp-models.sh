#!/bin/bash

# MCP Server Domain Models Creation Script

echo "🎯 Creating domain models for MCP Server..."

BASE_DIR="services/mcp-server"
MODEL_DIR="$BASE_DIR/src/main/java/com/gigapress/mcp/model"

# Create model subdirectories
mkdir -p $MODEL_DIR/{request,response,domain,event}

# ===== REQUEST MODELS =====

# ChangeAnalysisRequest.java
echo "📝 Creating ChangeAnalysisRequest.java..."
cat > $MODEL_DIR/request/ChangeAnalysisRequest.java << 'EOF'
package com.gigapress.mcp.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeAnalysisRequest {
    
    @NotBlank(message = "Project ID is required")
    @JsonProperty("project_id")
    private String projectId;
    
    @NotBlank(message = "Change description is required")
    @JsonProperty("change_description")
    private String changeDescription;
    
    @JsonProperty("change_type")
    private ChangeType changeType;
    
    @JsonProperty("target_components")
    private String[] targetComponents;
    
    @JsonProperty("user_context")
    private Map<String, Object> userContext;
    
    @JsonProperty("analysis_depth")
    @Builder.Default
    private AnalysisDepth analysisDepth = AnalysisDepth.NORMAL;
    
    public enum ChangeType {
        FEATURE_ADD,
        FEATURE_MODIFY,
        FEATURE_REMOVE,
        REFACTOR,
        BUG_FIX,
        PERFORMANCE,
        SECURITY,
        DEPENDENCY_UPDATE
    }
    
    public enum AnalysisDepth {
        SHALLOW,    // Direct dependencies only
        NORMAL,     // Up to 2 levels
        DEEP        // Full dependency tree
    }
}
EOF

# ProjectGenerationRequest.java
echo "📝 Creating ProjectGenerationRequest.java..."
cat > $MODEL_DIR/request/ProjectGenerationRequest.java << 'EOF'
package com.gigapress.mcp.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ProjectGenerationRequest {
    
    @NotBlank(message = "Project name is required")
    @JsonProperty("project_name")
    private String projectName;
    
    @NotBlank(message = "Project description is required")
    @JsonProperty("project_description")
    private String projectDescription;
    
    @NotNull(message = "Project type is required")
    @JsonProperty("project_type")
    private ProjectType projectType;
    
    @JsonProperty("technology_stack")
    private TechnologyStack technologyStack;
    
    @JsonProperty("features")
    private List<String> features;
    
    @JsonProperty("constraints")
    private ProjectConstraints constraints;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
    
    public enum ProjectType {
        WEB_APPLICATION,
        MOBILE_APP,
        API_SERVICE,
        MICROSERVICES,
        DESKTOP_APP,
        CLI_TOOL,
        LIBRARY
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TechnologyStack {
        private String frontend;
        private String backend;
        private String database;
        private String deployment;
        private List<String> additionalTools;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectConstraints {
        private String budget;
        private String timeline;
        private List<String> regulations;
        private List<String> integrations;
    }
}
EOF

# ComponentUpdateRequest.java
echo "📝 Creating ComponentUpdateRequest.java..."
cat > $MODEL_DIR/request/ComponentUpdateRequest.java << 'EOF'
package com.gigapress.mcp.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class ComponentUpdateRequest {
    
    @NotBlank(message = "Project ID is required")
    @JsonProperty("project_id")
    private String projectId;
    
    @NotEmpty(message = "At least one component update is required")
    @JsonProperty("updates")
    private List<ComponentUpdate> updates;
    
    @JsonProperty("update_strategy")
    @Builder.Default
    private UpdateStrategy updateStrategy = UpdateStrategy.INCREMENTAL;
    
    @JsonProperty("rollback_on_error")
    @Builder.Default
    private boolean rollbackOnError = true;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComponentUpdate {
        
        @NotBlank(message = "Component ID is required")
        @JsonProperty("component_id")
        private String componentId;
        
        @NotNull(message = "Update type is required")
        @JsonProperty("update_type")
        private UpdateType updateType;
        
        @JsonProperty("update_content")
        private Map<String, Object> updateContent;
        
        @JsonProperty("dependencies")
        private List<String> dependencies;
        
        @JsonProperty("version")
        private String version;
    }
    
    public enum UpdateType {
        CREATE,
        MODIFY,
        DELETE,
        RENAME,
        MOVE,
        REFACTOR
    }
    
    public enum UpdateStrategy {
        INCREMENTAL,    // Update one by one
        BATCH,          // Update all at once
        PARALLEL        // Update in parallel where possible
    }
}
EOF

# ValidationRequest.java
echo "📝 Creating ValidationRequest.java..."
cat > $MODEL_DIR/request/ValidationRequest.java << 'EOF'
package com.gigapress.mcp.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationRequest {
    
    @NotBlank(message = "Project ID is required")
    @JsonProperty("project_id")
    private String projectId;
    
    @NotEmpty(message = "At least one validation type is required")
    @JsonProperty("validation_types")
    private List<ValidationType> validationTypes;
    
    @JsonProperty("include_warnings")
    @Builder.Default
    private boolean includeWarnings = true;
    
    @JsonProperty("auto_fix")
    @Builder.Default
    private boolean autoFix = false;
    
    public enum ValidationType {
        DEPENDENCY_CONSISTENCY,
        CODE_QUALITY,
        SECURITY_SCAN,
        PERFORMANCE_CHECK,
        ARCHITECTURE_COMPLIANCE,
        API_CONTRACT,
        DATABASE_SCHEMA,
        CONFIGURATION
    }
}
EOF

# ===== RESPONSE MODELS =====

# ChangeAnalysisResponse.java
echo "📝 Creating ChangeAnalysisResponse.java..."
cat > $MODEL_DIR/response/ChangeAnalysisResponse.java << 'EOF'
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
EOF

# ProjectGenerationResponse.java
echo "📝 Creating ProjectGenerationResponse.java..."
cat > $MODEL_DIR/response/ProjectGenerationResponse.java << 'EOF'
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
public class ProjectGenerationResponse {
    
    @JsonProperty("project_id")
    private String projectId;
    
    @JsonProperty("project_name")
    private String projectName;
    
    @JsonProperty("generation_status")
    private GenerationStatus generationStatus;
    
    @JsonProperty("project_structure")
    private ProjectStructure projectStructure;
    
    @JsonProperty("generated_components")
    private List<GeneratedComponent> generatedComponents;
    
    @JsonProperty("setup_instructions")
    private SetupInstructions setupInstructions;
    
    @JsonProperty("generation_timestamp")
    @Builder.Default
    private LocalDateTime generationTimestamp = LocalDateTime.now();
    
    @JsonProperty("generation_duration_ms")
    private long generationDurationMs;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectStructure {
        private String rootPath;
        private Map<String, List<String>> directoryStructure;
        private List<String> configFiles;
        private Map<String, String> mainEntryPoints;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneratedComponent {
        private String componentId;
        private String componentName;
        private String componentType;
        private String location;
        private List<String> files;
        private Map<String, Object> configuration;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SetupInstructions {
        private List<String> prerequisites;
        private List<String> installationSteps;
        private Map<String, String> environmentVariables;
        private String runCommand;
        private String testCommand;
    }
    
    public enum GenerationStatus {
        SUCCESS,
        PARTIAL_SUCCESS,
        FAILED,
        IN_PROGRESS
    }
}
EOF

# ComponentUpdateResponse.java
echo "📝 Creating ComponentUpdateResponse.java..."
cat > $MODEL_DIR/response/ComponentUpdateResponse.java << 'EOF'
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
EOF

# ValidationResponse.java
echo "📝 Creating ValidationResponse.java..."
cat > $MODEL_DIR/response/ValidationResponse.java << 'EOF'
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
EOF

# ===== DOMAIN MODELS =====

# Project.java
echo "📝 Creating Project.java..."
cat > $MODEL_DIR/domain/Project.java << 'EOF'
package com.gigapress.mcp.model.domain;

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
public class Project {
    
    private String projectId;
    private String projectName;
    private String description;
    private ProjectType type;
    private ProjectStatus status;
    private String version;
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;
    private Map<String, Object> metadata;
    private List<Component> components;
    private DependencyGraph dependencyGraph;
    
    public enum ProjectType {
        WEB_APPLICATION,
        MOBILE_APP,
        API_SERVICE,
        MICROSERVICES,
        DESKTOP_APP,
        CLI_TOOL,
        LIBRARY
    }
    
    public enum ProjectStatus {
        PLANNING,
        IN_DEVELOPMENT,
        TESTING,
        DEPLOYED,
        MAINTENANCE,
        ARCHIVED
    }
}
EOF

# Component.java
echo "📝 Creating Component.java..."
cat > $MODEL_DIR/domain/Component.java << 'EOF'
package com.gigapress.mcp.model.domain;

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
public class Component {
    
    private String componentId;
    private String componentName;
    private ComponentType type;
    private String version;
    private String location;
    private List<String> dependencies;
    private List<String> dependents;
    private Map<String, Object> configuration;
    private ComponentStatus status;
    private LocalDateTime lastModified;
    private Map<String, Object> metadata;
    
    public enum ComponentType {
        FRONTEND,
        BACKEND,
        DATABASE,
        API,
        SERVICE,
        LIBRARY,
        CONFIGURATION,
        INFRASTRUCTURE
    }
    
    public enum ComponentStatus {
        ACTIVE,
        DEPRECATED,
        MAINTENANCE,
        ARCHIVED
    }
}
EOF

# DependencyGraph.java
echo "📝 Creating DependencyGraph.java..."
cat > $MODEL_DIR/domain/DependencyGraph.java << 'EOF'
package com.gigapress.mcp.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DependencyGraph {
    
    private Map<String, Node> nodes;
    private List<Edge> edges;
    
    @Builder.Default
    private Map<String, Set<String>> adjacencyList = new HashMap<>();
    
    public void addNode(String componentId, Component component) {
        if (nodes == null) {
            nodes = new HashMap<>();
        }
        nodes.put(componentId, new Node(componentId, component));
    }
    
    public void addEdge(String from, String to, EdgeType type) {
        if (edges == null) {
            edges = new ArrayList<>();
        }
        edges.add(new Edge(from, to, type));
        
        adjacencyList.computeIfAbsent(from, k -> new HashSet<>()).add(to);
    }
    
    public Set<String> getDirectDependencies(String componentId) {
        return adjacencyList.getOrDefault(componentId, new HashSet<>());
    }
    
    public Set<String> getAllDependencies(String componentId) {
        Set<String> visited = new HashSet<>();
        Set<String> allDeps = new HashSet<>();
        
        collectDependencies(componentId, visited, allDeps);
        return allDeps;
    }
    
    private void collectDependencies(String componentId, Set<String> visited, Set<String> allDeps) {
        if (visited.contains(componentId)) {
            return;
        }
        
        visited.add(componentId);
        Set<String> directDeps = getDirectDependencies(componentId);
        
        for (String dep : directDeps) {
            allDeps.add(dep);
            collectDependencies(dep, visited, allDeps);
        }
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Node {
        private String id;
        private Component component;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Edge {
        private String from;
        private String to;
        private EdgeType type;
    }
    
    public enum EdgeType {
        DEPENDS_ON,
        USES,
        CALLS,
        EXTENDS,
        IMPLEMENTS
    }
}
EOF

# ===== EVENT MODELS =====

# ProjectEvent.java
echo "📝 Creating ProjectEvent.java..."
cat > $MODEL_DIR/event/ProjectEvent.java << 'EOF'
package com.gigapress.mcp.model.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEvent {
    
    @JsonProperty("event_id")
    private String eventId;
    
    @JsonProperty("event_type")
    private EventType eventType;
    
    @JsonProperty("project_id")
    private String projectId;
    
    @JsonProperty("component_id")
    private String componentId;
    
    @JsonProperty("payload")
    private Map<String, Object> payload;
    
    @JsonProperty("source_service")
    private String sourceService;
    
    @JsonProperty("timestamp")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    @JsonProperty("correlation_id")
    private String correlationId;
    
    @JsonProperty("metadata")
    private Map<String, String> metadata;
    
    public enum EventType {
        PROJECT_CREATED,
        PROJECT_UPDATED,
        PROJECT_DELETED,
        COMPONENT_ADDED,
        COMPONENT_UPDATED,
        COMPONENT_REMOVED,
        DEPENDENCY_CHANGED,
        VALIDATION_COMPLETED,
        ANALYSIS_COMPLETED,
        ERROR_OCCURRED
    }
}
EOF

# AnalysisEvent.java
echo "📝 Creating AnalysisEvent.java..."
cat > $MODEL_DIR/event/AnalysisEvent.java << 'EOF'
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
EOF

# Common ApiResponse wrapper
echo "📝 Creating ApiResponse.java..."
cat > $MODEL_DIR/response/ApiResponse.java << 'EOF'
package com.gigapress.mcp.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("data")
    private T data;
    
    @JsonProperty("error")
    private ErrorDetails error;
    
    @JsonProperty("timestamp")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    @JsonProperty("request_id")
    private String requestId;
    
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }
    
    public static <T> ApiResponse<T> error(String message, String code) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ErrorDetails.builder()
                        .message(message)
                        .code(code)
                        .build())
                .build();
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetails {
        private String message;
        private String code;
        private String details;
    }
}
EOF

echo "✅ Domain models and DTOs created successfully!"
echo ""
echo "📋 Created models:"
echo "  Request Models:"
echo "    - ChangeAnalysisRequest"
echo "    - ProjectGenerationRequest" 
echo "    - ComponentUpdateRequest"
echo "    - ValidationRequest"
echo ""
echo "  Response Models:"
echo "    - ChangeAnalysisResponse"
echo "    - ProjectGenerationResponse"
echo "    - ComponentUpdateResponse"
echo "    - ValidationResponse"
echo "    - ApiResponse (wrapper)"
echo ""
echo "  Domain Models:"
echo "    - Project"
echo "    - Component"
echo "    - DependencyGraph"
echo ""
echo "  Event Models:"
echo "    - ProjectEvent"
echo "    - AnalysisEvent"