package com.gigapress.backend.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DomainSchemaServiceClient {

    private final RestTemplate restTemplate;
    
    @Value("${service.domain-schema.url}")
    private String domainSchemaUrl;

    public Map<String, Object> analyzeDomain(String domainDescription) {
        log.info("Calling Domain Schema Service to analyze domain");
        
        Map<String, Object> request = Map.of("description", domainDescription);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        
        return restTemplate.postForObject(
            domainSchemaUrl + "/api/domain/analyze",
            entity,
            Map.class
        );
    }

    public Map<String, Object> generateSchema(Map<String, Object> domainModel) {
        log.info("Calling Domain Schema Service to generate schema");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(domainModel, headers);
        
        return restTemplate.postForObject(
            domainSchemaUrl + "/api/schema/generate",
            entity,
            Map.class
        );
    }

    public Map<String, Object> getEntityDefinition(String entityName) {
        log.info("Calling Domain Schema Service to get entity definition for: {}", entityName);
        
        return restTemplate.getForObject(
            domainSchemaUrl + "/api/entities/" + entityName,
            Map.class
        );
    }
}
