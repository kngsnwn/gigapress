package com.gigapress.backend.controller;

import com.gigapress.backend.dto.BusinessLogicRequest;
import com.gigapress.backend.dto.GeneratedBusinessLogic;
import com.gigapress.backend.model.BusinessLogicPattern;
import com.gigapress.backend.service.BusinessLogicGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/business-logic")
@RequiredArgsConstructor
@Tag(name = "Business Logic Generation", description = "Business logic pattern generation endpoints")
public class BusinessLogicController {

    private final BusinessLogicGenerationService businessLogicGenerationService;

    @PostMapping("/generate")
    @Operation(summary = "Generate business logic", description = "Generate business logic based on pattern")
    public ResponseEntity<GeneratedBusinessLogic> generateBusinessLogic(@RequestBody BusinessLogicRequest request) {
        GeneratedBusinessLogic result = businessLogicGenerationService.generateBusinessLogic(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/patterns")
    @Operation(summary = "Get available patterns", description = "Get list of available business logic patterns")
    public ResponseEntity<List<String>> getAvailablePatterns() {
        List<String> patterns = Arrays.stream(BusinessLogicPattern.PatternType.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(patterns);
    }
}
