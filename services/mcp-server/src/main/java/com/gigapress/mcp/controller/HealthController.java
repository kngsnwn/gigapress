package com.gigapress.mcp.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class HealthController {
    
    @Value("${spring.application.name}")
    private String applicationName;
    
    @Value("${server.port}")
    private String serverPort;
    
    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
            "service", applicationName,
            "port", serverPort,
            "status", "UP",
            "timestamp", LocalDateTime.now(),
            "message", "MCP Server is ready to handle tool requests",
            "endpoints", Map.of(
                "analyze", "/api/tools/analyze",
                "generate", "/api/tools/generate",
                "update", "/api/tools/update",
                "validate", "/api/tools/validate",
                "health", "/api/tools/health",
                "swagger", "/swagger-ui.html"
            )
        );
    }
}
