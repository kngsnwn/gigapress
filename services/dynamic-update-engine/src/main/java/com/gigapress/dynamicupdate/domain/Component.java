package com.gigapress.dynamicupdate.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Node("Component")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Component {
    
    @Id
    @GeneratedValue
    private Long id;
    
    @Property("componentId")
    private String componentId;
    
    @Property("name")
    private String name;
    
    @Property("type")
    private ComponentType type;
    
    @Property("version")
    private String version;
    
    @Property("projectId")
    private String projectId;
    
    @Property("status")
    private ComponentStatus status;
    
    @Property("metadata")
    private String metadata; // JSON string for flexible metadata
    
    @Property("createdAt")
    private LocalDateTime createdAt;
    
    @Property("updatedAt")
    private LocalDateTime updatedAt;
    
    @Relationship(type = "DEPENDS_ON", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private Set<Dependency> dependencies = new HashSet<>();
    
    @Relationship(type = "DEPENDS_ON", direction = Relationship.Direction.INCOMING)
    @Builder.Default
    private Set<Dependency> dependents = new HashSet<>();
    
    // Add dependency
    public void addDependency(Component target, DependencyType type) {
        if (target == null || this.equals(target)) {
            throw new IllegalArgumentException("Invalid dependency target");
        }
        
        Dependency dependency = Dependency.builder()
                .source(this)
                .target(target)
                .type(type)
                .createdAt(LocalDateTime.now())
                .build();
        dependencies.add(dependency);
    }
    
    // Check if component has circular dependencies
    public boolean hasCircularDependency(Component target) {
        if (target == null || this.equals(target)) {
            return false;
        }
        return hasCircularDependency(target, new HashSet<>());
    }
    
    private boolean hasCircularDependency(Component target, Set<String> visited) {
        // Prevent infinite recursion
        if (visited.contains(this.componentId)) {
            return false;
        }
        
        // Check if target depends on this component
        if (target.componentId != null && target.componentId.equals(this.componentId)) {
            return true;
        }
        
        visited.add(this.componentId);
        
        // Check all dependencies
        if (dependencies != null) {
            for (Dependency dep : dependencies) {
                if (dep.getTarget() != null && dep.getTarget().hasCircularDependency(target, visited)) {
                    return true;
                }
            }
        }
        
        visited.remove(this.componentId);
        return false;
    }
}
