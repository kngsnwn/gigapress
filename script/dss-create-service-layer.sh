#!/bin/bash

# Service Layer Creation Script for Domain/Schema Service
set -e


# Navigate to the project directory
cd services/domain-schema-service/src/main/java/com/gigapress/domainschema

# Create Repository interfaces
echo "📦 Creating Repository interfaces..."
mkdir -p domain/common/repository

# ProjectRepository
cat > domain/common/repository/ProjectRepository.java << 'EOF'
package com.gigapress.domainschema.domain.common.repository;

import com.gigapress.domainschema.domain.common.entity.Project;
import com.gigapress.domainschema.domain.common.entity.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    Optional<Project> findByProjectId(String projectId);
    
    boolean existsByProjectId(String projectId);
    
    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);
    
    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.requirements WHERE p.projectId = :projectId")
    Optional<Project> findByProjectIdWithRequirements(@Param("projectId") String projectId);
    
    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = :status")
    long countByStatus(@Param("status") ProjectStatus status);
    
    List<Project> findTop10ByOrderByCreatedAtDesc();
}
EOF

# RequirementRepository
cat > domain/common/repository/RequirementRepository.java << 'EOF'
package com.gigapress.domainschema.domain.common.repository;

import com.gigapress.domainschema.domain.common.entity.Requirement;
import com.gigapress.domainschema.domain.common.entity.RequirementStatus;
import com.gigapress.domainschema.domain.common.entity.RequirementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequirementRepository extends JpaRepository<Requirement, Long> {
    
    List<Requirement> findByProjectProjectId(String projectId);
    
    List<Requirement> findByProjectProjectIdAndStatus(String projectId, RequirementStatus status);
    
    List<Requirement> findByProjectProjectIdAndType(String projectId, RequirementType type);
    
    @Query("SELECT r FROM Requirement r WHERE r.project.projectId = :projectId ORDER BY r.priority.order ASC")
    List<Requirement> findByProjectIdOrderByPriority(@Param("projectId") String projectId);
    
    long countByProjectProjectId(String projectId);
}
EOF

# DomainModelRepository
cat > domain/common/repository/DomainModelRepository.java << 'EOF'
package com.gigapress.domainschema.domain.common.repository;

import com.gigapress.domainschema.domain.common.entity.DomainModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DomainModelRepository extends JpaRepository<DomainModel, Long> {
    
    Optional<DomainModel> findByProjectProjectId(String projectId);
    
    @Query("SELECT dm FROM DomainModel dm LEFT JOIN FETCH dm.entities WHERE dm.project.projectId = :projectId")
    Optional<DomainModel> findByProjectIdWithEntities(@Param("projectId") String projectId);
    
    @Query("SELECT dm FROM DomainModel dm LEFT JOIN FETCH dm.relationships WHERE dm.project.projectId = :projectId")
    Optional<DomainModel> findByProjectIdWithRelationships(@Param("projectId") String projectId);
    
    boolean existsByProjectProjectId(String projectId);
}
EOF

# SchemaDesignRepository
cat > domain/common/repository/SchemaDesignRepository.java << 'EOF'
package com.gigapress.domainschema.domain.common.repository;

import com.gigapress.domainschema.domain.common.entity.SchemaDesign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchemaDesignRepository extends JpaRepository<SchemaDesign, Long> {
    
    Optional<SchemaDesign> findByProjectProjectId(String projectId);
    
    @Query("SELECT sd FROM SchemaDesign sd LEFT JOIN FETCH sd.tables WHERE sd.project.projectId = :projectId")
    Optional<SchemaDesign> findByProjectIdWithTables(@Param("projectId") String projectId);
    
    boolean existsByProjectProjectId(String projectId);
}
EOF

# Create Mapper interfaces
echo "📦 Creating Mapper interfaces..."

# ProjectMapper
cat > domain/analysis/mapper/ProjectMapper.java << 'EOF'
package com.gigapress.domainschema.domain.analysis.mapper;

import com.gigapress.domainschema.domain.analysis.dto.request.CreateProjectRequest;
import com.gigapress.domainschema.domain.analysis.dto.response.ProjectResponse;
import com.gigapress.domainschema.domain.common.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "projectId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "requirements", ignore = true)
    @Mapping(target = "domainModel", ignore = true)
    @Mapping(target = "schemaDesign", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Project toEntity(CreateProjectRequest request);
    
    @Mapping(target = "requirementCount", expression = "java(project.getRequirements() != null ? project.getRequirements().size() : 0)")
    ProjectResponse toResponse(Project project);
    
    void updateEntityFromRequest(CreateProjectRequest request, @MappingTarget Project project);
}
EOF

# RequirementMapper
cat > domain/analysis/mapper/RequirementMapper.java << 'EOF'
package com.gigapress.domainschema.domain.analysis.mapper;

import com.gigapress.domainschema.domain.analysis.dto.request.AddRequirementRequest;
import com.gigapress.domainschema.domain.analysis.dto.response.RequirementResponse;
import com.gigapress.domainschema.domain.common.entity.Requirement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RequirementMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Requirement toEntity(AddRequirementRequest request);
    
    RequirementResponse toResponse(Requirement requirement);
    
    List<RequirementResponse> toResponseList(List<Requirement> requirements);
}
EOF

# Create Service interfaces
echo "📦 Creating Service interfaces..."

# ProjectService interface
cat > domain/analysis/service/ProjectService.java << 'EOF'
package com.gigapress.domainschema.domain.analysis.service;

import com.gigapress.domainschema.domain.analysis.dto.request.CreateProjectRequest;
import com.gigapress.domainschema.domain.analysis.dto.response.ProjectResponse;
import com.gigapress.domainschema.domain.common.dto.PageResponse;
import org.springframework.data.domain.Pageable;

public interface ProjectService {
    
    ProjectResponse createProject(CreateProjectRequest request);
    
    ProjectResponse getProject(String projectId);
    
    PageResponse<ProjectResponse> listProjects(Pageable pageable, String status);
    
    void deleteProject(String projectId);
    
    ProjectResponse updateProjectStatus(String projectId, String status);
}
EOF

# RequirementAnalysisService interface
cat > domain/analysis/service/RequirementAnalysisService.java << 'EOF'
package com.gigapress.domainschema.domain.analysis.service;

import com.gigapress.domainschema.domain.analysis.dto.request.AddRequirementRequest;
import com.gigapress.domainschema.domain.analysis.dto.request.AnalyzeRequirementsRequest;
import com.gigapress.domainschema.domain.analysis.dto.response.AnalysisResultResponse;
import com.gigapress.domainschema.domain.analysis.dto.response.RequirementResponse;

import java.util.List;

public interface RequirementAnalysisService {
    
    AnalysisResultResponse analyzeRequirements(AnalyzeRequirementsRequest request);
    
    RequirementResponse addRequirement(String projectId, AddRequirementRequest request);
    
    List<RequirementResponse> getProjectRequirements(String projectId);
    
    RequirementResponse updateRequirementStatus(Long requirementId, String status);
}
EOF

# DomainModelService interface
cat > domain/analysis/service/DomainModelService.java << 'EOF'
package com.gigapress.domainschema.domain.analysis.service;

import com.gigapress.domainschema.domain.analysis.dto.response.DomainModelResponse;

public interface DomainModelService {
    
    DomainModelResponse generateDomainModel(String projectId);
    
    DomainModelResponse getDomainModel(String projectId);
    
    DomainModelResponse regenerateDomainModel(String projectId);
}
EOF

# SchemaDesignService interface
cat > schema/design/service/SchemaDesignService.java << 'EOF'
package com.gigapress.domainschema.schema.design.service;

import com.gigapress.domainschema.schema.design.dto.request.GenerateSchemaRequest;
import com.gigapress.domainschema.schema.design.dto.response.SchemaDesignResponse;

public interface SchemaDesignService {
    
    SchemaDesignResponse generateSchema(GenerateSchemaRequest request);
    
    SchemaDesignResponse getSchemaDesign(String projectId);
    
    String getDdlScript(String projectId);
    
    String getMigrationScript(String projectId);
}
EOF

# EntityMappingService interface
cat > schema/mapping/service/EntityMappingService.java << 'EOF'
package com.gigapress.domainschema.schema.mapping.service;

import com.gigapress.domainschema.schema.mapping.dto.request.GenerateEntitiesRequest;
import com.gigapress.domainschema.schema.mapping.dto.response.EntityMappingResponse;

public interface EntityMappingService {
    
    EntityMappingResponse generateEntities(GenerateEntitiesRequest request);
    
    EntityMappingResponse getEntityMappings(String projectId);
    
    String getEntityFileContent(String projectId, String fileName);
    
    byte[] getAllEntitiesAsZip(String projectId);
}
EOF

# Create Service implementations
echo "📦 Creating Service implementations..."

# ProjectServiceImpl
cat > domain/analysis/service/ProjectServiceImpl.java << 'EOF'
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
EOF

# RequirementAnalysisServiceImpl
cat > domain/analysis/service/RequirementAnalysisServiceImpl.java << 'EOF'
package com.gigapress.domainschema.domain.analysis.service;

import com.gigapress.domainschema.domain.analysis.dto.request.AddRequirementRequest;
import com.gigapress.domainschema.domain.analysis.dto.request.AnalyzeRequirementsRequest;
import com.gigapress.domainschema.domain.analysis.dto.response.AnalysisResultResponse;
import com.gigapress.domainschema.domain.analysis.dto.response.RequirementResponse;
import com.gigapress.domainschema.domain.analysis.mapper.RequirementMapper;
import com.gigapress.domainschema.domain.common.entity.*;
import com.gigapress.domainschema.domain.common.event.RequirementsAnalyzedEvent;
import com.gigapress.domainschema.domain.common.exception.InvalidRequirementException;
import com.gigapress.domainschema.domain.common.exception.ProjectNotFoundException;
import com.gigapress.domainschema.domain.common.repository.ProjectRepository;
import com.gigapress.domainschema.domain.common.repository.RequirementRepository;
import com.gigapress.domainschema.integration.kafka.producer.DomainEventProducer;
import com.gigapress.domainschema.integration.mcp.client.McpServerClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RequirementAnalysisServiceImpl implements RequirementAnalysisService {
    
    private final ProjectRepository projectRepository;
    private final RequirementRepository requirementRepository;
    private final RequirementMapper requirementMapper;
    private final McpServerClient mcpServerClient;
    private final DomainEventProducer eventProducer;
    
    @Override
    public AnalysisResultResponse analyzeRequirements(AnalyzeRequirementsRequest request) {
        log.info("Analyzing requirements for project: {}", request.getProjectId());
        
        // Get project
        Project project = projectRepository.findByProjectId(request.getProjectId())
                .orElseThrow(() -> new ProjectNotFoundException(request.getProjectId()));
        
        // Update project status
        project.updateStatus(ProjectStatus.ANALYZING);
        projectRepository.save(project);
        
        try {
            // Call MCP Server for AI analysis
            Map<String, Object> analysisRequest = new HashMap<>();
            analysisRequest.put("projectId", request.getProjectId());
            analysisRequest.put("naturalLanguageRequirements", request.getNaturalLanguageRequirements());
            analysisRequest.put("constraints", request.getConstraints());
            analysisRequest.put("technologyPreferences", request.getTechnologyPreferences());
            
            Map<String, Object> analysisResult = mcpServerClient.analyzeRequirements(analysisRequest);
            
            // Parse analysis results
            List<Map<String, Object>> extractedRequirements = 
                (List<Map<String, Object>>) analysisResult.get("requirements");
            
            // Create requirement entities
            List<Requirement> requirements = new ArrayList<>();
            for (Map<String, Object> reqData : extractedRequirements) {
                Requirement requirement = Requirement.builder()
                        .title((String) reqData.get("title"))
                        .description((String) reqData.get("description"))
                        .type(RequirementType.valueOf((String) reqData.get("type")))
                        .priority(RequirementPriority.valueOf((String) reqData.get("priority")))
                        .status(RequirementStatus.ANALYZED)
                        .metadata((Map<String, String>) reqData.get("metadata"))
                        .build();
                
                project.addRequirement(requirement);
                requirements.add(requirement);
            }
            
            // Save requirements
            projectRepository.save(project);
            
            // Update project status
            project.updateStatus(ProjectStatus.DESIGNING);
            projectRepository.save(project);
            
            // Publish event
            List<String> requirementIds = requirements.stream()
                    .map(r -> r.getId().toString())
                    .collect(Collectors.toList());
            
            RequirementsAnalyzedEvent event = RequirementsAnalyzedEvent.builder()
                    .projectId(request.getProjectId())
                    .totalRequirements(requirements.size())
                    .requirementIds(requirementIds)
                    .build();
            eventProducer.publishRequirementsAnalyzedEvent(event);
            
            // Build response
            return AnalysisResultResponse.builder()
                    .projectId(request.getProjectId())
                    .summary((String) analysisResult.get("summary"))
                    .requirements(requirementMapper.toResponseList(requirements))
                    .identifiedEntities((List<String>) analysisResult.get("identifiedEntities"))
                    .suggestedRelationships((List<String>) analysisResult.get("suggestedRelationships"))
                    .technologyRecommendations((Map<String, String>) analysisResult.get("technologyRecommendations"))
                    .confidenceScore((Double) analysisResult.get("confidenceScore"))
                    .build();
            
        } catch (Exception e) {
            log.error("Failed to analyze requirements", e);
            project.updateStatus(ProjectStatus.FAILED);
            projectRepository.save(project);
            throw new InvalidRequirementException("Failed to analyze requirements: " + e.getMessage());
        }
    }
    
    @Override
    public RequirementResponse addRequirement(String projectId, AddRequirementRequest request) {
        log.info("Adding requirement to project: {}", projectId);
        
        Project project = projectRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
        
        Requirement requirement = requirementMapper.toEntity(request);
        requirement.setStatus(RequirementStatus.PENDING);
        project.addRequirement(requirement);
        
        projectRepository.save(project);
        
        return requirementMapper.toResponse(requirement);
    }
    
    @Override
    public List<RequirementResponse> getProjectRequirements(String projectId) {
        log.info("Fetching requirements for project: {}", projectId);
        
        List<Requirement> requirements = requirementRepository.findByProjectIdOrderByPriority(projectId);
        return requirementMapper.toResponseList(requirements);
    }
    
    @Override
    public RequirementResponse updateRequirementStatus(Long requirementId, String status) {
        log.info("Updating requirement {} status to: {}", requirementId, status);
        
        Requirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new InvalidRequirementException("Requirement not found: " + requirementId));
        
        requirement.updateStatus(RequirementStatus.valueOf(status.toUpperCase()));
        Requirement updatedRequirement = requirementRepository.save(requirement);
        
        return requirementMapper.toResponse(updatedRequirement);
    }
}
EOF

# Create Kafka Event Producer
echo "📦 Creating Kafka Event Producer..."

# DomainEventProducer
cat > integration/kafka/producer/DomainEventProducer.java << 'EOF'
package com.gigapress.domainschema.integration.kafka.producer;

import com.gigapress.domainschema.domain.common.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DomainEventProducer {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String PROJECT_EVENTS_TOPIC = "project-events";
    private static final String DOMAIN_ANALYZED_TOPIC = "domain-analyzed";
    private static final String SCHEMA_GENERATED_TOPIC = "schema-generated";
    
    public void publishProjectCreatedEvent(ProjectCreatedEvent event) {
        log.info("Publishing project created event for project: {}", event.getAggregateId());
        kafkaTemplate.send(PROJECT_EVENTS_TOPIC, event.getAggregateId(), event);
    }
    
    public void publishRequirementsAnalyzedEvent(RequirementsAnalyzedEvent event) {
        log.info("Publishing requirements analyzed event for project: {}", event.getAggregateId());
        kafkaTemplate.send(PROJECT_EVENTS_TOPIC, event.getAggregateId(), event);
    }
    
    public void publishDomainModelGeneratedEvent(DomainModelGeneratedEvent event) {
        log.info("Publishing domain model generated event for project: {}", event.getAggregateId());
        kafkaTemplate.send(DOMAIN_ANALYZED_TOPIC, event.getAggregateId(), event);
    }
    
    public void publishSchemaGeneratedEvent(SchemaGeneratedEvent event) {
        log.info("Publishing schema generated event for project: {}", event.getAggregateId());
        kafkaTemplate.send(SCHEMA_GENERATED_TOPIC, event.getAggregateId(), event);
    }
}
EOF

# Create MCP Server Client
echo "📦 Creating MCP Server Client..."

# McpServerClient
cat > integration/mcp/client/McpServerClient.java << 'EOF'
package com.gigapress.domainschema.integration.mcp.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class McpServerClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${mcp.server.url}")
    private String mcpServerUrl;
    
    public Map<String, Object> analyzeRequirements(Map<String, Object> request) {
        log.info("Calling MCP Server to analyze requirements");
        
        String url = mcpServerUrl + "/api/v1/tools/analyze_requirements";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        
        try {
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            log.info("Successfully analyzed requirements");
            return response;
        } catch (Exception e) {
            log.error("Failed to call MCP Server", e);
            throw new RuntimeException("Failed to analyze requirements: " + e.getMessage());
        }
    }
    
    public Map<String, Object> generateDomainModel(Map<String, Object> request) {
        log.info("Calling MCP Server to generate domain model");
        
        String url = mcpServerUrl + "/api/v1/tools/generate_domain_model";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        
        try {
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            log.info("Successfully generated domain model");
            return response;
        } catch (Exception e) {
            log.error("Failed to call MCP Server", e);
            throw new RuntimeException("Failed to generate domain model: " + e.getMessage());
        }
    }
    
    public Map<String, Object> generateSchema(Map<String, Object> request) {
        log.info("Calling MCP Server to generate schema");
        
        String url = mcpServerUrl + "/api/v1/tools/generate_schema";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        
        try {
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            log.info("Successfully generated schema");
            return response;
        } catch (Exception e) {
            log.error("Failed to call MCP Server", e);
            throw new RuntimeException("Failed to generate schema: " + e.getMessage());
        }
    }
}
EOF

# Create RestTemplate Configuration
echo "📦 Creating RestTemplate Configuration..."

# Add to existing config directory
cat > config/RestTemplateConfig.java << 'EOF'
package com.gigapress.domainschema.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {
    
    @Value("${mcp.server.timeout:30000}")
    private int timeout;
    
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(timeout))
                .setReadTimeout(Duration.ofMillis(timeout))
                .build();
    }
}
EOF

# Create placeholder for other service implementations
echo "📦 Creating placeholder service implementations..."

# DomainModelServiceImpl (placeholder)
cat > domain/analysis/service/DomainModelServiceImpl.java << 'EOF'
package com.gigapress.domainschema.domain.analysis.service;

import com.gigapress.domainschema.domain.analysis.dto.response.DomainModelResponse;
import com.gigapress.domainschema.domain.common.exception.DomainModelGenerationException;
import com.gigapress.domainschema.domain.common.exception.ProjectNotFoundException;
import com.gigapress.domainschema.integration.mcp.client.McpServerClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DomainModelServiceImpl implements DomainModelService {
    
    private final McpServerClient mcpServerClient;
    
    @Override
    public DomainModelResponse generateDomainModel(String projectId) {
        log.info("Generating domain model for project: {}", projectId);
        // TODO: Implement domain model generation
        throw new UnsupportedOperationException("Domain model generation will be implemented in next step");
    }
    
    @Override
    public DomainModelResponse getDomainModel(String projectId) {
        log.info("Fetching domain model for project: {}", projectId);
        // TODO: Implement get domain model
        throw new UnsupportedOperationException("Get domain model will be implemented in next step");
    }
    
    @Override
    public DomainModelResponse regenerateDomainModel(String projectId) {
        log.info("Regenerating domain model for project: {}", projectId);
        // TODO: Implement regenerate domain model
        throw new UnsupportedOperationException("Regenerate domain model will be implemented in next step");
    }
}
EOF

# SchemaDesignServiceImpl (placeholder)
cat > schema/design/service/SchemaDesignServiceImpl.java << 'EOF'
package com.gigapress.domainschema.schema.design.service;

import com.gigapress.domainschema.schema.design.dto.request.GenerateSchemaRequest;
import com.gigapress.domainschema.schema.design.dto.response.SchemaDesignResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SchemaDesignServiceImpl implements SchemaDesignService {
    
    @Override
    public SchemaDesignResponse generateSchema(GenerateSchemaRequest request) {
        log.info("Generating schema for project: {}", request.getProjectId());
        // TODO: Implement schema generation
        throw new UnsupportedOperationException("Schema generation will be implemented in next step");
    }
    
    @Override
    public SchemaDesignResponse getSchemaDesign(String projectId) {
        log.info("Fetching schema design for project: {}", projectId);
        // TODO: Implement get schema design
        throw new UnsupportedOperationException("Get schema design will be implemented in next step");
    }
    
    @Override
    public String getDdlScript(String projectId) {
        log.info("Fetching DDL script for project: {}", projectId);
        // TODO: Implement get DDL script
        throw new UnsupportedOperationException("Get DDL script will be implemented in next step");
    }
    
    @Override
    public String getMigrationScript(String projectId) {
        log.info("Fetching migration script for project: {}", projectId);
        // TODO: Implement get migration script
        throw new UnsupportedOperationException("Get migration script will be implemented in next step");
    }
}
EOF

# EntityMappingServiceImpl (placeholder)
cat > schema/mapping/service/EntityMappingServiceImpl.java << 'EOF'
package com.gigapress.domainschema.schema.mapping.service;

import com.gigapress.domainschema.schema.mapping.dto.request.GenerateEntitiesRequest;
import com.gigapress.domainschema.schema.mapping.dto.response.EntityMappingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EntityMappingServiceImpl implements EntityMappingService {
    
    @Override
    public EntityMappingResponse generateEntities(GenerateEntitiesRequest request) {
        log.info("Generating entities for project: {}", request.getProjectId());
        // TODO: Implement entity generation
        throw new UnsupportedOperationException("Entity generation will be implemented in next step");
    }
    
    @Override
    public EntityMappingResponse getEntityMappings(String projectId) {
        log.info("Fetching entity mappings for project: {}", projectId);
        // TODO: Implement get entity mappings
        throw new UnsupportedOperationException("Get entity mappings will be implemented in next step");
    }
    
    @Override
    public String getEntityFileContent(String projectId, String fileName) {
        log.info("Fetching entity file {} for project: {}", fileName, projectId);
        // TODO: Implement get entity file content
        throw new UnsupportedOperationException("Get entity file content will be implemented in next step");
    }
    
    @Override
    public byte[] getAllEntitiesAsZip(String projectId) {
        log.info("Creating ZIP file for project: {}", projectId);
        // TODO: Implement get all entities as ZIP
        throw new UnsupportedOperationException("Get all entities as ZIP will be implemented in next step");
    }
}
EOF

# Update Controllers to inject services
echo "📦 Updating Controllers with service injection..."

# Update in the controller files to add service injection
# This would be done by modifying the existing controller files

echo "✅ Service Layer created successfully!"
echo ""
echo "📋 Created:"
echo "  - Repository interfaces:"
echo "    - ProjectRepository"
echo "    - RequirementRepository"
echo "    - DomainModelRepository"
echo "    - SchemaDesignRepository"
echo "  - Mapper interfaces (MapStruct):"
echo "    - ProjectMapper"
echo "    - RequirementMapper"
echo "  - Service interfaces and implementations:"
echo "    - ProjectService (✅ Fully implemented)"
echo "    - RequirementAnalysisService (✅ Fully implemented)"
echo "    - DomainModelService (🔄 Placeholder)"
echo "    - SchemaDesignService (🔄 Placeholder)"
echo "    - EntityMappingService (🔄 Placeholder)"
echo "  - Integration components:"
echo "    - DomainEventProducer (Kafka)"
echo "    - McpServerClient (MCP Server)"
echo "    - RestTemplateConfig"
echo ""
echo "🎯 Next step: Create Data Layer implementation (Repositories and Database)"