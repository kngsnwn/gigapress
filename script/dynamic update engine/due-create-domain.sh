#!/bin/bash

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${BLUE}🚀 Creating Dynamic Update Engine Domain Models and Services${NC}"
echo "======================================================="

# Base package path
BASE_PATH="src/main/java/com/gigapress/dynamicupdate"

# Create directories if they don't exist
echo -e "\n${YELLOW}📁 Creating directory structure...${NC}"
mkdir -p $BASE_PATH/{domain,repository,service,event,controller,config,dto,exception}

# 1. Create Domain Entities
echo -e "\n${YELLOW}📝 Creating domain entities...${NC}"

# Component.java
cat > $BASE_PATH/domain/Component.java << 'EOF'
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
        return hasCircularDependency(target, new HashSet<>());
    }
    
    private boolean hasCircularDependency(Component target, Set<String> visited) {
        if (visited.contains(this.componentId)) {
            return true;
        }
        
        if (this.componentId.equals(target.componentId)) {
            return true;
        }
        
        visited.add(this.componentId);
        
        for (Dependency dep : dependencies) {
            if (dep.getTarget().hasCircularDependency(target, visited)) {
                return true;
            }
        }
        
        visited.remove(this.componentId);
        return false;
    }
}
EOF

# Dependency.java
cat > $BASE_PATH/domain/Dependency.java << 'EOF'
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
EOF

# Create Enums
echo -e "\n${YELLOW}📝 Creating enum types...${NC}"

# ComponentType.java
cat > $BASE_PATH/domain/ComponentType.java << 'EOF'
package com.gigapress.dynamicupdate.domain;

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
EOF

# ComponentStatus.java
cat > $BASE_PATH/domain/ComponentStatus.java << 'EOF'
package com.gigapress.dynamicupdate.domain;

public enum ComponentStatus {
    ACTIVE,
    INACTIVE,
    UPDATING,
    ERROR,
    DEPRECATED
}
EOF

# DependencyType.java
cat > $BASE_PATH/domain/DependencyType.java << 'EOF'
package com.gigapress.dynamicupdate.domain;

public enum DependencyType {
    COMPILE,
    RUNTIME,
    TEST,
    PROVIDED,
    IMPORT,
    API_CALL,
    DATABASE,
    CONFIGURATION
}
EOF

# DependencyStrength.java
cat > $BASE_PATH/domain/DependencyStrength.java << 'EOF'
package com.gigapress.dynamicupdate.domain;

public enum DependencyStrength {
    STRONG,  // Breaking changes will affect dependent
    WEAK,    // Changes might affect dependent
    OPTIONAL // Changes unlikely to affect dependent
}
EOF

echo -e "${GREEN}✅ Domain entities created!${NC}"

# 2. Create Repository
echo -e "\n${YELLOW}📝 Creating repository...${NC}"

cat > $BASE_PATH/repository/ComponentRepository.java << 'EOF'
package com.gigapress.dynamicupdate.repository;

import com.gigapress.dynamicupdate.domain.Component;
import com.gigapress.dynamicupdate.domain.ComponentType;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ComponentRepository extends Neo4jRepository<Component, Long> {
    
    Optional<Component> findByComponentId(String componentId);
    
    List<Component> findByProjectId(String projectId);
    
    List<Component> findByType(ComponentType type);
    
    @Query("MATCH (c:Component {componentId: $componentId})-[d:DEPENDS_ON]->(dep:Component) " +
           "RETURN c, collect(d), collect(dep)")
    Optional<Component> findByComponentIdWithDependencies(@Param("componentId") String componentId);
    
    @Query("MATCH (c:Component {componentId: $componentId})<-[d:DEPENDS_ON]-(dep:Component) " +
           "RETURN dep")
    Set<Component> findDependents(@Param("componentId") String componentId);
    
    @Query("MATCH (c:Component {componentId: $componentId})-[d:DEPENDS_ON*1..]->(dep:Component) " +
           "RETURN DISTINCT dep")
    Set<Component> findAllDependencies(@Param("componentId") String componentId);
    
    @Query("MATCH (c:Component {componentId: $componentId})<-[d:DEPENDS_ON*1..]-(dep:Component) " +
           "RETURN DISTINCT dep")
    Set<Component> findAllDependents(@Param("componentId") String componentId);
    
    @Query("MATCH path = (c1:Component {componentId: $sourceId})-[d:DEPENDS_ON*]->(c2:Component {componentId: $targetId}) " +
           "RETURN path LIMIT 1")
    Optional<Object> findDependencyPath(@Param("sourceId") String sourceId, @Param("targetId") String targetId);
    
    @Query("MATCH (c:Component {componentId: $componentId})-[d:DEPENDS_ON]->(c) " +
           "RETURN c")
    Optional<Component> findCircularDependency(@Param("componentId") String componentId);
    
    @Query("MATCH (c:Component) WHERE c.projectId = $projectId " +
           "OPTIONAL MATCH (c)-[d:DEPENDS_ON]->(dep:Component) " +
           "RETURN c, collect(d), collect(dep)")
    List<Component> findProjectComponentsWithDependencies(@Param("projectId") String projectId);
}
EOF

echo -e "${GREEN}✅ Repository created!${NC}"

# 3. Create Event DTOs
echo -e "\n${YELLOW}📝 Creating event DTOs...${NC}"

# ComponentUpdateEvent.java
cat > $BASE_PATH/event/ComponentUpdateEvent.java << 'EOF'
package com.gigapress.dynamicupdate.event;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class ComponentUpdateEvent {
    private String eventId;
    private String componentId;
    private String projectId;
    private UpdateType updateType;
    private String previousVersion;
    private String newVersion;
    private Map<String, Object> changes;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private String userId;
    private String reason;
    
    public enum UpdateType {
        CREATE,
        UPDATE,
        DELETE,
        VERSION_CHANGE,
        DEPENDENCY_CHANGE,
        CONFIGURATION_CHANGE
    }
}
EOF

# DependencyChangeEvent.java
cat > $BASE_PATH/event/DependencyChangeEvent.java << 'EOF'
package com.gigapress.dynamicupdate.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gigapress.dynamicupdate.domain.DependencyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DependencyChangeEvent {
    private String eventId;
    private String sourceComponentId;
    private String targetComponentId;
    private String projectId;
    private ChangeType changeType;
    private DependencyType dependencyType;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private String metadata;
    
    public enum ChangeType {
        ADDED,
        REMOVED,
        UPDATED
    }
}
EOF

# UpdatePropagationEvent.java
cat > $BASE_PATH/event/UpdatePropagationEvent.java << 'EOF'
package com.gigapress.dynamicupdate.event;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class UpdatePropagationEvent {
    private String eventId;
    private String triggerComponentId;
    private String projectId;
    private List<String> affectedComponentIds;
    private PropagationType propagationType;
    private Map<String, Object> updateDetails;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private int propagationDepth;
    private String initiatedBy;
    
    public enum PropagationType {
        CASCADE,
        SELECTIVE,
        FORCED,
        ROLLBACK
    }
}
EOF

echo -e "${GREEN}✅ Event DTOs created!${NC}"

# 4. Create Service Classes
echo -e "\n${YELLOW}📝 Creating service classes...${NC}"

# ComponentService.java
cat > $BASE_PATH/service/ComponentService.java << 'EOF'
package com.gigapress.dynamicupdate.service;

import com.gigapress.dynamicupdate.domain.*;
import com.gigapress.dynamicupdate.event.ComponentUpdateEvent;
import com.gigapress.dynamicupdate.event.DependencyChangeEvent;
import com.gigapress.dynamicupdate.event.UpdatePropagationEvent;
import com.gigapress.dynamicupdate.repository.ComponentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComponentService {
    
    private final ComponentRepository componentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String COMPONENT_UPDATE_TOPIC = "component.changes";
    private static final String DEPENDENCY_CHANGE_TOPIC = "dependency.events";
    private static final String UPDATE_PROPAGATION_TOPIC = "update.propagation";
    
    @Transactional
    public Component createComponent(Component component) {
        component.setCreatedAt(LocalDateTime.now());
        component.setUpdatedAt(LocalDateTime.now());
        component.setStatus(ComponentStatus.ACTIVE);
        
        Component saved = componentRepository.save(component);
        
        // Publish component creation event
        publishComponentUpdate(saved, ComponentUpdateEvent.UpdateType.CREATE, null);
        
        log.info("Created component: {}", saved.getComponentId());
        return saved;
    }
    
    @Cacheable(value = "components", key = "#componentId")
    public Optional<Component> findByComponentId(String componentId) {
        return componentRepository.findByComponentIdWithDependencies(componentId);
    }
    
    public List<Component> findByProjectId(String projectId) {
        return componentRepository.findByProjectId(projectId);
    }
    
    @Transactional
    @CacheEvict(value = "components", allEntries = true)
    public Component updateComponent(String componentId, Map<String, Object> updates) {
        Component component = componentRepository.findByComponentId(componentId)
                .orElseThrow(() -> new RuntimeException("Component not found: " + componentId));
        
        String previousVersion = component.getVersion();
        
        // Apply updates
        if (updates.containsKey("version")) {
            component.setVersion((String) updates.get("version"));
        }
        if (updates.containsKey("status")) {
            component.setStatus(ComponentStatus.valueOf((String) updates.get("status")));
        }
        if (updates.containsKey("metadata")) {
            component.setMetadata((String) updates.get("metadata"));
        }
        
        component.setUpdatedAt(LocalDateTime.now());
        Component saved = componentRepository.save(component);
        
        // Publish update event
        publishComponentUpdate(saved, ComponentUpdateEvent.UpdateType.UPDATE, previousVersion);
        
        // Check if update requires propagation
        if (shouldPropagateUpdate(updates)) {
            propagateUpdate(saved, updates);
        }
        
        return saved;
    }
    
    @Transactional
    @CacheEvict(value = "components", allEntries = true)
    public void addDependency(String sourceId, String targetId, DependencyType type) {
        Component source = componentRepository.findByComponentId(sourceId)
                .orElseThrow(() -> new RuntimeException("Source component not found: " + sourceId));
        Component target = componentRepository.findByComponentId(targetId)
                .orElseThrow(() -> new RuntimeException("Target component not found: " + targetId));
        
        // Check for circular dependency
        if (target.hasCircularDependency(source)) {
            throw new RuntimeException("Circular dependency detected");
        }
        
        source.addDependency(target, type);
        componentRepository.save(source);
        
        // Publish dependency change event
        publishDependencyChange(sourceId, targetId, type, DependencyChangeEvent.ChangeType.ADDED);
        
        log.info("Added dependency: {} -> {}", sourceId, targetId);
    }
    
    @Cacheable(value = "dependencies", key = "#componentId")
    public Set<Component> getDirectDependencies(String componentId) {
        return componentRepository.findByComponentIdWithDependencies(componentId)
                .map(component -> component.getDependencies().stream()
                        .map(Dependency::getTarget)
                        .collect(Collectors.toSet()))
                .orElse(new HashSet<>());
    }
    
    @Cacheable(value = "dependents", key = "#componentId")
    public Set<Component> getDirectDependents(String componentId) {
        return componentRepository.findDependents(componentId);
    }
    
    public Set<Component> getAllAffectedComponents(String componentId) {
        Set<Component> affected = new HashSet<>();
        Set<String> visited = new HashSet<>();
        
        collectAffectedComponents(componentId, affected, visited);
        
        return affected;
    }
    
    private void collectAffectedComponents(String componentId, Set<Component> affected, Set<String> visited) {
        if (visited.contains(componentId)) {
            return;
        }
        
        visited.add(componentId);
        Set<Component> dependents = componentRepository.findDependents(componentId);
        
        for (Component dependent : dependents) {
            affected.add(dependent);
            collectAffectedComponents(dependent.getComponentId(), affected, visited);
        }
    }
    
    private void propagateUpdate(Component component, Map<String, Object> updates) {
        Set<Component> affected = getAllAffectedComponents(component.getComponentId());
        
        if (!affected.isEmpty()) {
            UpdatePropagationEvent event = UpdatePropagationEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .triggerComponentId(component.getComponentId())
                    .projectId(component.getProjectId())
                    .affectedComponentIds(affected.stream()
                            .map(Component::getComponentId)
                            .collect(Collectors.toList()))
                    .propagationType(UpdatePropagationEvent.PropagationType.CASCADE)
                    .updateDetails(updates)
                    .timestamp(LocalDateTime.now())
                    .propagationDepth(calculatePropagationDepth(component.getComponentId(), affected))
                    .build();
            
            kafkaTemplate.send(UPDATE_PROPAGATION_TOPIC, event);
            log.info("Propagating update from {} to {} components", component.getComponentId(), affected.size());
        }
    }
    
    private boolean shouldPropagateUpdate(Map<String, Object> updates) {
        // Define rules for when updates should propagate
        return updates.containsKey("version") || 
               updates.containsKey("status") ||
               updates.containsKey("breaking_change");
    }
    
    private int calculatePropagationDepth(String componentId, Set<Component> affected) {
        // Calculate maximum depth of propagation
        int maxDepth = 0;
        for (Component component : affected) {
            Optional<Object> path = componentRepository.findDependencyPath(component.getComponentId(), componentId);
            if (path.isPresent()) {
                // Parse path length (simplified)
                maxDepth = Math.max(maxDepth, 1); // Placeholder
            }
        }
        return maxDepth;
    }
    
    private void publishComponentUpdate(Component component, ComponentUpdateEvent.UpdateType type, String previousVersion) {
        ComponentUpdateEvent event = ComponentUpdateEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .componentId(component.getComponentId())
                .projectId(component.getProjectId())
                .updateType(type)
                .previousVersion(previousVersion)
                .newVersion(component.getVersion())
                .timestamp(LocalDateTime.now())
                .build();
        
        kafkaTemplate.send(COMPONENT_UPDATE_TOPIC, event);
    }
    
    private void publishDependencyChange(String sourceId, String targetId, DependencyType type, DependencyChangeEvent.ChangeType changeType) {
        DependencyChangeEvent event = DependencyChangeEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .sourceComponentId(sourceId)
                .targetComponentId(targetId)
                .changeType(changeType)
                .dependencyType(type)
                .timestamp(LocalDateTime.now())
                .build();
        
        kafkaTemplate.send(DEPENDENCY_CHANGE_TOPIC, event);
    }
}
EOF

# UpdatePropagationService.java
cat > $BASE_PATH/service/UpdatePropagationService.java << 'EOF'
package com.gigapress.dynamicupdate.service;

import com.gigapress.dynamicupdate.event.ComponentUpdateEvent;
import com.gigapress.dynamicupdate.event.UpdatePropagationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdatePropagationService {
    
    private final ComponentService componentService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public void analyzeAndPropagateChanges(ComponentUpdateEvent event) {
        log.info("Analyzing changes for component: {}", event.getComponentId());
        
        // Get affected components
        Set<com.gigapress.dynamicupdate.domain.Component> affected = 
                componentService.getAllAffectedComponents(event.getComponentId());
        
        if (!affected.isEmpty()) {
            // Create propagation event
            UpdatePropagationEvent propagationEvent = UpdatePropagationEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .triggerComponentId(event.getComponentId())
                    .projectId(event.getProjectId())
                    .affectedComponentIds(affected.stream()
                            .map(com.gigapress.dynamicupdate.domain.Component::getComponentId)
                            .toList())
                    .propagationType(determinePropagationType(event))
                    .updateDetails(event.getChanges())
                    .timestamp(LocalDateTime.now())
                    .initiatedBy(event.getUserId())
                    .build();
            
            // Send propagation event
            kafkaTemplate.send("update.propagation", propagationEvent);
            
            log.info("Propagated changes to {} components", affected.size());
        }
    }
    
    private UpdatePropagationEvent.PropagationType determinePropagationType(ComponentUpdateEvent event) {
        // Determine propagation type based on update type and changes
        if (event.getUpdateType() == ComponentUpdateEvent.UpdateType.VERSION_CHANGE) {
            return UpdatePropagationEvent.PropagationType.CASCADE;
        } else if (event.getChanges().containsKey("breaking_change")) {
            return UpdatePropagationEvent.PropagationType.FORCED;
        }
        return UpdatePropagationEvent.PropagationType.SELECTIVE;
    }
}
EOF

echo -e "${GREEN}✅ Service classes created!${NC}"

# 5. Create Event Listener
echo -e "\n${YELLOW}📝 Creating event listener...${NC}"

cat > $BASE_PATH/event/UpdateEventListener.java << 'EOF'
package com.gigapress.dynamicupdate.event;

import com.gigapress.dynamicupdate.service.ComponentService;
import com.gigapress.dynamicupdate.service.UpdatePropagationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateEventListener {
    
    private final ComponentService componentService;
    private final UpdatePropagationService propagationService;
    
    @KafkaListener(topics = "project.updates", groupId = "update-engine-group")
    public void handleProjectUpdate(@Payload ComponentUpdateEvent event,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   Acknowledgment acknowledgment) {
        try {
            log.info("Received project update event: {} from topic: {}", event.getEventId(), topic);
            
            // Process the update based on update type
            switch (event.getUpdateType()) {
                case CREATE:
                    handleComponentCreation(event);
                    break;
                case UPDATE:
                case VERSION_CHANGE:
                    handleComponentUpdate(event);
                    break;
                case DELETE:
                    handleComponentDeletion(event);
                    break;
                case DEPENDENCY_CHANGE:
                    handleDependencyChange(event);
                    break;
                default:
                    log.warn("Unknown update type: {}", event.getUpdateType());
            }
            
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing project update event: {}", event.getEventId(), e);
            // Implement retry logic or dead letter queue handling
        }
    }
    
    @KafkaListener(topics = "generation.requests", groupId = "update-engine-group")
    public void handleGenerationRequest(@Payload GenerationRequestEvent event,
                                       Acknowledgment acknowledgment) {
        try {
            log.info("Received generation request: {}", event.getRequestId());
            
            // Process generation request
            if (event.getComponents() != null) {
                for (ComponentDefinition componentDef : event.getComponents()) {
                    processComponentDefinition(event.getProjectId(), componentDef);
                }
            }
            
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing generation request: {}", event.getRequestId(), e);
        }
    }
    
    @KafkaListener(topics = "validation.results", groupId = "update-engine-group")
    public void handleValidationResult(@Payload ValidationResultEvent event,
                                      Acknowledgment acknowledgment) {
        try {
            log.info("Received validation result for component: {}", event.getComponentId());
            
            if (!event.isValid()) {
                // Handle validation failure
                handleValidationFailure(event);
            }
            
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing validation result: {}", event.getComponentId(), e);
        }
    }
    
    private void handleComponentCreation(ComponentUpdateEvent event) {
        log.debug("Handling component creation: {}", event.getComponentId());
        // Additional logic for component creation if needed
    }
    
    private void handleComponentUpdate(ComponentUpdateEvent event) {
        log.debug("Handling component update: {}", event.getComponentId());
        
        // Check if this update affects other components
        if (event.getChanges() != null && !event.getChanges().isEmpty()) {
            propagationService.analyzeAndPropagateChanges(event);
        }
    }
    
    private void handleComponentDeletion(ComponentUpdateEvent event) {
        log.debug("Handling component deletion: {}", event.getComponentId());
        
        // Check dependencies before deletion
        var dependents = componentService.getDirectDependents(event.getComponentId());
        if (!dependents.isEmpty()) {
            log.warn("Cannot delete component {} - has {} dependents", 
                    event.getComponentId(), dependents.size());
            // Publish deletion failure event
        }
    }
    
    private void handleDependencyChange(ComponentUpdateEvent event) {
        log.debug("Handling dependency change for component: {}", event.getComponentId());
        // Process dependency changes
    }
    
    private void processComponentDefinition(String projectId, ComponentDefinition definition) {
        log.debug("Processing component definition: {} for project: {}", 
                definition.getName(), projectId);
        // Convert definition to Component entity and save
    }
    
    private void handleValidationFailure(ValidationResultEvent event) {
        log.warn("Validation failed for component: {} - Errors: {}", 
                event.getComponentId(), event.getErrors());
        // Update component status or trigger rollback
    }
}

// Supporting event classes
@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
class GenerationRequestEvent {
    private String requestId;
    private String projectId;
    private List<ComponentDefinition> components;
    private String userId;
    private LocalDateTime timestamp;
}

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
class ComponentDefinition {
    private String name;
    private String type;
    private Map<String, Object> configuration;
    private List<String> dependencies;
}

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
class ValidationResultEvent {
    private String componentId;
    private boolean valid;
    private List<String> errors;
    private LocalDateTime timestamp;
}
EOF

echo -e "${GREEN}✅ Event listener created!${NC}"

# 6. Create Configuration Classes
echo -e "\n${YELLOW}📝 Creating configuration classes...${NC}"

# KafkaConfig.java
cat > $BASE_PATH/config/KafkaConfig.java << 'EOF'
package com.gigapress.dynamicupdate.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;
    
    // Producer Configuration
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configs.put(ProducerConfig.ACKS_CONFIG, "all");
        configs.put(ProducerConfig.RETRIES_CONFIG, 3);
        configs.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        configs.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        return new DefaultKafkaProducerFactory<>(configs);
    }
    
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
    
    // Consumer Configuration
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        configs.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configs.put(JsonDeserializer.VALUE_DEFAULT_TYPE, Object.class);
        
        return new DefaultKafkaConsumerFactory<>(configs);
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        
        return factory;
    }
    
    // Topic Creation
    @Bean
    public NewTopic componentChangesTopic() {
        return TopicBuilder.name("component.changes")
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic dependencyEventsTopic() {
        return TopicBuilder.name("dependency.events")
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic updatePropagationTopic() {
        return TopicBuilder.name("update.propagation")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
EOF

# RedisConfig.java
cat > $BASE_PATH/config/RedisConfig.java << 'EOF'
package com.gigapress.dynamicupdate.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {
    
    @Value("${spring.data.redis.host}")
    private String redisHost;
    
    @Value("${spring.data.redis.port}")
    private int redisPort;
    
    @Value("${spring.data.redis.password}")
    private String redisPassword;
    
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost);
        redisConfig.setPort(redisPort);
        redisConfig.setPassword(redisPassword);
        
        return new LettuceConnectionFactory(redisConfig);
    }
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Use JSON serializer for values
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .prefixCacheNameWith("gigapress:");
        
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfig)
                .withCacheConfiguration("components", 
                    RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(15)))
                .withCacheConfiguration("dependencies",
                    RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration("dependents",
                    RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(30)))
                .build();
    }
}
EOF

echo -e "${GREEN}✅ Configuration classes created!${NC}"

# 7. Create Controller
echo -e "\n${YELLOW}📝 Creating REST controller...${NC}"

cat > $BASE_PATH/controller/ComponentController.java << 'EOF'
package com.gigapress.dynamicupdate.controller;

import com.gigapress.dynamicupdate.domain.Component;
import com.gigapress.dynamicupdate.domain.ComponentType;
import com.gigapress.dynamicupdate.domain.DependencyType;
import com.gigapress.dynamicupdate.dto.ComponentRequest;
import com.gigapress.dynamicupdate.dto.DependencyRequest;
import com.gigapress.dynamicupdate.dto.UpdateRequest;
import com.gigapress.dynamicupdate.service.ComponentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/components")
@RequiredArgsConstructor
public class ComponentController {
    
    private final ComponentService componentService;
    
    @PostMapping
    public ResponseEntity<Component> createComponent(@Valid @RequestBody ComponentRequest request) {
        log.info("Creating component: {}", request.getName());
        
        Component component = Component.builder()
                .componentId(request.getComponentId())
                .name(request.getName())
                .type(request.getType())
                .version(request.getVersion())
                .projectId(request.getProjectId())
                .metadata(request.getMetadata())
                .build();
        
        Component created = componentService.createComponent(component);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping("/{componentId}")
    public ResponseEntity<Component> getComponent(@PathVariable String componentId) {
        return componentService.findByComponentId(componentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{componentId}")
    public ResponseEntity<Component> updateComponent(
            @PathVariable String componentId,
            @Valid @RequestBody UpdateRequest request) {
        
        log.info("Updating component: {}", componentId);
        Component updated = componentService.updateComponent(componentId, request.getUpdates());
        return ResponseEntity.ok(updated);
    }
    
    @PostMapping("/{componentId}/dependencies")
    public ResponseEntity<Void> addDependency(
            @PathVariable String componentId,
            @Valid @RequestBody DependencyRequest request) {
        
        log.info("Adding dependency: {} -> {}", componentId, request.getTargetComponentId());
        componentService.addDependency(
                componentId, 
                request.getTargetComponentId(), 
                request.getType()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @GetMapping("/{componentId}/dependencies")
    public ResponseEntity<Set<Component>> getDependencies(@PathVariable String componentId) {
        Set<Component> dependencies = componentService.getDirectDependencies(componentId);
        return ResponseEntity.ok(dependencies);
    }
    
    @GetMapping("/{componentId}/dependents")
    public ResponseEntity<Set<Component>> getDependents(@PathVariable String componentId) {
        Set<Component> dependents = componentService.getDirectDependents(componentId);
        return ResponseEntity.ok(dependents);
    }
    
    @GetMapping("/{componentId}/impact-analysis")
    public ResponseEntity<Set<Component>> getImpactAnalysis(@PathVariable String componentId) {
        Set<Component> affected = componentService.getAllAffectedComponents(componentId);
        return ResponseEntity.ok(affected);
    }
    
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Component>> getProjectComponents(@PathVariable String projectId) {
        List<Component> components = componentService.findByProjectId(projectId);
        return ResponseEntity.ok(components);
    }
}
EOF

echo -e "${GREEN}✅ Controller created!${NC}"

# 8. Create DTOs
echo -e "\n${YELLOW}📝 Creating DTOs...${NC}"

# ComponentRequest.java
cat > $BASE_PATH/dto/ComponentRequest.java << 'EOF'
package com.gigapress.dynamicupdate.dto;

import com.gigapress.dynamicupdate.domain.ComponentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComponentRequest {
    @NotBlank
    private String componentId;
    
    @NotBlank
    private String name;
    
    @NotNull
    private ComponentType type;
    
    @NotBlank
    private String version;
    
    @NotBlank
    private String projectId;
    
    private String metadata;
}
EOF

# DependencyRequest.java
cat > $BASE_PATH/dto/DependencyRequest.java << 'EOF'
package com.gigapress.dynamicupdate.dto;

import com.gigapress.dynamicupdate.domain.DependencyType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DependencyRequest {
    @NotBlank
    private String targetComponentId;
    
    @NotNull
    private DependencyType type;
    
    private String metadata;
}
EOF

# UpdateRequest.java
cat > $BASE_PATH/dto/UpdateRequest.java << 'EOF'
package com.gigapress.dynamicupdate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRequest {
    @NotNull
    private Map<String, Object> updates;
}
EOF

echo -e "${GREEN}✅ DTOs created!${NC}"

# Final summary
echo -e "\n${BLUE}========================================${NC}"
echo -e "${GREEN}✨ Domain models and services created successfully!${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo "📁 Created files:"
echo "  Domain:"
echo "    - Component.java"
echo "    - Dependency.java"
echo "    - ComponentType.java"
echo "    - ComponentStatus.java"
echo "    - DependencyType.java"
echo "    - DependencyStrength.java"
echo ""
echo "  Repository:"
echo "    - ComponentRepository.java"
echo ""
echo "  Service:"
echo "    - ComponentService.java"
echo "    - UpdatePropagationService.java"
echo ""
echo "  Event:"
echo "    - ComponentUpdateEvent.java"
echo "    - DependencyChangeEvent.java"
echo "    - UpdatePropagationEvent.java"
echo "    - UpdateEventListener.java"
echo ""
echo "  Controller:"
echo "    - ComponentController.java"
echo ""
echo "  Config:"
echo "    - KafkaConfig.java"
echo "    - RedisConfig.java"
echo ""
echo "  DTOs:"
echo "    - ComponentRequest.java"
echo "    - DependencyRequest.java"
echo "    - UpdateRequest.java"
echo ""
echo "🚀 Next steps:"
echo "  1. Generate Gradle wrapper: gradle wrapper --gradle-version 8.5"
echo "  2. Build the project: ./gradlew build"
echo "  3. Run the application: ./gradlew bootRun"