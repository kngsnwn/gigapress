package com.gigapress.domainschema.integration.kafka.producer;

import com.gigapress.domainschema.domain.common.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DomainEventProducer {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String PROJECT_EVENTS_TOPIC = "project-events";
    private static final String DOMAIN_ANALYZED_TOPIC = "domain-analyzed";
    private static final String SCHEMA_GENERATED_TOPIC = "schema-generated";
    
    public void publishProjectCreatedEvent(ProjectCreatedEvent event) {
        log.info("Publishing project created event for project: {}", event.getAggregateId());
        kafkaTemplate.send(PROJECT_EVENTS_TOPIC, event.getAggregateId(), event);
    }
    
    public void publishRequirementsAnalyzedEvent(RequirementsAnalyzedEvent event) {
        log.info("Publishing requirements analyzed event for project: {}", event.getAggregateId());
        kafkaTemplate.send(PROJECT_EVENTS_TOPIC, event.getAggregateId(), event);
    }
    
    public void publishDomainModelGeneratedEvent(DomainModelGeneratedEvent event) {
        log.info("Publishing domain model generated event for project: {}", event.getAggregateId());
        kafkaTemplate.send(DOMAIN_ANALYZED_TOPIC, event.getAggregateId(), event);
    }
    
    public void publishSchemaGeneratedEvent(SchemaGeneratedEvent event) {
        log.info("Publishing schema generated event for project: {}", event.getAggregateId());
        kafkaTemplate.send(SCHEMA_GENERATED_TOPIC, event.getAggregateId(), event);
    }
}
