package com.gigapress.backend.service;

import com.gigapress.backend.dto.ApiSpecification;
import com.gigapress.backend.dto.GeneratedApi;
import com.gigapress.backend.template.ApiTemplateEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiGenerationService {

    private final ApiTemplateEngine templateEngine;
    private final KafkaProducerService kafkaProducerService;

    public GeneratedApi generateApiEndpoints(ApiSpecification specification) {
        log.info("Generating API endpoints for: {}", specification.getApiName());
        
        try {
            // Generate controller code
            String controllerCode = templateEngine.generateController(specification);
            
            // Generate service code
            String serviceCode = templateEngine.generateService(specification);
            
            // Generate repository code
            String repositoryCode = templateEngine.generateRepository(specification);
            
            // Generate DTO classes
            Map<String, String> dtoClasses = templateEngine.generateDtos(specification);
            
            // Create response
            GeneratedApi generatedApi = GeneratedApi.builder()
                    .apiName(specification.getApiName())
                    .controllerCode(controllerCode)
                    .serviceCode(serviceCode)
                    .repositoryCode(repositoryCode)
                    .dtoClasses(dtoClasses)
                    .build();
            
            // Send event to Kafka
            kafkaProducerService.sendApiGeneratedEvent(generatedApi);
            
            return generatedApi;
            
        } catch (Exception e) {
            log.error("Error generating API endpoints", e);
            throw new RuntimeException("Failed to generate API endpoints", e);
        }
    }
}
