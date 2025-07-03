package com.gigapress.mcp.controller;

import com.gigapress.mcp.model.request.*;
import com.gigapress.mcp.model.response.*;
import com.gigapress.mcp.service.*;
import io.swagger.v3.oas.annotations.Operation;
import com.gigapress.mcp.model.response.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;
import lombok.Data;
import lombok.Builder;
@Slf4j
@RestController
@RequestMapping("/api/tools")
@RequiredArgsConstructor
@Tag(name = "MCP Tools", description = "Core tool APIs for project management")
public class ToolController {
    
    private final ChangeAnalysisService changeAnalysisService;
    private final ProjectGenerationService projectGenerationService;
    private final ComponentUpdateService componentUpdateService;
    private final ConsistencyValidationService consistencyValidationService;
    
    @PostMapping("/analyze")
    @Operation(summary = "Analyze change impact", 
              description = "Analyzes the impact of proposed changes on the project")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Analysis completed successfully",
                    content = @Content(schema = @Schema(implementation = ChangeAnalysisResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<ResponseEntity<com.gigapress.mcp.model.response.ApiResponse<ChangeAnalysisResponse>>> analyzeChangeImpact(
            @Valid @RequestBody ChangeAnalysisRequest request,
            @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
        
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }
        final String finalRequestId = requestId;
        
        log.info("Analyzing change impact for project: {} [Request ID: {}]", 
                request.getProjectId(), requestId);
        
        return changeAnalysisService.analyzeChange(request)
            .map(response -> {
                ApiResponse<ChangeAnalysisResponse> apiResponse = ApiResponse.<ChangeAnalysisResponse>builder()
                    .success(true)
                    .data(response)
                    .requestId(finalRequestId)
                    .build();
                return ResponseEntity.ok(apiResponse);
            })
            .onErrorResume(error -> {
                log.error("Error analyzing change impact", error);
                ApiResponse<ChangeAnalysisResponse> errorResponse = ApiResponse.error(
                    "Failed to analyze change impact: " + error.getMessage(),
                    "ANALYSIS_ERROR"
                );
                errorResponse.setRequestId(finalRequestId);
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse));
            });
    }
    
    @PostMapping("/generate")
    @Operation(summary = "Generate project structure", 
              description = "Generates a new project structure based on requirements")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Project generated successfully",
                    content = @Content(schema = @Schema(implementation = ProjectGenerationResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<ResponseEntity<ApiResponse<ProjectGenerationResponse>>> generateProjectStructure(
            @Valid @RequestBody ProjectGenerationRequest request,
            @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
        
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }
        final String finalRequestId = requestId;
        
        log.info("Generating project structure: {} [Request ID: {}]", 
                request.getProjectName(), requestId);
        
        return projectGenerationService.generateProject(request)
            .map(response -> {
                ApiResponse<ProjectGenerationResponse> apiResponse = ApiResponse.<ProjectGenerationResponse>builder()
                    .success(true)
                    .data(response)
                    .requestId(finalRequestId)
                    .build();
                return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
            })
            .onErrorResume(error -> {
                log.error("Error generating project", error);
                ApiResponse<ProjectGenerationResponse> errorResponse = ApiResponse.error(
                    "Failed to generate project: " + error.getMessage(),
                    "GENERATION_ERROR"
                );
                errorResponse.setRequestId(finalRequestId);
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse));
            });
    }
    
    @PutMapping("/update")
    @Operation(summary = "Update components", 
              description = "Updates one or more components in the project")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Components updated successfully",
                    content = @Content(schema = @Schema(implementation = ComponentUpdateResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<ResponseEntity<ApiResponse<ComponentUpdateResponse>>> updateComponents(
            @Valid @RequestBody ComponentUpdateRequest request,
            @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
        
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }
        final String finalRequestId = requestId;
        
        log.info("Updating components for project: {} [Request ID: {}]", 
                request.getProjectId(), requestId);
        
        return componentUpdateService.updateComponents(request)
            .map(response -> {
                ApiResponse<ComponentUpdateResponse> apiResponse = ApiResponse.<ComponentUpdateResponse>builder()
                    .success(true)
                    .data(response)
                    .requestId(finalRequestId)
                    .build();
                return ResponseEntity.ok(apiResponse);
            })
            .onErrorResume(error -> {
                log.error("Error updating components", error);
                ApiResponse<ComponentUpdateResponse> errorResponse = ApiResponse.error(
                    "Failed to update components: " + error.getMessage(),
                    "UPDATE_ERROR"
                );
                errorResponse.setRequestId(finalRequestId);
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse));
            });
    }
    
    @PostMapping("/validate")
    @Operation(summary = "Validate project consistency", 
              description = "Validates various aspects of project consistency")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Validation completed",
                    content = @Content(schema = @Schema(implementation = ValidationResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<ResponseEntity<ApiResponse<ValidationResponse>>> validateConsistency(
            @Valid @RequestBody ValidationRequest request,
            @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
        
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }
        final String finalRequestId = requestId;
        
        log.info("Validating consistency for project: {} [Request ID: {}]", 
                request.getProjectId(), requestId);
        
        return consistencyValidationService.validateConsistency(request)
            .map(response -> {
                ApiResponse<ValidationResponse> apiResponse = ApiResponse.<ValidationResponse>builder()
                    .success(true)
                    .data(response)
                    .requestId(finalRequestId)
                    .build();
                
                HttpStatus status = response.getValidationStatus() == 
                    ValidationResponse.ValidationStatus.FAILED ? 
                    HttpStatus.UNPROCESSABLE_ENTITY : HttpStatus.OK;
                
                return ResponseEntity.status(status).body(apiResponse);
            })
            .onErrorResume(error -> {
                log.error("Error validating consistency", error);
                ApiResponse<ValidationResponse> errorResponse = ApiResponse.error(
                    "Failed to validate consistency: " + error.getMessage(),
                    "VALIDATION_ERROR"
                );
                errorResponse.setRequestId(finalRequestId);
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse));
            });
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the tools API is healthy")
    public ResponseEntity<ApiResponse<HealthStatus>> healthCheck() {
        HealthStatus status = HealthStatus.builder()
            .status("UP")
            .service("MCP Tools API")
            .version("1.0.0")
            .build();
        
        return ResponseEntity.ok(ApiResponse.success(status));
    }
    
    @Data
    @Builder
    public static class HealthStatus {
        private String status;
        private String service;
        private String version;
    }
}
