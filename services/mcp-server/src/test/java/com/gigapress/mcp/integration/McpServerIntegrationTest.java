package com.gigapress.mcp.integration;

import com.gigapress.mcp.model.request.*;
import com.gigapress.mcp.model.response.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class McpServerIntegrationTest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private WebTestClient webTestClient;
    
    @Test
    void testHealthEndpoint() {
        webTestClient.get()
            .uri("/api/tools/health")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.success").isEqualTo(true)
            .jsonPath("$.data.status").isEqualTo("UP");
    }
    
    @Test
    void testCompleteProjectWorkflow() {
        // Step 1: Generate Project
        ProjectGenerationRequest generateRequest = ProjectGenerationRequest.builder()
            .projectName("Integration Test Project")
            .projectDescription("Test project for integration testing")
            .projectType(ProjectGenerationRequest.ProjectType.WEB_APPLICATION)
            .features(List.of("Authentication", "API"))
            .build();
        
        String projectId = webTestClient.post()
            .uri("/api/tools/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(generateRequest)
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.success").isEqualTo(true)
            .jsonPath("$.data.projectId").value(String.class);
        
        assertNotNull(projectId);
        
        // Step 2: Analyze Change Impact
        ChangeAnalysisRequest analysisRequest = ChangeAnalysisRequest.builder()
            .projectId(projectId)
            .changeDescription("Add new user profile feature")
            .changeType(ChangeAnalysisRequest.ChangeType.FEATURE_ADD)
            .build();
        
        webTestClient.mutate()
            .responseTimeout(Duration.ofSeconds(30))
            .build()
            .post()
            .uri("/api/tools/analyze")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(analysisRequest)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.success").isEqualTo(true)
            .jsonPath("$.data.analysisId").isNotEmpty();
        
        // Step 3: Validate Project
        ValidationRequest validationRequest = ValidationRequest.builder()
            .projectId(projectId)
            .validationTypes(List.of(
                ValidationRequest.ValidationType.DEPENDENCY_CONSISTENCY,
                ValidationRequest.ValidationType.CODE_QUALITY
            ))
            .build();
        
        webTestClient.post()
            .uri("/api/tools/validate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(validationRequest)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.success").isEqualTo(true)
            .jsonPath("$.data.validationId").isNotEmpty();
    }
}
