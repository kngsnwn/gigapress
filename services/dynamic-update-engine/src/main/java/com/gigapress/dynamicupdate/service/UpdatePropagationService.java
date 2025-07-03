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
