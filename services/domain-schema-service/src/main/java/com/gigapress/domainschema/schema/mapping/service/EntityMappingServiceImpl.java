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
