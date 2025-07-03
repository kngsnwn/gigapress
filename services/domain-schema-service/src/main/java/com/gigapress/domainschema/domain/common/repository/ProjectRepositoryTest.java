package com.gigapress.domainschema.domain.common.repository;

import com.gigapress.domainschema.domain.common.entity.Project;
import com.gigapress.domainschema.domain.common.entity.ProjectStatus;
import com.gigapress.domainschema.domain.common.entity.ProjectType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ProjectRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    private Project testProject;
    
    @BeforeEach
    void setUp() {
        testProject = Project.builder()
                .projectId("test_proj_001")
                .name("Test Project")
                .description("Test Description")
                .projectType(ProjectType.WEB_APPLICATION)
                .status(ProjectStatus.CREATED)
                .build();
        
        entityManager.persistAndFlush(testProject);
    }
    
    @Test
    void findByProjectId_ShouldReturnProject() {
        // When
        Optional<Project> found = projectRepository.findByProjectId("test_proj_001");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Project");
    }
    
    @Test
    void existsByProjectId_ShouldReturnTrue_WhenProjectExists() {
        // When
        boolean exists = projectRepository.existsByProjectId("test_proj_001");
        
        // Then
        assertThat(exists).isTrue();
    }
    
    @Test
    void findByStatus_ShouldReturnProjects() {
        // Given
        Project anotherProject = Project.builder()
                .projectId("test_proj_002")
                .name("Another Project")
                .projectType(ProjectType.REST_API)
                .status(ProjectStatus.CREATED)
                .build();
        entityManager.persistAndFlush(anotherProject);
        
        // When
        Page<Project> projects = projectRepository.findByStatus(
                ProjectStatus.CREATED, PageRequest.of(0, 10));
        
        // Then
        assertThat(projects.getContent()).hasSize(2);
        assertThat(projects.getTotalElements()).isEqualTo(2);
    }
}
