package com.gigapress.mcp.service;

import com.gigapress.mcp.event.EventProducer;
import com.gigapress.mcp.model.request.ProjectGenerationRequest;
import com.gigapress.mcp.model.response.ProjectGenerationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectGenerationServiceTest {
    
    @Mock
    private EventProducer eventProducer;
    
    @InjectMocks
    private ProjectGenerationService projectGenerationService;
    
    @Test
    void testGenerateWebApplication() {
        // Given
        ProjectGenerationRequest request = ProjectGenerationRequest.builder()
            .projectName("Test Web App")
            .projectDescription("A test web application")
            .projectType(ProjectGenerationRequest.ProjectType.WEB_APPLICATION)
            .features(List.of("Authentication", "User Management"))
            .technologyStack(ProjectGenerationRequest.TechnologyStack.builder()
                .frontend("react")
                .backend("node")
                .database("postgresql")
                .build())
            .build();
        
        // When
        Mono<ProjectGenerationResponse> result = projectGenerationService.generateProject(request);
        
        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response);
                assertEquals("Test Web App", response.getProjectName());
                assertEquals(ProjectGenerationResponse.GenerationStatus.SUCCESS, 
                    response.getGenerationStatus());
                assertNotNull(response.getProjectStructure());
                assertFalse(response.getGeneratedComponents().isEmpty());
                assertNotNull(response.getSetupInstructions());
                assertTrue(response.getGenerationDurationMs() > 0);
            })
            .verifyComplete();
        
        verify(eventProducer, times(1)).publishProjectEvent(any());
    }
    
    @Test
    void testGenerateMicroservices() {
        // Given
        ProjectGenerationRequest request = ProjectGenerationRequest.builder()
            .projectName("Test Microservices")
            .projectDescription("A microservices architecture")
            .projectType(ProjectGenerationRequest.ProjectType.MICROSERVICES)
            .build();
        
        // When
        Mono<ProjectGenerationResponse> result = projectGenerationService.generateProject(request);
        
        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response);
                assertTrue(response.getProjectStructure().getDirectoryStructure()
                    .containsKey("services"));
                assertTrue(response.getGeneratedComponents().stream()
                    .anyMatch(c -> c.getComponentId().equals("gateway")));
            })
            .verifyComplete();
    }
}
