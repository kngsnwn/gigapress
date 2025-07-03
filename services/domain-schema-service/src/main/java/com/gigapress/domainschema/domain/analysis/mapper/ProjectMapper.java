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
