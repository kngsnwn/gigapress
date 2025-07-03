package com.gigapress.domainschema.domain.analysis.service;

import com.gigapress.domainschema.domain.analysis.dto.request.CreateProjectRequest;
import com.gigapress.domainschema.domain.analysis.dto.response.ProjectResponse;
import com.gigapress.domainschema.domain.analysis.mapper.ProjectMapper;
import com.gigapress.domainschema.domain.common.dto.PageResponse;
import com.gigapress.domainschema.domain.common.entity.Project;
import com.gigapress.domainschema.domain.common.entity.ProjectStatus;
import com.gigapress.domainschema.domain.common.event.ProjectCreatedEvent;
import com.gigapress.domainschema.domain.common.exception.ProjectNotFoundException;
import com.gigapress.domainschema.domain.common.repository.ProjectRepository;
import com.gigapress.domainschema.integration.kafka.producer.DomainEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProjectServiceImpl implements ProjectService {
    
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final DomainEventProducer eventProducer;
    
    @Override
    public ProjectResponse createProject(CreateProjectRequest request) {
        log.info("Creating new project: {}", request.getName());
        
        // Generate unique project ID
        String projectId = "proj_" + UUID.randomUUID().toString().substring(0, 8);
        
        // Create project entity
        Project project = projectMapper.toEntity(request);
        project.setProjectId(projectId);
        project.setStatus(ProjectStatus.CREATED);
        
        // Save project
        Project savedProject = projectRepository.save(project);
        log.info("Project created with ID: {}", projectId);
        
        // Publish event
        ProjectCreatedEvent event = ProjectCreatedEvent.builder()
                .projectId(projectId)
                .projectName(savedProject.getName())
                .projectType(savedProject.getProjectType())
                .description(savedProject.getDescription())
                .build();
        eventProducer.publishProjectCreatedEvent(event);
        
        return projectMapper.toResponse(savedProject);
    }
    
    @Override
    @Cacheable(value = "projects", key = "#projectId")
    public ProjectResponse getProject(String projectId) {
        log.info("Fetching project: {}", projectId);
        
        Project project = projectRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
        
        return projectMapper.toResponse(project);
    }
    
    @Override
    public PageResponse<ProjectResponse> listProjects(Pageable pageable, String status) {
        log.info("Listing projects with status: {}", status);
        
        Page<Project> projectPage;
        if (status != null) {
            ProjectStatus projectStatus = ProjectStatus.valueOf(status.toUpperCase());
            projectPage = projectRepository.findByStatus(projectStatus, pageable);
        } else {
            projectPage = projectRepository.findAll(pageable);
        }
        
        return PageResponse.<ProjectResponse>builder()
                .content(projectPage.map(projectMapper::toResponse).getContent())
                .pageNumber(projectPage.getNumber())
                .pageSize(projectPage.getSize())
                .totalElements(projectPage.getTotalElements())
                .totalPages(projectPage.getTotalPages())
                .first(projectPage.isFirst())
                .last(projectPage.isLast())
                .empty(projectPage.isEmpty())
                .build();
    }
    
    @Override
    @CacheEvict(value = "projects", key = "#projectId")
    public void deleteProject(String projectId) {
        log.info("Deleting project: {}", projectId);
        
        Project project = projectRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
        
        projectRepository.delete(project);
        log.info("Project deleted: {}", projectId);
    }
    
    @Override
    @CacheEvict(value = "projects", key = "#projectId")
    public ProjectResponse updateProjectStatus(String projectId, String status) {
        log.info("Updating project {} status to: {}", projectId, status);
        
        Project project = projectRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
        
        project.updateStatus(ProjectStatus.valueOf(status.toUpperCase()));
        Project updatedProject = projectRepository.save(project);
        
        return projectMapper.toResponse(updatedProject);
    }
}
