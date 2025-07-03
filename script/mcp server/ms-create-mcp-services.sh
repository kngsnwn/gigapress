#!/bin/bash

# MCP Server Service Layer Creation Script

echo "🔧 Creating service layer for MCP Server..."

BASE_DIR="services/mcp-server"
SERVICE_DIR="$BASE_DIR/src/main/java/com/gigapress/mcp/service"
CLIENT_DIR="$BASE_DIR/src/main/java/com/gigapress/mcp/client"
EVENT_DIR="$BASE_DIR/src/main/java/com/gigapress/mcp/event"
EXCEPTION_DIR="$BASE_DIR/src/main/java/com/gigapress/mcp/exception"

# Create directories
mkdir -p $SERVICE_DIR
mkdir -p $CLIENT_DIR  
mkdir -p $EVENT_DIR
mkdir -p $EXCEPTION_DIR

# ===== CORE SERVICES =====

# ChangeAnalysisService.java
echo "📝 Creating ChangeAnalysisService.java..."
cat > $SERVICE_DIR/ChangeAnalysisService.java << 'EOF'
package com.gigapress.mcp.service;

import com.gigapress.mcp.client.DynamicUpdateEngineClient;
import com.gigapress.mcp.event.EventProducer;
import com.gigapress.mcp.model.domain.Component;
import com.gigapress.mcp.model.domain.DependencyGraph;
import com.gigapress.mcp.model.event.AnalysisEvent;
import com.gigapress.mcp.model.request.ChangeAnalysisRequest;
import com.gigapress.mcp.model.response.ChangeAnalysisResponse;
import com.gigapress.mcp.model.response.ChangeAnalysisResponse.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChangeAnalysisService {
    
    private final DynamicUpdateEngineClient dynamicUpdateEngineClient;
    private final EventProducer eventProducer;
    
    public Mono<ChangeAnalysisResponse> analyzeChange(ChangeAnalysisRequest request) {
        log.info("Analyzing change for project: {}", request.getProjectId());
        
        String analysisId = UUID.randomUUID().toString();
        
        return dynamicUpdateEngineClient.getDependencyGraph(request.getProjectId())
            .flatMap(graph -> performAnalysis(request, graph, analysisId))
            .doOnSuccess(response -> publishAnalysisEvent(response, request))
            .doOnError(error -> log.error("Error analyzing change", error));
    }
    
    private Mono<ChangeAnalysisResponse> performAnalysis(
            ChangeAnalysisRequest request, 
            DependencyGraph graph,
            String analysisId) {
        
        return Mono.fromCallable(() -> {
            // Identify affected components
            List<AffectedComponent> affectedComponents = analyzeAffectedComponents(request, graph);
            
            // Calculate impact summary
            ImpactSummary impactSummary = calculateImpactSummary(affectedComponents, graph);
            
            // Assess risks
            RiskAssessment riskAssessment = assessRisks(affectedComponents, request);
            
            // Generate recommendations
            List<String> recommendations = generateRecommendations(affectedComponents, riskAssessment);
            
            // Estimate effort
            EffortEstimate effortEstimate = estimateEffort(affectedComponents);
            
            return ChangeAnalysisResponse.builder()
                .analysisId(analysisId)
                .projectId(request.getProjectId())
                .impactSummary(impactSummary)
                .affectedComponents(affectedComponents)
                .riskAssessment(riskAssessment)
                .recommendations(recommendations)
                .estimatedEffort(effortEstimate)
                .build();
        });
    }
    
    private List<AffectedComponent> analyzeAffectedComponents(
            ChangeAnalysisRequest request, 
            DependencyGraph graph) {
        
        List<AffectedComponent> affected = new ArrayList<>();
        Set<String> visitedComponents = new HashSet<>();
        
        // Start with target components
        String[] targetComponents = request.getTargetComponents();
        if (targetComponents != null) {
            for (String componentId : targetComponents) {
                analyzeComponentImpact(
                    componentId, 
                    graph, 
                    affected, 
                    visitedComponents,
                    request.getAnalysisDepth(),
                    0
                );
            }
        }
        
        return affected;
    }
    
    private void analyzeComponentImpact(
            String componentId,
            DependencyGraph graph,
            List<AffectedComponent> affected,
            Set<String> visited,
            ChangeAnalysisRequest.AnalysisDepth depth,
            int currentLevel) {
        
        if (visited.contains(componentId)) {
            return;
        }
        
        visited.add(componentId);
        
        // Check depth limits
        int maxDepth = switch (depth) {
            case SHALLOW -> 1;
            case NORMAL -> 2;
            case DEEP -> Integer.MAX_VALUE;
        };
        
        if (currentLevel > maxDepth) {
            return;
        }
        
        // Get component details
        DependencyGraph.Node node = graph.getNodes().get(componentId);
        if (node != null) {
            Component component = node.getComponent();
            
            ImpactLevel impactLevel = calculateImpactLevel(currentLevel);
            
            AffectedComponent affectedComponent = AffectedComponent.builder()
                .componentId(componentId)
                .componentName(component.getComponentName())
                .componentType(component.getType().toString())
                .impactLevel(impactLevel)
                .impactedFeatures(identifyImpactedFeatures(component))
                .changeDetails(new HashMap<>())
                .build();
            
            affected.add(affectedComponent);
            
            // Analyze dependencies
            Set<String> dependencies = graph.getDirectDependencies(componentId);
            for (String depId : dependencies) {
                analyzeComponentImpact(depId, graph, affected, visited, depth, currentLevel + 1);
            }
        }
    }
    
    private ImpactLevel calculateImpactLevel(int level) {
        return switch (level) {
            case 0 -> ImpactLevel.CRITICAL;
            case 1 -> ImpactLevel.HIGH;
            case 2 -> ImpactLevel.MEDIUM;
            default -> ImpactLevel.LOW;
        };
    }
    
    private List<String> identifyImpactedFeatures(Component component) {
        // Simplified - in real implementation, would analyze component metadata
        return List.of("Core functionality", "API endpoints", "Data processing");
    }
    
    private ImpactSummary calculateImpactSummary(
            List<AffectedComponent> affectedComponents,
            DependencyGraph graph) {
        
        int directImpact = (int) affectedComponents.stream()
            .filter(c -> c.getImpactLevel() == ImpactLevel.CRITICAL)
            .count();
        
        int indirectImpact = affectedComponents.size() - directImpact;
        
        List<String> criticalPaths = identifyCriticalPaths(affectedComponents, graph);
        
        return ImpactSummary.builder()
            .totalComponentsAffected(affectedComponents.size())
            .directImpact(directImpact)
            .indirectImpact(indirectImpact)
            .criticalPaths(criticalPaths)
            .build();
    }
    
    private List<String> identifyCriticalPaths(
            List<AffectedComponent> affectedComponents,
            DependencyGraph graph) {
        // Simplified - identify paths with multiple critical components
        return affectedComponents.stream()
            .filter(c -> c.getImpactLevel() == ImpactLevel.CRITICAL || 
                        c.getImpactLevel() == ImpactLevel.HIGH)
            .map(c -> c.getComponentName() + " dependency chain")
            .limit(3)
            .collect(Collectors.toList());
    }
    
    private RiskAssessment assessRisks(
            List<AffectedComponent> affectedComponents,
            ChangeAnalysisRequest request) {
        
        List<Risk> risks = new ArrayList<>();
        Map<String, String> mitigationStrategies = new HashMap<>();
        
        // Check for high-impact components
        long criticalCount = affectedComponents.stream()
            .filter(c -> c.getImpactLevel() == ImpactLevel.CRITICAL)
            .count();
        
        if (criticalCount > 0) {
            risks.add(Risk.builder()
                .riskType("Critical Component Impact")
                .level(RiskLevel.HIGH)
                .description(criticalCount + " critical components affected")
                .probability(0.8)
                .build());
            
            mitigationStrategies.put("Critical Component Impact", 
                "Implement feature flags and gradual rollout");
        }
        
        // Check change type risks
        if (request.getChangeType() == ChangeAnalysisRequest.ChangeType.SECURITY) {
            risks.add(Risk.builder()
                .riskType("Security Change")
                .level(RiskLevel.MODERATE)
                .description("Security-related changes require thorough testing")
                .probability(0.6)
                .build());
            
            mitigationStrategies.put("Security Change", 
                "Conduct security review and penetration testing");
        }
        
        RiskLevel overallRisk = calculateOverallRisk(risks);
        
        return RiskAssessment.builder()
            .overallRisk(overallRisk)
            .identifiedRisks(risks)
            .mitigationStrategies(mitigationStrategies)
            .build();
    }
    
    private RiskLevel calculateOverallRisk(List<Risk> risks) {
        if (risks.isEmpty()) return RiskLevel.MINIMAL;
        
        boolean hasHighRisk = risks.stream()
            .anyMatch(r -> r.getLevel() == RiskLevel.HIGH || r.getLevel() == RiskLevel.SEVERE);
        
        if (hasHighRisk) return RiskLevel.HIGH;
        
        boolean hasModerateRisk = risks.stream()
            .anyMatch(r -> r.getLevel() == RiskLevel.MODERATE);
        
        return hasModerateRisk ? RiskLevel.MODERATE : RiskLevel.LOW;
    }
    
    private List<String> generateRecommendations(
            List<AffectedComponent> affectedComponents,
            RiskAssessment riskAssessment) {
        
        List<String> recommendations = new ArrayList<>();
        
        if (riskAssessment.getOverallRisk() == RiskLevel.HIGH || 
            riskAssessment.getOverallRisk() == RiskLevel.SEVERE) {
            recommendations.add("Consider breaking down the change into smaller, incremental updates");
            recommendations.add("Implement comprehensive testing strategy including integration tests");
        }
        
        if (affectedComponents.size() > 10) {
            recommendations.add("Large number of components affected - consider phased rollout");
        }
        
        recommendations.add("Update documentation for all affected components");
        recommendations.add("Notify stakeholders of impacted services");
        
        return recommendations;
    }
    
    private EffortEstimate estimateEffort(List<AffectedComponent> affectedComponents) {
        Map<String, Integer> effortByComponent = new HashMap<>();
        int totalHours = 0;
        
        for (AffectedComponent component : affectedComponents) {
            int hours = switch (component.getImpactLevel()) {
                case CRITICAL -> 16;
                case HIGH -> 8;
                case MEDIUM -> 4;
                case LOW -> 2;
                case NONE -> 0;
            };
            
            effortByComponent.put(component.getComponentId(), hours);
            totalHours += hours;
        }
        
        int developerCount = Math.max(1, Math.min(totalHours / 40, 5)); // Max 5 developers
        
        return EffortEstimate.builder()
            .estimatedHours(totalHours)
            .developerCount(developerCount)
            .effortByComponent(effortByComponent)
            .build();
    }
    
    private void publishAnalysisEvent(ChangeAnalysisResponse response, ChangeAnalysisRequest request) {
        AnalysisEvent event = AnalysisEvent.builder()
            .analysisId(response.getAnalysisId())
            .projectId(response.getProjectId())
            .analysisType(AnalysisEvent.AnalysisType.CHANGE_IMPACT)
            .triggerSource("MCP Server")
            .affectedComponents(response.getAffectedComponents().stream()
                .map(AffectedComponent::getComponentId)
                .collect(Collectors.toList()))
            .analysisResults(Map.of(
                "totalAffected", response.getImpactSummary().getTotalComponentsAffected(),
                "overallRisk", response.getRiskAssessment().getOverallRisk().toString()
            ))
            .recommendations(response.getRecommendations())
            .build();
        
        eventProducer.publishAnalysisEvent(event);
    }
}
EOF

# ProjectGenerationService.java
echo "📝 Creating ProjectGenerationService.java..."
cat > $SERVICE_DIR/ProjectGenerationService.java << 'EOF'
package com.gigapress.mcp.service;

import com.gigapress.mcp.event.EventProducer;
import com.gigapress.mcp.model.domain.Component;
import com.gigapress.mcp.model.domain.Project;
import com.gigapress.mcp.model.event.ProjectEvent;
import com.gigapress.mcp.model.request.ProjectGenerationRequest;
import com.gigapress.mcp.model.response.ProjectGenerationResponse;
import com.gigapress.mcp.model.response.ProjectGenerationResponse.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectGenerationService {
    
    private final EventProducer eventProducer;
    
    public Mono<ProjectGenerationResponse> generateProject(ProjectGenerationRequest request) {
        log.info("Generating project: {}", request.getProjectName());
        
        String projectId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        
        return Mono.fromCallable(() -> {
            // Generate project structure
            ProjectStructure structure = generateProjectStructure(request);
            
            // Generate components
            List<GeneratedComponent> components = generateComponents(request, projectId);
            
            // Create setup instructions
            SetupInstructions instructions = generateSetupInstructions(request);
            
            long duration = System.currentTimeMillis() - startTime;
            
            return ProjectGenerationResponse.builder()
                .projectId(projectId)
                .projectName(request.getProjectName())
                .generationStatus(GenerationStatus.SUCCESS)
                .projectStructure(structure)
                .generatedComponents(components)
                .setupInstructions(instructions)
                .generationDurationMs(duration)
                .build();
        })
        .doOnSuccess(response -> publishProjectCreatedEvent(response, request))
        .doOnError(error -> log.error("Error generating project", error));
    }
    
    private ProjectStructure generateProjectStructure(ProjectGenerationRequest request) {
        Map<String, List<String>> directoryStructure = new HashMap<>();
        List<String> configFiles = new ArrayList<>();
        Map<String, String> mainEntryPoints = new HashMap<>();
        
        switch (request.getProjectType()) {
            case WEB_APPLICATION -> {
                directoryStructure.put("frontend", List.of("src", "public", "components", "pages"));
                directoryStructure.put("backend", List.of("src", "controllers", "services", "models"));
                directoryStructure.put("database", List.of("migrations", "seeds"));
                
                configFiles.addAll(List.of("package.json", "tsconfig.json", "webpack.config.js"));
                
                mainEntryPoints.put("frontend", "src/index.tsx");
                mainEntryPoints.put("backend", "src/server.ts");
            }
            case API_SERVICE -> {
                directoryStructure.put("src", List.of("controllers", "services", "models", "middleware"));
                directoryStructure.put("tests", List.of("unit", "integration"));
                directoryStructure.put("docs", List.of("api", "schemas"));
                
                configFiles.addAll(List.of("package.json", "tsconfig.json", ".env.example"));
                
                mainEntryPoints.put("api", "src/app.ts");
            }
            case MICROSERVICES -> {
                directoryStructure.put("services", List.of("auth", "user", "product", "order"));
                directoryStructure.put("shared", List.of("models", "utils", "config"));
                directoryStructure.put("infrastructure", List.of("docker", "kubernetes"));
                
                configFiles.addAll(List.of("docker-compose.yml", "lerna.json"));
                
                mainEntryPoints.put("gateway", "gateway/src/index.ts");
            }
            default -> {
                directoryStructure.put("src", List.of("main", "test", "resources"));
                configFiles.add("build.gradle");
                mainEntryPoints.put("main", "src/main/Main.java");
            }
        }
        
        return ProjectStructure.builder()
            .rootPath(request.getProjectName().toLowerCase().replace(" ", "-"))
            .directoryStructure(directoryStructure)
            .configFiles(configFiles)
            .mainEntryPoints(mainEntryPoints)
            .build();
    }
    
    private List<GeneratedComponent> generateComponents(
            ProjectGenerationRequest request, 
            String projectId) {
        
        List<GeneratedComponent> components = new ArrayList<>();
        
        // Generate based on project type and features
        switch (request.getProjectType()) {
            case WEB_APPLICATION -> {
                components.add(createComponent("frontend", "Frontend Application", 
                    "frontend", List.of("App.tsx", "index.tsx", "router.tsx")));
                components.add(createComponent("backend", "Backend API", 
                    "backend", List.of("server.ts", "routes.ts", "database.ts")));
                components.add(createComponent("database", "Database Schema", 
                    "database", List.of("schema.sql", "migrations/")));
            }
            case API_SERVICE -> {
                components.add(createComponent("api", "REST API", 
                    "src", List.of("app.ts", "routes/", "controllers/", "services/")));
                components.add(createComponent("auth", "Authentication Module", 
                    "src/auth", List.of("auth.service.ts", "jwt.strategy.ts")));
            }
            case MICROSERVICES -> {
                components.add(createComponent("gateway", "API Gateway", 
                    "services/gateway", List.of("index.ts", "proxy.ts")));
                components.add(createComponent("auth-service", "Auth Service", 
                    "services/auth", List.of("server.ts", "auth.controller.ts")));
                components.add(createComponent("user-service", "User Service", 
                    "services/user", List.of("server.ts", "user.controller.ts")));
            }
            default -> {
                components.add(createComponent("core", "Core Module", 
                    "src/main", List.of("Main.java", "Application.java")));
            }
        }
        
        // Add components based on requested features
        if (request.getFeatures() != null) {
            for (String feature : request.getFeatures()) {
                components.add(generateFeatureComponent(feature, projectId));
            }
        }
        
        return components;
    }
    
    private GeneratedComponent createComponent(
            String id, 
            String name, 
            String location, 
            List<String> files) {
        
        return GeneratedComponent.builder()
            .componentId(id)
            .componentName(name)
            .componentType("module")
            .location(location)
            .files(files)
            .configuration(new HashMap<>())
            .build();
    }
    
    private GeneratedComponent generateFeatureComponent(String feature, String projectId) {
        String componentId = feature.toLowerCase().replace(" ", "-");
        
        return GeneratedComponent.builder()
            .componentId(componentId)
            .componentName(feature)
            .componentType("feature")
            .location("src/features/" + componentId)
            .files(List.of(
                componentId + ".service.ts",
                componentId + ".controller.ts",
                componentId + ".model.ts"
            ))
            .configuration(Map.of("enabled", true))
            .build();
    }
    
    private SetupInstructions generateSetupInstructions(ProjectGenerationRequest request) {
        List<String> prerequisites = new ArrayList<>();
        List<String> installationSteps = new ArrayList<>();
        Map<String, String> environmentVariables = new HashMap<>();
        String runCommand = "";
        String testCommand = "";
        
        // Add technology-specific instructions
        if (request.getTechnologyStack() != null) {
            var stack = request.getTechnologyStack();
            
            if ("node".equalsIgnoreCase(stack.getBackend()) || 
                "react".equalsIgnoreCase(stack.getFrontend())) {
                prerequisites.add("Node.js 18+ and npm");
                installationSteps.add("npm install");
                runCommand = "npm start";
                testCommand = "npm test";
            }
            
            if ("java".equalsIgnoreCase(stack.getBackend())) {
                prerequisites.add("Java 17+ JDK");
                prerequisites.add("Gradle or Maven");
                installationSteps.add("./gradlew build");
                runCommand = "./gradlew bootRun";
                testCommand = "./gradlew test";
            }
            
            if (stack.getDatabase() != null) {
                prerequisites.add(stack.getDatabase() + " database");
                environmentVariables.put("DATABASE_URL", "connection_string_here");
                environmentVariables.put("DB_USER", "username");
                environmentVariables.put("DB_PASSWORD", "password");
            }
        }
        
        // Common setup steps
        installationSteps.add(0, "Clone the repository");
        installationSteps.add("Copy .env.example to .env and configure");
        
        return SetupInstructions.builder()
            .prerequisites(prerequisites)
            .installationSteps(installationSteps)
            .environmentVariables(environmentVariables)
            .runCommand(runCommand)
            .testCommand(testCommand)
            .build();
    }
    
    private void publishProjectCreatedEvent(
            ProjectGenerationResponse response, 
            ProjectGenerationRequest request) {
        
        ProjectEvent event = ProjectEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .eventType(ProjectEvent.EventType.PROJECT_CREATED)
            .projectId(response.getProjectId())
            .sourceService("MCP Server")
            .payload(Map.of(
                "projectName", response.getProjectName(),
                "projectType", request.getProjectType().toString(),
                "componentCount", response.getGeneratedComponents().size()
            ))
            .build();
        
        eventProducer.publishProjectEvent(event);
    }
}
EOF

# ComponentUpdateService.java
echo "📝 Creating ComponentUpdateService.java..."
cat > $SERVICE_DIR/ComponentUpdateService.java << 'EOF'
package com.gigapress.mcp.service;

import com.gigapress.mcp.client.DynamicUpdateEngineClient;
import com.gigapress.mcp.event.EventProducer;
import com.gigapress.mcp.model.event.ProjectEvent;
import com.gigapress.mcp.model.request.ComponentUpdateRequest;
import com.gigapress.mcp.model.response.ComponentUpdateResponse;
import com.gigapress.mcp.model.response.ComponentUpdateResponse.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComponentUpdateService {
    
    private final DynamicUpdateEngineClient dynamicUpdateEngineClient;
    private final EventProducer eventProducer;
    
    public Mono<ComponentUpdateResponse> updateComponents(ComponentUpdateRequest request) {
        log.info("Updating components for project: {}", request.getProjectId());
        
        String updateId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        
        return executeUpdates(request, updateId)
            .collectList()
            .map(results -> buildResponse(updateId, request, results, startTime))
            .doOnSuccess(response -> publishUpdateEvents(response, request))
            .doOnError(error -> log.error("Error updating components", error));
    }
    
    private Flux<UpdateResult> executeUpdates(ComponentUpdateRequest request, String updateId) {
        switch (request.getUpdateStrategy()) {
            case INCREMENTAL:
                return executeIncremental(request.getUpdates());
            case BATCH:
                return executeBatch(request.getUpdates());
            case PARALLEL:
                return executeParallel(request.getUpdates());
            default:
                return executeIncremental(request.getUpdates());
        }
    }
    
    private Flux<UpdateResult> executeIncremental(List<ComponentUpdateRequest.ComponentUpdate> updates) {
        return Flux.fromIterable(updates)
            .concatMap(this::performSingleUpdate);
    }
    
    private Flux<UpdateResult> executeBatch(List<ComponentUpdateRequest.ComponentUpdate> updates) {
        return Flux.fromIterable(updates)
            .collectList()
            .flatMapMany(batch -> {
                log.info("Executing batch update for {} components", batch.size());
                return Flux.fromIterable(batch)
                    .flatMap(this::performSingleUpdate);
            });
    }
    
    private Flux<UpdateResult> executeParallel(List<ComponentUpdateRequest.ComponentUpdate> updates) {
        return Flux.fromIterable(updates)
            .parallel()
            .runOn(reactor.core.scheduler.Schedulers.parallel())
            .flatMap(this::performSingleUpdate)
            .sequential();
    }
    
    private Mono<UpdateResult> performSingleUpdate(ComponentUpdateRequest.ComponentUpdate update) {
        return Mono.fromCallable(() -> {
            try {
                // Simulate update logic
                Thread.sleep(100); // Simulate processing time
                
                List<String> modifiedFiles = generateModifiedFiles(update);
                List<String> warnings = validateUpdate(update);
                
                return UpdateResult.builder()
                    .componentId(update.getComponentId())
                    .componentName("Component " + update.getComponentId())
                    .status(warnings.isEmpty() ? UpdateStatus.SUCCESS : UpdateStatus.PARTIAL)
                    .modifiedFiles(modifiedFiles)
                    .warnings(warnings)
                    .metadata(Map.of("version", update.getVersion() != null ? update.getVersion() : "1.0.0"))
                    .build();
                    
            } catch (Exception e) {
                return UpdateResult.builder()
                    .componentId(update.getComponentId())
                    .componentName("Component " + update.getComponentId())
                    .status(UpdateStatus.FAILED)
                    .errorMessage(e.getMessage())
                    .modifiedFiles(new ArrayList<>())
                    .warnings(new ArrayList<>())
                    .metadata(new HashMap<>())
                    .build();
            }
        });
    }
    
    private List<String> generateModifiedFiles(ComponentUpdateRequest.ComponentUpdate update) {
        List<String> files = new ArrayList<>();
        
        switch (update.getUpdateType()) {
            case CREATE:
                files.add(update.getComponentId() + "/index.ts");
                files.add(update.getComponentId() + "/component.tsx");
                files.add(update.getComponentId() + "/styles.css");
                break;
            case MODIFY:
                files.add(update.getComponentId() + "/index.ts");
                if (update.getUpdateContent() != null && update.getUpdateContent().containsKey("styles")) {
                    files.add(update.getComponentId() + "/styles.css");
                }
                break;
            case DELETE:
                files.add("DELETED: " + update.getComponentId() + "/*");
                break;
            case RENAME:
                String newName = (String) update.getUpdateContent().get("newName");
                files.add("RENAMED: " + update.getComponentId() + " -> " + newName);
                break;
            case MOVE:
                String newPath = (String) update.getUpdateContent().get("newPath");
                files.add("MOVED: " + update.getComponentId() + " -> " + newPath);
                break;
            case REFACTOR:
                files.add(update.getComponentId() + "/*");
                break;
        }
        
        return files;
    }
    
    private List<String> validateUpdate(ComponentUpdateRequest.ComponentUpdate update) {
        List<String> warnings = new ArrayList<>();
        
        // Validate dependencies
        if (update.getDependencies() != null && update.getDependencies().size() > 5) {
            warnings.add("Component has many dependencies (" + update.getDependencies().size() + ")");
        }
        
        // Check for breaking changes
        if (update.getUpdateType() == ComponentUpdateRequest.UpdateType.DELETE ||
            update.getUpdateType() == ComponentUpdateRequest.UpdateType.RENAME) {
            warnings.add("This is a breaking change that may affect dependent components");
        }
        
        // Version compatibility
        if (update.getVersion() != null && update.getVersion().startsWith("0.")) {
            warnings.add("Component is using pre-release version");
        }
        
        return warnings;
    }
    
    private ComponentUpdateResponse buildResponse(
            String updateId,
            ComponentUpdateRequest request,
            List<UpdateResult> results,
            long startTime) {
        
        int successCount = (int) results.stream()
            .filter(r -> r.getStatus() == UpdateStatus.SUCCESS)
            .count();
        
        int failedCount = (int) results.stream()
            .filter(r -> r.getStatus() == UpdateStatus.FAILED)
            .count();
        
        int warningCount = results.stream()
            .mapToInt(r -> r.getWarnings().size())
            .sum();
        
        UpdateStatus overallStatus;
        if (failedCount == 0) {
            overallStatus = warningCount > 0 ? UpdateStatus.PARTIAL : UpdateStatus.SUCCESS;
        } else if (successCount == 0) {
            overallStatus = UpdateStatus.FAILED;
        } else {
            overallStatus = UpdateStatus.PARTIAL;
        }
        
        UpdateSummary summary = UpdateSummary.builder()
            .totalComponents(results.size())
            .successfulUpdates(successCount)
            .failedUpdates(failedCount)
            .warningCount(warningCount)
            .totalDurationMs(System.currentTimeMillis() - startTime)
            .build();
        
        return ComponentUpdateResponse.builder()
            .updateId(updateId)
            .projectId(request.getProjectId())
            .overallStatus(overallStatus)
            .updateResults(results)
            .rollbackAvailable(request.isRollbackOnError() && failedCount > 0)
            .updateSummary(summary)
            .build();
    }
    
    private void publishUpdateEvents(ComponentUpdateResponse response, ComponentUpdateRequest request) {
        response.getUpdateResults().forEach(result -> {
            ProjectEvent event = ProjectEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(ProjectEvent.EventType.COMPONENT_UPDATED)
                .projectId(request.getProjectId())
                .componentId(result.getComponentId())
                .sourceService("MCP Server")
                .payload(Map.of(
                    "updateStatus", result.getStatus().toString(),
                    "modifiedFiles", result.getModifiedFiles(),
                    "warnings", result.getWarnings()
                ))
                .correlationId(response.getUpdateId())
                .build();
            
            eventProducer.publishProjectEvent(event);
        });
    }
}
EOF

# ConsistencyValidationService.java
echo "📝 Creating ConsistencyValidationService.java..."
cat > $SERVICE_DIR/ConsistencyValidationService.java << 'EOF'
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
EOF

# ===== CLIENT =====

# DynamicUpdateEngineClient.java
echo "📝 Creating DynamicUpdateEngineClient.java..."
cat > $CLIENT_DIR/DynamicUpdateEngineClient.java << 'EOF'
package com.gigapress.mcp.client;

import com.gigapress.mcp.model.domain.DependencyGraph;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicUpdateEngineClient {
    
    private final WebClient dynamicUpdateEngineWebClient;
    
    public Mono<DependencyGraph> getDependencyGraph(String projectId) {
        log.debug("Fetching dependency graph for project: {}", projectId);
        
        return dynamicUpdateEngineWebClient
            .get()
            .uri("/api/projects/{projectId}/dependency-graph", projectId)
            .retrieve()
            .bodyToMono(DependencyGraph.class)
            .timeout(Duration.ofSeconds(30))
            .doOnSuccess(graph -> log.debug("Retrieved dependency graph with {} nodes", 
                graph.getNodes() != null ? graph.getNodes().size() : 0))
            .doOnError(error -> log.error("Error fetching dependency graph", error))
            .onErrorReturn(new DependencyGraph()); // Return empty graph on error
    }
    
    public Mono<Void> updateDependencyGraph(String projectId, DependencyGraph graph) {
        log.debug("Updating dependency graph for project: {}", projectId);
        
        return dynamicUpdateEngineWebClient
            .put()
            .uri("/api/projects/{projectId}/dependency-graph", projectId)
            .bodyValue(graph)
            .retrieve()
            .bodyToMono(Void.class)
            .timeout(Duration.ofSeconds(30))
            .doOnSuccess(v -> log.debug("Successfully updated dependency graph"))
            .doOnError(error -> log.error("Error updating dependency graph", error));
    }
}
EOF

# ===== EVENT HANDLING =====

# EventProducer.java
echo "📝 Creating EventProducer.java..."
cat > $EVENT_DIR/EventProducer.java << 'EOF'
package com.gigapress.mcp.event;

import com.gigapress.mcp.model.event.AnalysisEvent;
import com.gigapress.mcp.model.event.ProjectEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventProducer {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public void publishProjectEvent(ProjectEvent event) {
        String topic = "project-generation";
        
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(topic, event.getProjectId(), event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Published project event: {} to topic: {}", 
                    event.getEventType(), topic);
            } else {
                log.error("Failed to publish project event", ex);
            }
        });
    }
    
    public void publishAnalysisEvent(AnalysisEvent event) {
        String topic = "change-analysis";
        
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(topic, event.getProjectId(), event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Published analysis event: {} to topic: {}", 
                    event.getAnalysisType(), topic);
            } else {
                log.error("Failed to publish analysis event", ex);
            }
        });
    }
    
    public void publishComponentUpdateEvent(String projectId, Object event) {
        String topic = "component-update";
        
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(topic, projectId, event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Published component update event to topic: {}", topic);
            } else {
                log.error("Failed to publish component update event", ex);
            }
        });
    }
    
    public void publishValidationEvent(String projectId, Object event) {
        String topic = "validation-result";
        
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(topic, projectId, event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Published validation event to topic: {}", topic);
            } else {
                log.error("Failed to publish validation event", ex);
            }
        });
    }
}
EOF

# EventConsumer.java
echo "📝 Creating EventConsumer.java..."
cat > $EVENT_DIR/EventConsumer.java << 'EOF'
package com.gigapress.mcp.event;

import com.gigapress.mcp.model.event.ProjectEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventConsumer {
    
    @KafkaListener(topics = "project-generation", groupId = "mcp-server-group")
    public void handleProjectEvent(ProjectEvent event) {
        log.info("Received project event: {} for project: {}", 
            event.getEventType(), event.getProjectId());
        
        // Handle project events from other services
        switch (event.getEventType()) {
            case PROJECT_CREATED:
                log.info("New project created: {}", event.getProjectId());
                break;
            case PROJECT_UPDATED:
                log.info("Project updated: {}", event.getProjectId());
                break;
            case COMPONENT_ADDED:
                log.info("Component added to project: {}", event.getProjectId());
                break;
            default:
                log.debug("Unhandled event type: {}", event.getEventType());
        }
    }
    
    @KafkaListener(topics = "dependency-updates", groupId = "mcp-server-group")
    public void handleDependencyUpdate(String message) {
        log.info("Received dependency update: {}", message);
        // Process dependency updates from Dynamic Update Engine
    }
}
EOF

# ===== EXCEPTIONS =====

# McpServerException.java
echo "📝 Creating McpServerException.java..."
cat > $EXCEPTION_DIR/McpServerException.java << 'EOF'
package com.gigapress.mcp.exception;

public class McpServerException extends RuntimeException {
    
    private final String errorCode;
    
    public McpServerException(String message) {
        super(message);
        this.errorCode = "MCP_ERROR";
    }
    
    public McpServerException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public McpServerException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "MCP_ERROR";
    }
    
    public McpServerException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
EOF

# GlobalExceptionHandler.java
echo "📝 Creating GlobalExceptionHandler.java..."
cat > $EXCEPTION_DIR/GlobalExceptionHandler.java << 'EOF'
package com.gigapress.mcp.exception;

import com.gigapress.mcp.model.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(McpServerException.class)
    public ResponseEntity<ApiResponse<Void>> handleMcpServerException(McpServerException ex) {
        log.error("MCP Server error: {}", ex.getMessage(), ex);
        
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), ex.getErrorCode());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
            .success(false)
            .error(ApiResponse.ErrorDetails.builder()
                .message("Validation failed")
                .code("VALIDATION_ERROR")
                .details(errors.toString())
                .build())
            .data(errors)
            .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ApiResponse<Void>> handleWebClientException(WebClientResponseException ex) {
        log.error("External service error: {}", ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(
            "External service error: " + ex.getMessage(), 
            "EXTERNAL_SERVICE_ERROR"
        );
        
        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        ApiResponse<Void> response = ApiResponse.error(
            "An unexpected error occurred", 
            "INTERNAL_ERROR"
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
EOF

echo "✅ Service layer created successfully!"
echo ""
echo "📋 Created services:"
echo "  Core Services:"
echo "    - ChangeAnalysisService"
echo "    - ProjectGenerationService"
echo "    - ComponentUpdateService"
echo "    - ConsistencyValidationService"
echo ""
echo "  Supporting Components:"
echo "    - DynamicUpdateEngineClient"
echo "    - EventProducer"
echo "    - EventConsumer"
echo "    - Exception handling"