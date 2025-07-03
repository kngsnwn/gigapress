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
