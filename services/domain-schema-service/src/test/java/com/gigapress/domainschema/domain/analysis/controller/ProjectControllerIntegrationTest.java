package com.gigapress.domainschema.domain.analysis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigapress.domainschema.IntegrationTestBase;
import com.gigapress.domainschema.domain.analysis.dto.request.CreateProjectRequest;
import com.gigapress.domainschema.domain.common.entity.ProjectType;
import com.gigapress.domainschema.domain.common.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProjectControllerIntegrationTest extends IntegrationTestBase {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @BeforeEach
    void setUp() {
        projectRepository.deleteAll();
    }
    
    @Test
    void createProject_ShouldReturnCreatedProject() throws Exception {
        // Given
        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("E-Commerce Platform")
                .description("A modern e-commerce platform with microservices")
                .projectType(ProjectType.WEB_APPLICATION)
                .build();
        
        // When & Then
        mockMvc.perform(post("/api/v1/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("E-Commerce Platform"))
                .andExpect(jsonPath("$.data.projectType").value("WEB_APPLICATION"))
                .andExpect(jsonPath("$.data.status").value("CREATED"))
                .andExpect(jsonPath("$.data.projectId").isNotEmpty());
    }
    
    @Test
    void createProject_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given
        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("") // Invalid: empty name
                .projectType(null) // Invalid: null type
                .build();
        
        // When & Then
        mockMvc.perform(post("/api/v1/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.data.name").exists())
                .andExpect(jsonPath("$.data.projectType").exists());
    }
    
    @Test
    void getProject_WithExistingProject_ShouldReturnProject() throws Exception {
        // Given
        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("Test Project")
                .projectType(ProjectType.REST_API)
                .build();
        
        String response = mockMvc.perform(post("/api/v1/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        String projectId = objectMapper.readTree(response)
                .path("data")
                .path("projectId")
                .asText();
        
        // When & Then
        mockMvc.perform(get("/api/v1/projects/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectId").value(projectId))
                .andExpect(jsonPath("$.data.name").value("Test Project"));
    }
    
    @Test
    void getProject_WithNonExistentProject_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/projects/{projectId}", "non_existent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value(containsString("Project not found")));
    }
    
    @Test
    void listProjects_ShouldReturnPaginatedList() throws Exception {
        // Given - Create multiple projects
        for (int i = 1; i <= 5; i++) {
            CreateProjectRequest request = CreateProjectRequest.builder()
                    .name("Project " + i)
                    .projectType(ProjectType.WEB_APPLICATION)
                    .build();
            
            mockMvc.perform(post("/api/v1/projects")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        }
        
        // When & Then
        mockMvc.perform(get("/api/v1/projects")
                .param("page", "0")
                .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andExpect(jsonPath("$.data.totalElements").value(5))
                .andExpect(jsonPath("$.data.totalPages").value(2));
    }
}
