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
