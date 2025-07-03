package com.gigapress.domainschema.domain.analysis.service;

import com.gigapress.domainschema.domain.analysis.dto.response.DomainModelResponse;

public interface DomainModelService {
    
    DomainModelResponse generateDomainModel(String projectId);
    
    DomainModelResponse getDomainModel(String projectId);
    
    DomainModelResponse regenerateDomainModel(String projectId);
}
