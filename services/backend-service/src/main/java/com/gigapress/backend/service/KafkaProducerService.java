package com.gigapress.backend.service;

import com.gigapress.backend.dto.GeneratedApi;
import com.gigapress.backend.dto.GeneratedBusinessLogic;
import com.gigapress.backend.event.ApiGeneratedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "api-generation-events";

    public void sendApiGeneratedEvent(GeneratedApi generatedApi) {
        ApiGeneratedEvent event = new ApiGeneratedEvent();
        event.setApiName(generatedApi.getApiName());
        event.setTimestamp(System.currentTimeMillis());
        event.setStatus("COMPLETED");

        log.info("Sending API generated event: {}", event);
        kafkaTemplate.send(TOPIC, event);
    }

    public void sendBusinessLogicGeneratedEvent(GeneratedBusinessLogic businessLogic) {
        Map<String, Object> event = new HashMap<>();
        event.put("patternType", businessLogic.getPatternType());
        event.put("timestamp", System.currentTimeMillis());
        event.put("status", "COMPLETED");

        log.info("Sending business logic generated event: {}", event);
        kafkaTemplate.send("business-logic-events", event);
    }
}