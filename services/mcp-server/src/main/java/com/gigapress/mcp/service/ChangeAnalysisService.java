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
