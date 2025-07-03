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
