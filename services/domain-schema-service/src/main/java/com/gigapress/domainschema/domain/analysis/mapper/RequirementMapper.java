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
