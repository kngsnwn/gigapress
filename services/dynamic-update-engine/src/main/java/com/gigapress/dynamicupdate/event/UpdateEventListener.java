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
