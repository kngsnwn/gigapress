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
