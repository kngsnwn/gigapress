package com.gigapress.domainschema.domain.analysis.controller;

import com.gigapress.domainschema.domain.analysis.dto.request.AddRequirementRequest;
import com.gigapress.domainschema.domain.analysis.dto.request.AnalyzeRequirementsRequest;
import com.gigapress.domainschema.domain.analysis.dto.response.AnalysisResultResponse;
import com.gigapress.domainschema.domain.analysis.dto.response.RequirementResponse;
import com.gigapress.domainschema.domain.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/requirements")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Requirements Analysis", description = "APIs for analyzing and managing requirements")
public class RequirementsController {
    
    @PostMapping("/analyze")
    @Operation(summary = "Analyze natural language requirements", 
              description = "Analyzes natural language requirements and extracts structured requirements")
    public ResponseEntity<ApiResponse<AnalysisResultResponse>> analyzeRequirements(
            @Valid @RequestBody AnalyzeRequirementsRequest request) {
        log.info("Analyzing requirements for project: {}", request.getProjectId());
        
        // TODO: Implement service call
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, "Requirements analyzed successfully"));
    }
    
    @PostMapping("/{projectId}")
    @Operation(summary = "Add requirement to project", description = "Manually adds a requirement to an existing project")
    public ResponseEntity<ApiResponse<RequirementResponse>> addRequirement(
            @Parameter(description = "Project ID", example = "proj_123456")
            @PathVariable String projectId,
            @Valid @RequestBody AddRequirementRequest request) {
        log.info("Adding requirement to project: {}", projectId);
        
        // TODO: Implement service call
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, "Requirement added successfully"));
    }
    
    @GetMapping("/{projectId}")
    @Operation(summary = "List project requirements", description = "Retrieves all requirements for a project")
    public ResponseEntity<ApiResponse<List<RequirementResponse>>> getProjectRequirements(
            @Parameter(description = "Project ID", example = "proj_123456")
            @PathVariable String projectId) {
        log.info("Fetching requirements for project: {}", projectId);
        
        // TODO: Implement service call
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @PutMapping("/{requirementId}/status")
    @Operation(summary = "Update requirement status", description = "Updates the status of a specific requirement")
    public ResponseEntity<ApiResponse<RequirementResponse>> updateRequirementStatus(
            @Parameter(description = "Requirement ID")
            @PathVariable Long requirementId,
            @RequestParam String status) {
        log.info("Updating requirement {} status to: {}", requirementId, status);
        
        // TODO: Implement service call
        return ResponseEntity.ok(ApiResponse.success(null, "Status updated successfully"));
    }
}
