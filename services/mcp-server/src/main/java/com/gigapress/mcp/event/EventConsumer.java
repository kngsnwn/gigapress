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
