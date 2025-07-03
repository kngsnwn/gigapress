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
