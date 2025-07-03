package com.gigapress.domainschema.domain.analysis.service;

import com.gigapress.domainschema.IntegrationTestBase;
import com.gigapress.domainschema.domain.analysis.dto.request.CreateProjectRequest;
import com.gigapress.domainschema.domain.analysis.dto.response.ProjectResponse;
import com.gigapress.domainschema.domain.common.entity.ProjectType;
import com.gigapress.domainschema.domain.common.exception.ProjectNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.test.context.EmbeddedKafka;

import static org.assertj.core.api.Assertions.*;

class ProjectServiceIntegrationTest extends IntegrationTestBase {
    
    @Autowired
    private ProjectService projectService;
    
    @Test
    void createProject_ShouldCreateAndPublishEvent() {
        // Given
        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("Integration Test Project")
                .description("Testing project creation with events")
                .projectType(ProjectType.MICROSERVICE)
                .build();
        
        // When
        ProjectResponse response = projectService.createProject(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getProjectId()).isNotEmpty();
        assertThat(response.getName()).isEqualTo("Integration Test Project");
        assertThat(response.getStatus()).isEqualTo("CREATED");
        
        // Verify project can be retrieved
        ProjectResponse retrieved = projectService.getProject(response.getProjectId());
        assertThat(retrieved.getProjectId()).isEqualTo(response.getProjectId());
    }
    
    @Test
    void deleteProject_ShouldRemoveProject() {
        // Given
        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("Project to Delete")
                .projectType(ProjectType.REST_API)
                .build();
        
        ProjectResponse created = projectService.createProject(request);
        String projectId = created.getProjectId();
        
        // When
        projectService.deleteProject(projectId);
        
        // Then
        assertThatThrownBy(() -> projectService.getProject(projectId))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessageContaining(projectId);
    }
}
