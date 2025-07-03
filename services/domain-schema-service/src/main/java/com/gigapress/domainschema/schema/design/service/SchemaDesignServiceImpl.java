package com.gigapress.domainschema.schema.design.service;

import com.gigapress.domainschema.schema.design.dto.request.GenerateSchemaRequest;
import com.gigapress.domainschema.schema.design.dto.response.SchemaDesignResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SchemaDesignServiceImpl implements SchemaDesignService {
    
    @Override
    public SchemaDesignResponse generateSchema(GenerateSchemaRequest request) {
        log.info("Generating schema for project: {}", request.getProjectId());
        // TODO: Implement schema generation
        throw new UnsupportedOperationException("Schema generation will be implemented in next step");
    }
    
    @Override
    public SchemaDesignResponse getSchemaDesign(String projectId) {
        log.info("Fetching schema design for project: {}", projectId);
        // TODO: Implement get schema design
        throw new UnsupportedOperationException("Get schema design will be implemented in next step");
    }
    
    @Override
    public String getDdlScript(String projectId) {
        log.info("Fetching DDL script for project: {}", projectId);
        // TODO: Implement get DDL script
        throw new UnsupportedOperationException("Get DDL script will be implemented in next step");
    }
    
    @Override
    public String getMigrationScript(String projectId) {
        log.info("Fetching migration script for project: {}", projectId);
        // TODO: Implement get migration script
        throw new UnsupportedOperationException("Get migration script will be implemented in next step");
    }
}
