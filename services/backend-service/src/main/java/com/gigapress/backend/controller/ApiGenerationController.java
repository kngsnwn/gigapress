package com.gigapress.backend.controller;

import com.gigapress.backend.dto.ApiSpecification;
import com.gigapress.backend.dto.GeneratedApi;
import com.gigapress.backend.service.ApiGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/generation")
@RequiredArgsConstructor
@Tag(name = "API Generation", description = "API Generation endpoints")
public class ApiGenerationController {

    private final ApiGenerationService apiGenerationService;

    @PostMapping("/generate")
    @Operation(summary = "Generate API endpoints", description = "Generate REST API endpoints based on specification")
    public ResponseEntity<GeneratedApi> generateApi(@RequestBody ApiSpecification specification) {
        GeneratedApi generatedApi = apiGenerationService.generateApiEndpoints(specification);
        return ResponseEntity.ok(generatedApi);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the service is running")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Backend Service is running");
    }
}
