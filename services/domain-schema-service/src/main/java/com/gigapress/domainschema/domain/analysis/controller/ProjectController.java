package com.gigapress.domainschema.domain.analysis.controller;

import com.gigapress.domainschema.domain.analysis.dto.request.CreateProjectRequest;
import com.gigapress.domainschema.domain.analysis.dto.response.ProjectResponse;
import com.gigapress.domainschema.domain.common.dto.ApiResponse;
import com.gigapress.domainschema.domain.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Project Management", description = "APIs for managing projects")
public class ProjectController {
    
    // Service injection will be added in Step 4
    
    @PostMapping
    @Operation(summary = "Create a new project", description = "Creates a new project for domain and schema analysis")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody CreateProjectRequest request) {
        log.info("Creating new project: {}", request.getName());
        
        // TODO: Implement service call
        ProjectResponse response = ProjectResponse.builder()
                .projectId("proj_" + System.currentTimeMillis())
                .name(request.getName())
                .description(request.getDescription())
                .projectType(request.getProjectType())
                .build();
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Project created successfully"));
    }
    
    @GetMapping("/{projectId}")
    @Operation(summary = "Get project by ID", description = "Retrieves project details by project ID")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProject(
            @Parameter(description = "Project ID", example = "proj_123456")
            @PathVariable String projectId) {
        log.info("Fetching project: {}", projectId);
        
        // TODO: Implement service call
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @GetMapping
    @Operation(summary = "List all projects", description = "Retrieves a paginated list of all projects")
    public ResponseEntity<ApiResponse<PageResponse<ProjectResponse>>> listProjects(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String status) {
        log.info("Listing projects with status: {}", status);
        
        // TODO: Implement service call
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @DeleteMapping("/{projectId}")
    @Operation(summary = "Delete project", description = "Deletes a project and all associated data")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @Parameter(description = "Project ID", example = "proj_123456")
            @PathVariable String projectId) {
        log.info("Deleting project: {}", projectId);
        
        // TODO: Implement service call
        return ResponseEntity.ok(ApiResponse.success(null, "Project deleted successfully"));
    }
}
