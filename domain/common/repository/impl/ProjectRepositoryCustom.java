package com.gigapress.domainschema.domain.common.repository.impl;

import com.gigapress.domainschema.domain.common.entity.Project;
import com.gigapress.domainschema.domain.common.entity.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ProjectRepositoryCustom {
    
    Page<Project> findProjectsWithFilters(Map<String, Object> filters, Pageable pageable);
    
    List<Project> findProjectsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    Map<ProjectStatus, Long> getProjectStatusStatistics();
    
    void updateProjectStatusBulk(List<String> projectIds, ProjectStatus newStatus);
}
