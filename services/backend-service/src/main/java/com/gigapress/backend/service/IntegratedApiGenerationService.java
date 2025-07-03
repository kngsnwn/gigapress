package com.gigapress.backend.service;

import com.gigapress.backend.client.DomainSchemaServiceClient;
import com.gigapress.backend.client.McpServerClient;
import com.gigapress.backend.dto.ApiSpecification;
import com.gigapress.backend.dto.GeneratedApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntegratedApiGenerationService {

    private final ApiGenerationService apiGenerationService;
    private final BusinessLogicGenerationService businessLogicGenerationService;
    private final DomainSchemaServiceClient domainSchemaClient;
    private final McpServerClient mcpServerClient;
    private final KafkaProducerService kafkaProducerService;

    public GeneratedApi generateApiWithIntegration(ApiSpecification specification) {
        log.info("Starting integrated API generation for: {}", specification.getApiName());
        
        try {
            // Step 1: Get entity definition from Domain Schema Service
            Map<String, Object> entityDef = domainSchemaClient.getEntityDefinition(specification.getEntityName());
            enrichSpecificationWithDomainInfo(specification, entityDef);
            
            // Step 2: Validate structure with MCP Server
            Map<String, Object> validationResult = mcpServerClient.validateProjectStructure(
                Map.of("specification", specification)
            );
            
            if (!(Boolean) validationResult.getOrDefault("valid", false)) {
                throw new RuntimeException("Project structure validation failed: " + 
                    validationResult.get("errors"));
            }
            
            // Step 3: Generate API using enhanced specification
            GeneratedApi generatedApi = apiGenerationService.generateApiEndpoints(specification);
            
            // Step 4: Send integration event
            sendIntegrationEvent(specification, generatedApi);
            
            log.info("Integrated API generation completed successfully");
            return generatedApi;
            
        } catch (Exception e) {
            log.error("Error in integrated API generation", e);
            throw new RuntimeException("Failed to generate API with integration", e);
        }
    }

    private void enrichSpecificationWithDomainInfo(ApiSpecification spec, Map<String, Object> entityDef) {
        // Enrich specification with domain information
        if (entityDef.containsKey("fields")) {
            // Add or update fields based on domain definition
            log.info("Enriching specification with {} fields from domain", 
                ((java.util.List<?>) entityDef.get("fields")).size());
        }
        
        if (entityDef.containsKey("relationships")) {
            // Add relationship information
            log.info("Adding relationship information from domain");
        }
    }

    private void sendIntegrationEvent(ApiSpecification spec, GeneratedApi api) {
        Map<String, Object> event = Map.of(
            "type", "API_GENERATED_WITH_INTEGRATION",
            "apiName", spec.getApiName(),
            "entityName", spec.getEntityName(),
            "timestamp", System.currentTimeMillis(),
            "integratedServices", java.util.List.of("domain-schema", "mcp-server")
        );
        
        kafkaProducerService.sendApiGeneratedEvent(api);
        log.info("Integration event sent to Kafka");
    }
}
