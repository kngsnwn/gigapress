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
