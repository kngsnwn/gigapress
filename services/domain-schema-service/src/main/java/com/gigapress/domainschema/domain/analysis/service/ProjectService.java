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
