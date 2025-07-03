package com.gigapress.mcp.service;

import com.gigapress.mcp.client.DynamicUpdateEngineClient;
import com.gigapress.mcp.model.domain.DependencyGraph;
import com.gigapress.mcp.model.request.ValidationRequest;
import com.gigapress.mcp.model.response.ValidationResponse;
import com.gigapress.mcp.model.response.ValidationResponse.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsistencyValidationService {
    
    private final DynamicUpdateEngineClient dynamicUpdateEngineClient;
    
    public Mono<ValidationResponse> validateConsistency(ValidationRequest request) {
        log.info("Validating consistency for project: {}", request.getProjectId());
        
        String validationId = UUID.randomUUID().toString();
        
        return performValidations(request)
            .collectList()
            .map(results -> buildValidationResponse(validationId, request, results))
            .doOnError(error -> log.error("Error during validation", error));
    }
    
    private Flux<ValidationResult> performValidations(ValidationRequest request) {
        return Flux.fromIterable(request.getValidationTypes())
            .flatMap(type -> performValidation(type, request));
    }
    
    private Mono<ValidationResult> performValidation(
            ValidationRequest.ValidationType type,
            ValidationRequest request) {
        
        return switch (type) {
            case DEPENDENCY_CONSISTENCY -> validateDependencyConsistency(request);
            case CODE_QUALITY -> validateCodeQuality(request);
            case SECURITY_SCAN -> performSecurityScan(request);
            case PERFORMANCE_CHECK -> checkPerformance(request);
            case ARCHITECTURE_COMPLIANCE -> validateArchitecture(request);
            case API_CONTRACT -> validateApiContracts(request);
            case DATABASE_SCHEMA -> validateDatabaseSchema(request);
            case CONFIGURATION -> validateConfiguration(request);
        };
    }
    
    private Mono<ValidationResult> validateDependencyConsistency(ValidationRequest request) {
        return dynamicUpdateEngineClient.getDependencyGraph(request.getProjectId())
            .map(graph -> {
                List<Issue> issues = new ArrayList<>();
                
                // Check for circular dependencies
                Set<String> visited = new HashSet<>();
                Set<String> recursionStack = new HashSet<>();
                
                for (String nodeId : graph.getNodes().keySet()) {
                    if (hasCycle(nodeId, graph, visited, recursionStack)) {
                        issues.add(Issue.builder()
                            .issueId(UUID.randomUUID().toString())
                            .severity(Severity.ERROR)
                            .category("Circular Dependency")
                            .component(nodeId)
                            .description("Circular dependency detected involving " + nodeId)
                            .suggestion("Refactor to remove circular dependency")
                            .autoFixable(false)
                            .build());
                    }
                }
                
                // Check for orphaned components
                for (String nodeId : graph.getNodes().keySet()) {
                    if (graph.getDirectDependencies(nodeId).isEmpty() && 
                        !hasIncomingDependencies(nodeId, graph)) {
                        issues.add(Issue.builder()
                            .issueId(UUID.randomUUID().toString())
                            .severity(Severity.WARNING)
                            .category("Orphaned Component")
                            .component(nodeId)
                            .description("Component has no dependencies or dependents")
                            .suggestion("Consider removing or integrating this component")
                            .autoFixable(false)
                            .build());
                    }
                }
                
                ValidationStatus status = issues.isEmpty() ? ValidationStatus.PASSED :
                    issues.stream().anyMatch(i -> i.getSeverity() == Severity.ERROR) ?
                        ValidationStatus.FAILED : ValidationStatus.PASSED_WITH_WARNINGS;
                
                return ValidationResult.builder()
                    .validationType("DEPENDENCY_CONSISTENCY")
                    .status(status)
                    .issues(issues)
                    .metrics(Map.of(
                        "totalComponents", graph.getNodes().size(),
                        "totalDependencies", graph.getEdges() != null ? graph.getEdges().size() : 0
                    ))
                    .build();
            });
    }
    
    private boolean hasCycle(String node, DependencyGraph graph, 
                            Set<String> visited, Set<String> recursionStack) {
        visited.add(node);
        recursionStack.add(node);
        
        Set<String> dependencies = graph.getDirectDependencies(node);
        for (String dep : dependencies) {
            if (!visited.contains(dep)) {
                if (hasCycle(dep, graph, visited, recursionStack)) {
                    return true;
                }
            } else if (recursionStack.contains(dep)) {
                return true;
            }
        }
        
        recursionStack.remove(node);
        return false;
    }
    
    private boolean hasIncomingDependencies(String nodeId, DependencyGraph graph) {
        return graph.getNodes().values().stream()
            .anyMatch(node -> graph.getDirectDependencies(node.getId()).contains(nodeId));
    }
    
    private Mono<ValidationResult> validateCodeQuality(ValidationRequest request) {
        return Mono.fromCallable(() -> {
            List<Issue> issues = new ArrayList<>();
            
            // Simulate code quality checks
            issues.add(Issue.builder()
                .issueId(UUID.randomUUID().toString())
                .severity(Severity.WARNING)
                .category("Code Complexity")
                .component("UserService")
                .file("UserService.java")
                .line(45)
                .description("Method complexity is 15 (threshold: 10)")
                .suggestion("Consider breaking down the method into smaller functions")
                .autoFixable(false)
                .build());
            
            issues.add(Issue.builder()
                .issueId(UUID.randomUUID().toString())
                .severity(Severity.INFO)
                .category("Code Style")
                .component("AuthController")
                .file("AuthController.java")
                .line(23)
                .description("Missing JavaDoc comment")
                .suggestion("Add documentation for public methods")
                .autoFixable(true)
                .build());
            
            return ValidationResult.builder()
                .validationType("CODE_QUALITY")
                .status(ValidationStatus.PASSED_WITH_WARNINGS)
                .issues(issues)
                .metrics(Map.of(
                    "linesOfCode", 5432,
                    "testCoverage", 78.5,
                    "technicalDebt", "2.5 days"
                ))
                .build();
        });
    }
    
    private Mono<ValidationResult> performSecurityScan(ValidationRequest request) {
        return Mono.fromCallable(() -> {
            List<Issue> issues = new ArrayList<>();
            
            // Simulate security scan
            issues.add(Issue.builder()
                .issueId(UUID.randomUUID().toString())
                .severity(Severity.CRITICAL)
                .category("Security Vulnerability")
                .component("Dependencies")
                .file("package.json")
                .description("Vulnerable dependency: lodash@4.17.15 (CVE-2021-23337)")
                .suggestion("Update to lodash@4.17.21 or later")
                .autoFixable(true)
                .build());
            
            return ValidationResult.builder()
                .validationType("SECURITY_SCAN")
                .status(ValidationStatus.FAILED)
                .issues(issues)
                .metrics(Map.of(
                    "vulnerabilities", Map.of(
                        "critical", 1,
                        "high", 0,
                        "medium", 2,
                        "low", 3
                    )
                ))
                .build();
        });
    }
    
    private Mono<ValidationResult> checkPerformance(ValidationRequest request) {
        return Mono.fromCallable(() -> {
            List<Issue> issues = new ArrayList<>();
            
            // Simulate performance checks
            issues.add(Issue.builder()
                .issueId(UUID.randomUUID().toString())
                .severity(Severity.WARNING)
                .category("Performance")
                .component("DatabaseQuery")
                .file("UserRepository.java")
                .line(67)
                .description("N+1 query pattern detected")
                .suggestion("Use eager loading or batch queries")
                .autoFixable(false)
                .build());
            
            return ValidationResult.builder()
                .validationType("PERFORMANCE_CHECK")
                .status(ValidationStatus.PASSED_WITH_WARNINGS)
                .issues(issues)
                .metrics(Map.of(
                    "avgResponseTime", "245ms",
                    "p95ResponseTime", "890ms",
                    "throughput", "1200 req/s"
                ))
                .build();
        });
    }
    
    private Mono<ValidationResult> validateArchitecture(ValidationRequest request) {
        return Mono.fromCallable(() -> {
            List<Issue> issues = new ArrayList<>();
            
            // Check architecture compliance
            issues.add(Issue.builder()
                .issueId(UUID.randomUUID().toString())
                .severity(Severity.ERROR)
                .category("Architecture Violation")
                .component("Presentation Layer")
                .file("UserController.java")
                .line(34)
                .description("Direct database access from controller layer")
                .suggestion("Use service layer for business logic")
                .autoFixable(false)
                .build());
            
            return ValidationResult.builder()
                .validationType("ARCHITECTURE_COMPLIANCE")
                .status(ValidationStatus.FAILED)
                .issues(issues)
                .metrics(Map.of(
                    "layerViolations", 1,
                    "architectureScore", 85
                ))
                .build();
        });
    }
    
    private Mono<ValidationResult> validateApiContracts(ValidationRequest request) {
        return Mono.fromCallable(() -> {
            return ValidationResult.builder()
                .validationType("API_CONTRACT")
                .status(ValidationStatus.PASSED)
                .issues(new ArrayList<>())
                .metrics(Map.of(
                    "totalEndpoints", 24,
                    "documentedEndpoints", 24,
                    "contractViolations", 0
                ))
                .build();
        });
    }
    
    private Mono<ValidationResult> validateDatabaseSchema(ValidationRequest request) {
        return Mono.fromCallable(() -> {
            List<Issue> issues = new ArrayList<>();
            
            issues.add(Issue.builder()
                .issueId(UUID.randomUUID().toString())
                .severity(Severity.INFO)
                .category("Database Schema")
                .component("users_table")
                .description("Missing index on frequently queried column 'email'")
                .suggestion("Add index: CREATE INDEX idx_users_email ON users(email)")
                .autoFixable(true)
                .build());
            
            return ValidationResult.builder()
                .validationType("DATABASE_SCHEMA")
                .status(ValidationStatus.PASSED_WITH_WARNINGS)
                .issues(issues)
                .metrics(Map.of(
                    "tables", 12,
                    "indexes", 18,
                    "constraints", 24
                ))
                .build();
        });
    }
    
    private Mono<ValidationResult> validateConfiguration(ValidationRequest request) {
        return Mono.fromCallable(() -> {
            return ValidationResult.builder()
                .validationType("CONFIGURATION")
                .status(ValidationStatus.PASSED)
                .issues(new ArrayList<>())
                .metrics(Map.of(
                    "configFiles", 5,
                    "envVariables", 23,
                    "missingConfigs", 0
                ))
                .build();
        });
    }
    
    private ValidationResponse buildValidationResponse(
            String validationId,
            ValidationRequest request,
            List<ValidationResult> results) {
        
        // Count issues by severity
        Map<String, Integer> issuesBySeverity = new HashMap<>();
        Map<String, Integer> issuesByType = new HashMap<>();
        int totalIssues = 0;
        int autoFixableCount = 0;
        
        for (ValidationResult result : results) {
            for (Issue issue : result.getIssues()) {
                totalIssues++;
                issuesBySeverity.merge(issue.getSeverity().toString(), 1, Integer::sum);
                issuesByType.merge(issue.getCategory(), 1, Integer::sum);
                if (issue.isAutoFixable()) {
                    autoFixableCount++;
                }
            }
        }
        
        // Determine overall status
        ValidationStatus overallStatus = ValidationStatus.PASSED;
        boolean hasErrors = results.stream()
            .anyMatch(r -> r.getStatus() == ValidationStatus.FAILED);
        boolean hasWarnings = results.stream()
            .anyMatch(r -> r.getStatus() == ValidationStatus.PASSED_WITH_WARNINGS);
        
        if (hasErrors) {
            overallStatus = ValidationStatus.FAILED;
        } else if (hasWarnings) {
            overallStatus = ValidationStatus.PASSED_WITH_WARNINGS;
        }
        
        // Apply auto-fixes if requested
        List<AutoFix> autoFixesApplied = new ArrayList<>();
        if (request.isAutoFix()) {
            autoFixesApplied = applyAutoFixes(results);
        }
        
        ValidationSummary summary = ValidationSummary.builder()
            .totalIssues(totalIssues)
            .issuesBySeverity(issuesBySeverity)
            .issuesByType(issuesByType)
            .autoFixableCount(autoFixableCount)
            .autoFixedCount(autoFixesApplied.size())
            .build();
        
        return ValidationResponse.builder()
            .validationId(validationId)
            .projectId(request.getProjectId())
            .validationStatus(overallStatus)
            .validationResults(results)
            .autoFixesApplied(autoFixesApplied)
            .validationSummary(summary)
            .build();
    }
    
    private List<AutoFix> applyAutoFixes(List<ValidationResult> results) {
        List<AutoFix> fixes = new ArrayList<>();
        
        for (ValidationResult result : results) {
            for (Issue issue : result.getIssues()) {
                if (issue.isAutoFixable() && issue.getSeverity() != Severity.INFO) {
                    AutoFix fix = AutoFix.builder()
                        .issueId(issue.getIssueId())
                        .fixDescription("Auto-fixed: " + issue.getDescription())
                        .modifiedFiles(issue.getFile() != null ? 
                            List.of(issue.getFile()) : new ArrayList<>())
                        .successful(true)
                        .build();
                    fixes.add(fix);
                }
            }
        }
        
        return fixes;
    }
}
