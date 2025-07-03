package com.gigapress.domainschema.schema.mapping.service;

import com.gigapress.domainschema.schema.mapping.dto.request.GenerateEntitiesRequest;
import com.gigapress.domainschema.schema.mapping.dto.response.EntityMappingResponse;

public interface EntityMappingService {
    
    EntityMappingResponse generateEntities(GenerateEntitiesRequest request);
    
    EntityMappingResponse getEntityMappings(String projectId);
    
    String getEntityFileContent(String projectId, String fileName);
    
    byte[] getAllEntitiesAsZip(String projectId);
}
