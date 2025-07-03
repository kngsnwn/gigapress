package com.gigapress.domainschema.integration.mcp.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class McpServerClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${mcp.server.url}")
    private String mcpServerUrl;
    
    public Map<String, Object> analyzeRequirements(Map<String, Object> request) {
        log.info("Calling MCP Server to analyze requirements");
        
        String url = mcpServerUrl + "/api/v1/tools/analyze_requirements";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        
        try {
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            log.info("Successfully analyzed requirements");
            return response;
        } catch (Exception e) {
            log.error("Failed to call MCP Server", e);
            throw new RuntimeException("Failed to analyze requirements: " + e.getMessage());
        }
    }
    
    public Map<String, Object> generateDomainModel(Map<String, Object> request) {
        log.info("Calling MCP Server to generate domain model");
        
        String url = mcpServerUrl + "/api/v1/tools/generate_domain_model";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        
        try {
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            log.info("Successfully generated domain model");
            return response;
        } catch (Exception e) {
            log.error("Failed to call MCP Server", e);
            throw new RuntimeException("Failed to generate domain model: " + e.getMessage());
        }
    }
    
    public Map<String, Object> generateSchema(Map<String, Object> request) {
        log.info("Calling MCP Server to generate schema");
        
        String url = mcpServerUrl + "/api/v1/tools/generate_schema";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        
        try {
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            log.info("Successfully generated schema");
            return response;
        } catch (Exception e) {
            log.error("Failed to call MCP Server", e);
            throw new RuntimeException("Failed to generate schema: " + e.getMessage());
        }
    }
}
