package com.gigapress.domainschema.domain.analysis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigapress.domainschema.IntegrationTestBase;
import com.gigapress.domainschema.domain.analysis.dto.request.AnalyzeRequirementsRequest;
import com.gigapress.domainschema.domain.analysis.dto.request.CreateProjectRequest;
import com.gigapress.domainschema.domain.common.entity.ProjectType;
import com.gigapress.domainschema.integration.mcp.client.McpServerClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RequirementsControllerIntegrationTest extends IntegrationTestBase {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private McpServerClient mcpServerClient;
    
    private String projectId;
    
    @BeforeEach
    void setUp() throws Exception {
        // Create a test project
        CreateProjectRequest projectRequest = CreateProjectRequest.builder()
                .name("Test Project")
                .projectType(ProjectType.WEB_APPLICATION)
                .build();
        
        String response = mockMvc.perform(post("/api/v1/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        projectId = objectMapper.readTree(response)
                .path("data")
                .path("projectId")
                .asText();
    }
    
    @Test
    void analyzeRequirements_ShouldReturnAnalysisResults() throws Exception {
        // Given
        AnalyzeRequirementsRequest request = AnalyzeRequirementsRequest.builder()
                .projectId(projectId)
                .naturalLanguageRequirements("Users should be able to register and login. " +
                        "Products should have categories and customer reviews.")
                .constraints(Arrays.asList("Must support mobile devices", "Handle 1000 concurrent users"))
                .build();
        
        // Mock MCP Server response
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("summary", "E-commerce platform with user management and product catalog");
        
        List<Map<String, Object>> requirements = new ArrayList<>();
        Map<String, Object> req1 = new HashMap<>();
        req1.put("title", "User Registration");
        req1.put("description", "Users should be able to register with email and password");
        req1.put("type", "FUNCTIONAL");
        req1.put("priority", "HIGH");
        req1.put("metadata", new HashMap<>());
        requirements.add(req1);
        
        mockResponse.put("requirements", requirements);
        mockResponse.put("identifiedEntities", Arrays.asList("User", "Product", "Category", "Review"));
        mockResponse.put("suggestedRelationships", Arrays.asList("User-Review", "Product-Category", "Product-Review"));
        mockResponse.put("confidenceScore", 0.95);
        
        Mockito.when(mcpServerClient.analyzeRequirements(any())).thenReturn(mockResponse);
        
        // When & Then
        mockMvc.perform(post("/api/v1/requirements/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectId").value(projectId))
                .andExpect(jsonPath("$.data.summary").exists())
                .andExpect(jsonPath("$.data.requirements").isArray())
                .andExpect(jsonPath("$.data.identifiedEntities").isArray())
                .andExpect(jsonPath("$.data.confidenceScore").value(0.95));
    }
}
