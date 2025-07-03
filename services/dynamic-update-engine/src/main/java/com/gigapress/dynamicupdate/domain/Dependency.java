package com.gigapress.dynamicupdate.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.*;

import java.time.LocalDateTime;

@RelationshipProperties
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dependency {
    
    @Id
    @GeneratedValue
    private Long id;
    
    @TargetNode
    private Component target;
    
    @Property("type")
    private DependencyType type;
    
    @Property("strength")
    @Builder.Default
    private DependencyStrength strength = DependencyStrength.STRONG;
    
    @Property("createdAt")
    private LocalDateTime createdAt;
    
    @Property("metadata")
    private String metadata; // JSON string for additional info
    
    // For bidirectional relationship
    private Component source;
}
