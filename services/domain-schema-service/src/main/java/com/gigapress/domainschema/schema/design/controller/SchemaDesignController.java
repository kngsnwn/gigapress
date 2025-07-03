package com.gigapress.domainschema.schema.design.controller;

import com.gigapress.domainschema.domain.common.dto.ApiResponse;
import com.gigapress.domainschema.schema.design.dto.request.GenerateSchemaRequest;
import com.gigapress.domainschema.schema.design.dto.response.SchemaDesignResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/schema-designs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Schema Design", description = "APIs for database schema design and generation")
public class SchemaDesignController {
    
    @PostMapping
    @Operation(summary = "Generate database schema", 
              description = "Generates database schema based on domain model")
    public ResponseEntity<ApiResponse<SchemaDesignResponse>> generateSchema(
            @Valid @RequestBody GenerateSchemaRequest request) {
        log.info("Generating schema for project: {}", request.getProjectId());
        
        // TODO: Implement service call
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, "Schema generated successfully"));
    }
    
    @GetMapping("/{projectId}")
    @Operation(summary = "Get schema design", description = "Retrieves the schema design for a project")
    public ResponseEntity<ApiResponse<SchemaDesignResponse>> getSchemaDesign(
            @Parameter(description = "Project ID", example = "proj_123456")
            @PathVariable String projectId) {
        log.info("Fetching schema design for project: {}", projectId);
        
        // TODO: Implement service call
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @GetMapping("/{projectId}/ddl")
    @Operation(summary = "Get DDL script", description = "Downloads the complete DDL script for the schema")
    public ResponseEntity<String> getDdlScript(
            @Parameter(description = "Project ID", example = "proj_123456")
            @PathVariable String projectId) {
        log.info("Fetching DDL script for project: {}", projectId);
        
        // TODO: Implement service call
        String ddlScript = "-- DDL Script placeholder";
        
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header("Content-Disposition", "attachment; filename=\"schema_" + projectId + ".sql\"")
                .body(ddlScript);
    }
    
    @GetMapping("/{projectId}/migration")
    @Operation(summary = "Get migration script", 
              description = "Downloads the database migration script")
    public ResponseEntity<String> getMigrationScript(
            @Parameter(description = "Project ID", example = "proj_123456")
            @PathVariable String projectId) {
        log.info("Fetching migration script for project: {}", projectId);
        
        // TODO: Implement service call
        String migrationScript = "-- Migration Script placeholder";
        
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header("Content-Disposition", "attachment; filename=\"migration_" + projectId + ".sql\"")
                .body(migrationScript);
    }
}
