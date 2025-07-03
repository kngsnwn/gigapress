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
