package com.gigapress.mcp.service;

import com.gigapress.mcp.client.DynamicUpdateEngineClient;
import com.gigapress.mcp.event.EventProducer;
import com.gigapress.mcp.model.domain.Component;
import com.gigapress.mcp.model.domain.DependencyGraph;
import com.gigapress.mcp.model.request.ChangeAnalysisRequest;
import com.gigapress.mcp.model.response.ChangeAnalysisResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangeAnalysisServiceTest {
    
    @Mock
    private DynamicUpdateEngineClient dynamicUpdateEngineClient;
    
    @Mock
    private EventProducer eventProducer;
    
    @InjectMocks
    private ChangeAnalysisService changeAnalysisService;
    
    private DependencyGraph testGraph;
    
    @BeforeEach
    void setUp() {
        testGraph = new DependencyGraph();
        testGraph.setNodes(new HashMap<>());
        testGraph.addNode("comp1", Component.builder()
            .componentId("comp1")
            .componentName("Component 1")
            .type(Component.ComponentType.BACKEND)
            .build());
        testGraph.addNode("comp2", Component.builder()
            .componentId("comp2")
            .componentName("Component 2")
            .type(Component.ComponentType.FRONTEND)
            .build());
        testGraph.addEdge("comp1", "comp2", DependencyGraph.EdgeType.DEPENDS_ON);
    }
    
    @Test
    void testAnalyzeChange_Success() {
        // Given
        ChangeAnalysisRequest request = ChangeAnalysisRequest.builder()
            .projectId("test-project")
            .changeDescription("Update API endpoint")
            .changeType(ChangeAnalysisRequest.ChangeType.FEATURE_MODIFY)
            .targetComponents(new String[]{"comp1"})
            .analysisDepth(ChangeAnalysisRequest.AnalysisDepth.NORMAL)
            .build();
        
        when(dynamicUpdateEngineClient.getDependencyGraph(anyString()))
            .thenReturn(Mono.just(testGraph));
        
        // When
        Mono<ChangeAnalysisResponse> result = changeAnalysisService.analyzeChange(request);
        
        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response);
                assertEquals("test-project", response.getProjectId());
                assertNotNull(response.getAnalysisId());
                assertNotNull(response.getImpactSummary());
                assertEquals(2, response.getAffectedComponents().size());
                assertNotNull(response.getRiskAssessment());
                assertNotNull(response.getRecommendations());
                assertFalse(response.getRecommendations().isEmpty());
            })
            .verifyComplete();
        
        verify(eventProducer, times(1)).publishAnalysisEvent(any());
    }
    
    @Test
    void testAnalyzeChange_EmptyGraph() {
        // Given
        ChangeAnalysisRequest request = ChangeAnalysisRequest.builder()
            .projectId("empty-project")
            .changeDescription("Initial setup")
            .changeType(ChangeAnalysisRequest.ChangeType.FEATURE_ADD)
            .build();
        
        when(dynamicUpdateEngineClient.getDependencyGraph(anyString()))
            .thenReturn(Mono.just(new DependencyGraph()));
        
        // When
        Mono<ChangeAnalysisResponse> result = changeAnalysisService.analyzeChange(request);
        
        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response);
                assertEquals(0, response.getAffectedComponents().size());
            })
            .verifyComplete();
    }
}
