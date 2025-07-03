#!/bin/bash

# MCP Server Controllers Creation Script

echo "🎮 Creating controllers for MCP Server..."

BASE_DIR="services/mcp-server"
CONTROLLER_DIR="$BASE_DIR/src/main/java/com/gigapress/mcp/controller"
CONFIG_DIR="$BASE_DIR/src/main/java/com/gigapress/mcp/config"
FILTER_DIR="$BASE_DIR/src/main/java/com/gigapress/mcp/filter"

# Create directories
mkdir -p $CONTROLLER_DIR
mkdir -p $FILTER_DIR

# ===== MAIN CONTROLLER =====

# ToolController.java
echo "📝 Creating ToolController.java..."
cat > $CONTROLLER_DIR/ToolController.java << 'EOF'
package com.gigapress.mcp.controller;

import com.gigapress.mcp.model.request.*;
import com.gigapress.mcp.model.response.*;
import com.gigapress.mcp.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/tools")
@RequiredArgsConstructor
@Tag(name = "MCP Tools", description = "Core tool APIs for project management")
public class ToolController {
    
    private final ChangeAnalysisService changeAnalysisService;
    private final ProjectGenerationService projectGenerationService;
    private final ComponentUpdateService componentUpdateService;
    private final ConsistencyValidationService consistencyValidationService;
    
    @PostMapping("/analyze")
    @Operation(summary = "Analyze change impact", 
              description = "Analyzes the impact of proposed changes on the project")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Analysis completed successfully",
                    content = @Content(schema = @Schema(implementation = ChangeAnalysisResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<ResponseEntity<ApiResponse<ChangeAnalysisResponse>>> analyzeChangeImpact(
            @Valid @RequestBody ChangeAnalysisRequest request,
            @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
        
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }
        final String finalRequestId = requestId;
        
        log.info("Analyzing change impact for project: {} [Request ID: {}]", 
                request.getProjectId(), requestId);
        
        return changeAnalysisService.analyzeChange(request)
            .map(response -> {
                ApiResponse<ChangeAnalysisResponse> apiResponse = ApiResponse.<ChangeAnalysisResponse>builder()
                    .success(true)
                    .data(response)
                    .requestId(finalRequestId)
                    .build();
                return ResponseEntity.ok(apiResponse);
            })
            .onErrorResume(error -> {
                log.error("Error analyzing change impact", error);
                ApiResponse<ChangeAnalysisResponse> errorResponse = ApiResponse.error(
                    "Failed to analyze change impact: " + error.getMessage(),
                    "ANALYSIS_ERROR"
                );
                errorResponse.setRequestId(finalRequestId);
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse));
            });
    }
    
    @PostMapping("/generate")
    @Operation(summary = "Generate project structure", 
              description = "Generates a new project structure based on requirements")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Project generated successfully",
                    content = @Content(schema = @Schema(implementation = ProjectGenerationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<ResponseEntity<ApiResponse<ProjectGenerationResponse>>> generateProjectStructure(
            @Valid @RequestBody ProjectGenerationRequest request,
            @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
        
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }
        final String finalRequestId = requestId;
        
        log.info("Generating project structure: {} [Request ID: {}]", 
                request.getProjectName(), requestId);
        
        return projectGenerationService.generateProject(request)
            .map(response -> {
                ApiResponse<ProjectGenerationResponse> apiResponse = ApiResponse.<ProjectGenerationResponse>builder()
                    .success(true)
                    .data(response)
                    .requestId(finalRequestId)
                    .build();
                return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
            })
            .onErrorResume(error -> {
                log.error("Error generating project", error);
                ApiResponse<ProjectGenerationResponse> errorResponse = ApiResponse.error(
                    "Failed to generate project: " + error.getMessage(),
                    "GENERATION_ERROR"
                );
                errorResponse.setRequestId(finalRequestId);
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse));
            });
    }
    
    @PutMapping("/update")
    @Operation(summary = "Update components", 
              description = "Updates one or more components in the project")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Components updated successfully",
                    content = @Content(schema = @Schema(implementation = ComponentUpdateResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<ResponseEntity<ApiResponse<ComponentUpdateResponse>>> updateComponents(
            @Valid @RequestBody ComponentUpdateRequest request,
            @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
        
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }
        final String finalRequestId = requestId;
        
        log.info("Updating components for project: {} [Request ID: {}]", 
                request.getProjectId(), requestId);
        
        return componentUpdateService.updateComponents(request)
            .map(response -> {
                ApiResponse<ComponentUpdateResponse> apiResponse = ApiResponse.<ComponentUpdateResponse>builder()
                    .success(true)
                    .data(response)
                    .requestId(finalRequestId)
                    .build();
                return ResponseEntity.ok(apiResponse);
            })
            .onErrorResume(error -> {
                log.error("Error updating components", error);
                ApiResponse<ComponentUpdateResponse> errorResponse = ApiResponse.error(
                    "Failed to update components: " + error.getMessage(),
                    "UPDATE_ERROR"
                );
                errorResponse.setRequestId(finalRequestId);
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse));
            });
    }
    
    @PostMapping("/validate")
    @Operation(summary = "Validate project consistency", 
              description = "Validates various aspects of project consistency")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Validation completed",
                    content = @Content(schema = @Schema(implementation = ValidationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<ResponseEntity<ApiResponse<ValidationResponse>>> validateConsistency(
            @Valid @RequestBody ValidationRequest request,
            @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
        
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }
        final String finalRequestId = requestId;
        
        log.info("Validating consistency for project: {} [Request ID: {}]", 
                request.getProjectId(), requestId);
        
        return consistencyValidationService.validateConsistency(request)
            .map(response -> {
                ApiResponse<ValidationResponse> apiResponse = ApiResponse.<ValidationResponse>builder()
                    .success(true)
                    .data(response)
                    .requestId(finalRequestId)
                    .build();
                
                HttpStatus status = response.getValidationStatus() == 
                    ValidationResponse.ValidationStatus.FAILED ? 
                    HttpStatus.UNPROCESSABLE_ENTITY : HttpStatus.OK;
                
                return ResponseEntity.status(status).body(apiResponse);
            })
            .onErrorResume(error -> {
                log.error("Error validating consistency", error);
                ApiResponse<ValidationResponse> errorResponse = ApiResponse.error(
                    "Failed to validate consistency: " + error.getMessage(),
                    "VALIDATION_ERROR"
                );
                errorResponse.setRequestId(finalRequestId);
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse));
            });
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the tools API is healthy")
    public ResponseEntity<ApiResponse<HealthStatus>> healthCheck() {
        HealthStatus status = HealthStatus.builder()
            .status("UP")
            .service("MCP Tools API")
            .version("1.0.0")
            .build();
        
        return ResponseEntity.ok(ApiResponse.success(status));
    }
    
    @Data
    @Builder
    public static class HealthStatus {
        private String status;
        private String service;
        private String version;
    }
}
EOF

# Update HealthController to remove duplicate endpoint
echo "📝 Updating HealthController.java..."
cat > $CONTROLLER_DIR/HealthController.java << 'EOF'
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
EOF

# Create OpenAPI configuration
echo "📝 Creating OpenApiConfig.java..."
cat > $CONFIG_DIR/OpenApiConfig.java << 'EOF'
package com.gigapress.mcp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    
    @Value("${server.port}")
    private String serverPort;
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("MCP Server API")
                .version("1.0.0")
                .description("Model Context Protocol Server - Core Tool APIs for GigaPress")
                .contact(new Contact()
                    .name("GigaPress Team")
                    .email("support@gigapress.com")
                    .url("https://gigapress.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:" + serverPort)
                    .description("Local Development Server"),
                new Server()
                    .url("https://api.gigapress.com")
                    .description("Production Server")
            ));
    }
}
EOF

# Create Request/Response logging filter
echo "📝 Creating RequestResponseLoggingFilter.java..."
cat > $FILTER_DIR/RequestResponseLoggingFilter.java << 'EOF'
package com.gigapress.mcp.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
public class RequestResponseLoggingFilter implements WebFilter {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Instant start = Instant.now();
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();
        String requestId = exchange.getRequest().getHeaders().getFirst("X-Request-ID");
        
        if (requestId == null) {
            requestId = exchange.getRequest().getId();
        }
        
        log.info("Incoming request: {} {} [Request ID: {}]", method, path, requestId);
        
        return chain.filter(exchange)
            .doOnSuccess(aVoid -> {
                Duration duration = Duration.between(start, Instant.now());
                int statusCode = exchange.getResponse().getStatusCode() != null ? 
                    exchange.getResponse().getStatusCode().value() : 0;
                
                log.info("Outgoing response: {} {} - Status: {} - Duration: {}ms [Request ID: {}]",
                    method, path, statusCode, duration.toMillis(), requestId);
            })
            .doOnError(error -> {
                Duration duration = Duration.between(start, Instant.now());
                log.error("Request failed: {} {} - Duration: {}ms - Error: {} [Request ID: {}]",
                    method, path, duration.toMillis(), error.getMessage(), requestId);
            });
    }
}
EOF

# Update build.gradle to add Swagger dependencies
echo "📝 Updating build.gradle with OpenAPI dependencies..."
cat > $BASE_DIR/build.gradle << 'EOF'
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.4'
}

group = 'com.gigapress'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '17'

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    
    // Kafka
    implementation 'org.springframework.kafka:spring-kafka'
    
    // Redis
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    implementation 'org.apache.commons:commons-pool2'
    
    // WebClient
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    
    // OpenAPI/Swagger
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'
    implementation 'org.springdoc:springdoc-openapi-starter-webflux-ui:2.3.0'
    
    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    // JSON Processing
    implementation 'com.fasterxml.jackson.core:jackson-databind'
    implementation 'com.fasterxml.jackson.datatype:jackson-datatype-jsr310'
    
    // Apache Commons
    implementation 'org.apache.commons:commons-lang3:3.12.0'
    
    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.kafka:spring-kafka-test'
    testImplementation 'io.projectreactor:reactor-test'
}

tasks.named('test') {
    useJUnitPlatform()
}

springBoot {
    buildInfo()
}
EOF

# Update application.properties with OpenAPI settings
echo "📝 Updating application.properties with OpenAPI settings..."
cat >> $BASE_DIR/src/main/resources/application.properties << 'EOF'

# OpenAPI/Swagger Configuration
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operations-sorter=method
springdoc.swagger-ui.tags-sorter=alpha
springdoc.show-actuator=true
springdoc.default-produces-media-type=application/json
springdoc.default-consumes-media-type=application/json
EOF

# Create a test configuration for easier development
echo "📝 Creating application-dev.properties..."
cat > $BASE_DIR/src/main/resources/application-dev.properties << 'EOF'
# Development Profile Configuration
server.port=8082
spring.profiles.active=dev

# Enhanced logging for development
logging.level.com.gigapress.mcp=DEBUG
logging.level.org.springframework.web=DEBUG
logging.level.org.springframework.kafka=DEBUG

# Pretty print JSON
spring.jackson.serialization.indent-output=true

# Show SQL-like logs for Redis
logging.level.io.lettuce.core=DEBUG

# Actuator - expose all endpoints in dev
management.endpoints.web.exposure.include=*

# Error handling - show full stack trace in dev
server.error.include-stacktrace=always
server.error.include-message=always
EOF

# Create integration test
echo "📝 Creating ToolControllerTest.java..."
mkdir -p $BASE_DIR/src/test/java/com/gigapress/mcp/controller
cat > $BASE_DIR/src/test/java/com/gigapress/mcp/controller/ToolControllerTest.java << 'EOF'
package com.gigapress.mcp.controller;

import com.gigapress.mcp.model.request.ChangeAnalysisRequest;
import com.gigapress.mcp.model.response.ApiResponse;
import com.gigapress.mcp.model.response.ChangeAnalysisResponse;
import com.gigapress.mcp.service.ChangeAnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(ToolController.class)
class ToolControllerTest {
    
    @Autowired
    private WebTestClient webTestClient;
    
    @MockBean
    private ChangeAnalysisService changeAnalysisService;
    
    @MockBean
    private com.gigapress.mcp.service.ProjectGenerationService projectGenerationService;
    
    @MockBean
    private com.gigapress.mcp.service.ComponentUpdateService componentUpdateService;
    
    @MockBean
    private com.gigapress.mcp.service.ConsistencyValidationService consistencyValidationService;
    
    @Test
    void testAnalyzeChangeImpact() {
        // Given
        ChangeAnalysisRequest request = ChangeAnalysisRequest.builder()
            .projectId("test-project")
            .changeDescription("Add new feature")
            .changeType(ChangeAnalysisRequest.ChangeType.FEATURE_ADD)
            .build();
        
        ChangeAnalysisResponse mockResponse = ChangeAnalysisResponse.builder()
            .analysisId("test-analysis-id")
            .projectId("test-project")
            .build();
        
        when(changeAnalysisService.analyzeChange(any(ChangeAnalysisRequest.class)))
            .thenReturn(Mono.just(mockResponse));
        
        // When & Then
        webTestClient.post()
            .uri("/api/tools/analyze")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.success").isEqualTo(true)
            .jsonPath("$.data.analysisId").isEqualTo("test-analysis-id");
    }
    
    @Test
    void testHealthCheck() {
        webTestClient.get()
            .uri("/api/tools/health")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.success").isEqualTo(true)
            .jsonPath("$.data.status").isEqualTo("UP");
    }
}
EOF

echo "✅ Controllers and API configuration created successfully!"
echo ""
echo "📋 Created components:"
echo "  Controllers:"
echo "    - ToolController (main API endpoints)"
echo "    - Updated HealthController"
echo ""
echo "  Configuration:"
echo "    - OpenAPI/Swagger configuration"
echo "    - Request/Response logging filter"
echo "    - Development profile settings"
echo ""
echo "  Testing:"
echo "    - ToolControllerTest"
echo ""
echo "🔗 API Endpoints:"
echo "    - POST /api/tools/analyze - Analyze change impact"
echo "    - POST /api/tools/generate - Generate project structure"
echo "    - PUT  /api/tools/update - Update components"
echo "    - POST /api/tools/validate - Validate consistency"
echo "    - GET  /api/tools/health - Health check"
echo ""
echo "📚 API Documentation will be available at:"
echo "    - Swagger UI: http://localhost:8082/swagger-ui.html"
echo "    - OpenAPI JSON: http://localhost:8082/api-docs"