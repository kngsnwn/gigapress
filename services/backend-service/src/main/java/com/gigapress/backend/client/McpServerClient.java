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
public class McpServerClient {

    private final RestTemplate restTemplate;
    
    @Value("${service.mcp-server.url}")
    private String mcpServerUrl;

    public Map<String, Object> analyzeChangeImpact(Map<String, Object> changeRequest) {
        log.info("Calling MCP Server to analyze change impact");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(changeRequest, headers);
        
        return restTemplate.postForObject(
            mcpServerUrl + "/api/tools/analyze-change-impact",
            entity,
            Map.class
        );
    }

    public Map<String, Object> validateProjectStructure(Map<String, Object> structure) {
        log.info("Calling MCP Server to validate project structure");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(structure, headers);
        
        return restTemplate.postForObject(
            mcpServerUrl + "/api/tools/validate-structure",
            entity,
            Map.class
        );
    }
}
