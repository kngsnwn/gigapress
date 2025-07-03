package com.gigapress.domainschema.schema.design.service;

import com.gigapress.domainschema.schema.design.dto.request.GenerateSchemaRequest;
import com.gigapress.domainschema.schema.design.dto.response.SchemaDesignResponse;

public interface SchemaDesignService {
    
    SchemaDesignResponse generateSchema(GenerateSchemaRequest request);
    
    SchemaDesignResponse getSchemaDesign(String projectId);
    
    String getDdlScript(String projectId);
    
    String getMigrationScript(String projectId);
}
