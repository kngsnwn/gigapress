#!/bin/bash

# Backend Service Enhancement Script - Step 2
# This script adds business logic templates and API pattern support

echo "🚀 Enhancing Backend Service with Business Logic Templates..."

# Ensure we're in the correct directory
cd services/backend-service

# Create additional model classes
cat > src/main/java/com/gigapress/backend/model/BusinessLogicPattern.java << 'EOF'
package com.gigapress.backend.model;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class BusinessLogicPattern {
    private String patternName;
    private PatternType type;
    private List<String> requiredDependencies;
    private Map<String, Object> configuration;
    
    public enum PatternType {
        CRUD,
        SEARCH_AND_FILTER,
        BATCH_PROCESSING,
        WORKFLOW,
        NOTIFICATION,
        INTEGRATION,
        REPORT_GENERATION,
        FILE_PROCESSING,
        ASYNC_OPERATION,
        EVENT_DRIVEN
    }
}
EOF

cat > src/main/java/com/gigapress/backend/model/ApiPattern.java << 'EOF'
package com.gigapress.backend.model;

import lombok.Data;
import java.util.List;

@Data
public class ApiPattern {
    private String name;
    private String description;
    private ApiType apiType;
    private List<EndpointDefinition> endpoints;
    private SecurityRequirement security;
    private RateLimitConfig rateLimit;
    
    public enum ApiType {
        REST,
        GRAPHQL,
        GRPC,
        WEBSOCKET,
        SSE
    }
    
    @Data
    public static class EndpointDefinition {
        private String method;
        private String path;
        private String description;
        private List<Parameter> parameters;
        private RequestBody requestBody;
        private ResponseSpec response;
    }
    
    @Data
    public static class Parameter {
        private String name;
        private String in; // path, query, header
        private String type;
        private boolean required;
        private String description;
    }
    
    @Data
    public static class RequestBody {
        private String contentType;
        private String schemaRef;
    }
    
    @Data
    public static class ResponseSpec {
        private int statusCode;
        private String contentType;
        private String schemaRef;
    }
    
    @Data
    public static class SecurityRequirement {
        private boolean enabled;
        private List<String> scopes;
        private String authType;
    }
    
    @Data
    public static class RateLimitConfig {
        private boolean enabled;
        private int requestsPerMinute;
        private String keyResolver;
    }
}
EOF

# Create Business Logic Generation Service
cat > src/main/java/com/gigapress/backend/service/BusinessLogicGenerationService.java << 'EOF'
package com.gigapress.backend.service;

import com.gigapress.backend.dto.BusinessLogicRequest;
import com.gigapress.backend.dto.GeneratedBusinessLogic;
import com.gigapress.backend.model.BusinessLogicPattern;
import com.gigapress.backend.template.BusinessLogicTemplateEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessLogicGenerationService {

    private final BusinessLogicTemplateEngine templateEngine;
    private final ValidationService validationService;
    private final KafkaProducerService kafkaProducerService;

    public GeneratedBusinessLogic generateBusinessLogic(BusinessLogicRequest request) {
        log.info("Generating business logic for pattern: {}", request.getPatternType());
        
        // Validate request
        validationService.validateBusinessLogicRequest(request);
        
        GeneratedBusinessLogic result = new GeneratedBusinessLogic();
        result.setPatternType(request.getPatternType());
        
        switch (request.getPatternType()) {
            case CRUD:
                result = generateCrudLogic(request);
                break;
            case SEARCH_AND_FILTER:
                result = generateSearchLogic(request);
                break;
            case BATCH_PROCESSING:
                result = generateBatchLogic(request);
                break;
            case WORKFLOW:
                result = generateWorkflowLogic(request);
                break;
            case NOTIFICATION:
                result = generateNotificationLogic(request);
                break;
            case INTEGRATION:
                result = generateIntegrationLogic(request);
                break;
            case REPORT_GENERATION:
                result = generateReportLogic(request);
                break;
            case FILE_PROCESSING:
                result = generateFileProcessingLogic(request);
                break;
            case ASYNC_OPERATION:
                result = generateAsyncLogic(request);
                break;
            case EVENT_DRIVEN:
                result = generateEventDrivenLogic(request);
                break;
            default:
                throw new IllegalArgumentException("Unknown pattern type: " + request.getPatternType());
        }
        
        // Send event
        kafkaProducerService.sendBusinessLogicGeneratedEvent(result);
        
        return result;
    }

    private GeneratedBusinessLogic generateCrudLogic(BusinessLogicRequest request) {
        Map<String, String> generatedCode = new HashMap<>();
        
        // Generate entity
        generatedCode.put("entity", templateEngine.generateEntity(request));
        
        // Generate service with CRUD operations
        generatedCode.put("service", templateEngine.generateCrudService(request));
        
        // Generate repository with custom queries
        generatedCode.put("repository", templateEngine.generateCrudRepository(request));
        
        // Generate DTOs
        generatedCode.put("createDto", templateEngine.generateCreateDto(request));
        generatedCode.put("updateDto", templateEngine.generateUpdateDto(request));
        generatedCode.put("responseDto", templateEngine.generateResponseDto(request));
        
        // Generate mapper
        generatedCode.put("mapper", templateEngine.generateMapper(request));
        
        return GeneratedBusinessLogic.builder()
                .patternType(BusinessLogicPattern.PatternType.CRUD)
                .generatedCode(generatedCode)
                .documentation(generateDocumentation(request))
                .tests(generateTests(request))
                .build();
    }

    private GeneratedBusinessLogic generateSearchLogic(BusinessLogicRequest request) {
        Map<String, String> generatedCode = new HashMap<>();
        
        // Generate search specifications
        generatedCode.put("specification", templateEngine.generateSearchSpecification(request));
        
        // Generate search service
        generatedCode.put("searchService", templateEngine.generateSearchService(request));
        
        // Generate search DTOs
        generatedCode.put("searchCriteria", templateEngine.generateSearchCriteria(request));
        generatedCode.put("searchResult", templateEngine.generateSearchResult(request));
        
        // Generate pagination support
        generatedCode.put("paginationUtil", templateEngine.generatePaginationUtil(request));
        
        return GeneratedBusinessLogic.builder()
                .patternType(BusinessLogicPattern.PatternType.SEARCH_AND_FILTER)
                .generatedCode(generatedCode)
                .documentation(generateDocumentation(request))
                .tests(generateTests(request))
                .build();
    }

    private GeneratedBusinessLogic generateBatchLogic(BusinessLogicRequest request) {
        Map<String, String> generatedCode = new HashMap<>();
        
        // Generate batch processor
        generatedCode.put("batchProcessor", templateEngine.generateBatchProcessor(request));
        
        // Generate batch configuration
        generatedCode.put("batchConfig", templateEngine.generateBatchConfig(request));
        
        // Generate batch job listener
        generatedCode.put("jobListener", templateEngine.generateJobListener(request));
        
        return GeneratedBusinessLogic.builder()
                .patternType(BusinessLogicPattern.PatternType.BATCH_PROCESSING)
                .generatedCode(generatedCode)
                .documentation(generateDocumentation(request))
                .tests(generateTests(request))
                .build();
    }

    private GeneratedBusinessLogic generateWorkflowLogic(BusinessLogicRequest request) {
        Map<String, String> generatedCode = new HashMap<>();
        
        // Generate workflow engine
        generatedCode.put("workflowEngine", templateEngine.generateWorkflowEngine(request));
        
        // Generate state machine
        generatedCode.put("stateMachine", templateEngine.generateStateMachine(request));
        
        // Generate workflow steps
        generatedCode.put("workflowSteps", templateEngine.generateWorkflowSteps(request));
        
        return GeneratedBusinessLogic.builder()
                .patternType(BusinessLogicPattern.PatternType.WORKFLOW)
                .generatedCode(generatedCode)
                .documentation(generateDocumentation(request))
                .tests(generateTests(request))
                .build();
    }

    private GeneratedBusinessLogic generateNotificationLogic(BusinessLogicRequest request) {
        Map<String, String> generatedCode = new HashMap<>();
        
        // Generate notification service
        generatedCode.put("notificationService", templateEngine.generateNotificationService(request));
        
        // Generate notification templates
        generatedCode.put("emailTemplate", templateEngine.generateEmailTemplate(request));
        generatedCode.put("smsTemplate", templateEngine.generateSmsTemplate(request));
        
        // Generate notification queue handler
        generatedCode.put("queueHandler", templateEngine.generateNotificationQueueHandler(request));
        
        return GeneratedBusinessLogic.builder()
                .patternType(BusinessLogicPattern.PatternType.NOTIFICATION)
                .generatedCode(generatedCode)
                .documentation(generateDocumentation(request))
                .tests(generateTests(request))
                .build();
    }

    private GeneratedBusinessLogic generateIntegrationLogic(BusinessLogicRequest request) {
        Map<String, String> generatedCode = new HashMap<>();
        
        // Generate REST client
        generatedCode.put("restClient", templateEngine.generateRestClient(request));
        
        // Generate circuit breaker
        generatedCode.put("circuitBreaker", templateEngine.generateCircuitBreaker(request));
        
        // Generate retry logic
        generatedCode.put("retryConfig", templateEngine.generateRetryConfig(request));
        
        return GeneratedBusinessLogic.builder()
                .patternType(BusinessLogicPattern.PatternType.INTEGRATION)
                .generatedCode(generatedCode)
                .documentation(generateDocumentation(request))
                .tests(generateTests(request))
                .build();
    }

    private GeneratedBusinessLogic generateReportLogic(BusinessLogicRequest request) {
        Map<String, String> generatedCode = new HashMap<>();
        
        // Generate report service
        generatedCode.put("reportService", templateEngine.generateReportService(request));
        
        // Generate report builder
        generatedCode.put("reportBuilder", templateEngine.generateReportBuilder(request));
        
        // Generate export handlers
        generatedCode.put("pdfExporter", templateEngine.generatePdfExporter(request));
        generatedCode.put("excelExporter", templateEngine.generateExcelExporter(request));
        
        return GeneratedBusinessLogic.builder()
                .patternType(BusinessLogicPattern.PatternType.REPORT_GENERATION)
                .generatedCode(generatedCode)
                .documentation(generateDocumentation(request))
                .tests(generateTests(request))
                .build();
    }

    private GeneratedBusinessLogic generateFileProcessingLogic(BusinessLogicRequest request) {
        Map<String, String> generatedCode = new HashMap<>();
        
        // Generate file handler
        generatedCode.put("fileHandler", templateEngine.generateFileHandler(request));
        
        // Generate file validator
        generatedCode.put("fileValidator", templateEngine.generateFileValidator(request));
        
        // Generate storage service
        generatedCode.put("storageService", templateEngine.generateStorageService(request));
        
        return GeneratedBusinessLogic.builder()
                .patternType(BusinessLogicPattern.PatternType.FILE_PROCESSING)
                .generatedCode(generatedCode)
                .documentation(generateDocumentation(request))
                .tests(generateTests(request))
                .build();
    }

    private GeneratedBusinessLogic generateAsyncLogic(BusinessLogicRequest request) {
        Map<String, String> generatedCode = new HashMap<>();
        
        // Generate async service
        generatedCode.put("asyncService", templateEngine.generateAsyncService(request));
        
        // Generate async configuration
        generatedCode.put("asyncConfig", templateEngine.generateAsyncConfig(request));
        
        // Generate completion handler
        generatedCode.put("completionHandler", templateEngine.generateCompletionHandler(request));
        
        return GeneratedBusinessLogic.builder()
                .patternType(BusinessLogicPattern.PatternType.ASYNC_OPERATION)
                .generatedCode(generatedCode)
                .documentation(generateDocumentation(request))
                .tests(generateTests(request))
                .build();
    }

    private GeneratedBusinessLogic generateEventDrivenLogic(BusinessLogicRequest request) {
        Map<String, String> generatedCode = new HashMap<>();
        
        // Generate event publisher
        generatedCode.put("eventPublisher", templateEngine.generateEventPublisher(request));
        
        // Generate event listener
        generatedCode.put("eventListener", templateEngine.generateEventListener(request));
        
        // Generate event store
        generatedCode.put("eventStore", templateEngine.generateEventStore(request));
        
        return GeneratedBusinessLogic.builder()
                .patternType(BusinessLogicPattern.PatternType.EVENT_DRIVEN)
                .generatedCode(generatedCode)
                .documentation(generateDocumentation(request))
                .tests(generateTests(request))
                .build();
    }

    private String generateDocumentation(BusinessLogicRequest request) {
        return templateEngine.generateDocumentation(request);
    }

    private Map<String, String> generateTests(BusinessLogicRequest request) {
        Map<String, String> tests = new HashMap<>();
        tests.put("unitTest", templateEngine.generateUnitTest(request));
        tests.put("integrationTest", templateEngine.generateIntegrationTest(request));
        return tests;
    }
}
EOF

# Create Business Logic Template Engine
cat > src/main/java/com/gigapress/backend/template/BusinessLogicTemplateEngine.java << 'EOF'
package com.gigapress.backend.template;

import com.gigapress.backend.dto.BusinessLogicRequest;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class BusinessLogicTemplateEngine {

    private final Configuration freemarkerConfig;

    public BusinessLogicTemplateEngine() {
        this.freemarkerConfig = new Configuration(Configuration.VERSION_2_3_32);
        this.freemarkerConfig.setClassForTemplateLoading(this.getClass(), "/templates/business-logic");
        this.freemarkerConfig.setDefaultEncoding("UTF-8");
    }

    // Entity generation
    public String generateEntity(BusinessLogicRequest request) {
        return processTemplate("entity.ftl", createDataModel(request));
    }

    // CRUD Service generation
    public String generateCrudService(BusinessLogicRequest request) {
        return processTemplate("crud-service.ftl", createDataModel(request));
    }

    // Repository generation
    public String generateCrudRepository(BusinessLogicRequest request) {
        return processTemplate("crud-repository.ftl", createDataModel(request));
    }

    // DTO generation methods
    public String generateCreateDto(BusinessLogicRequest request) {
        Map<String, Object> model = createDataModel(request);
        model.put("dtoType", "Create");
        return processTemplate("dto.ftl", model);
    }

    public String generateUpdateDto(BusinessLogicRequest request) {
        Map<String, Object> model = createDataModel(request);
        model.put("dtoType", "Update");
        return processTemplate("dto.ftl", model);
    }

    public String generateResponseDto(BusinessLogicRequest request) {
        Map<String, Object> model = createDataModel(request);
        model.put("dtoType", "Response");
        return processTemplate("dto.ftl", model);
    }

    // Mapper generation
    public String generateMapper(BusinessLogicRequest request) {
        return processTemplate("mapper.ftl", createDataModel(request));
    }

    // Search and filter generation
    public String generateSearchSpecification(BusinessLogicRequest request) {
        return processTemplate("search-specification.ftl", createDataModel(request));
    }

    public String generateSearchService(BusinessLogicRequest request) {
        return processTemplate("search-service.ftl", createDataModel(request));
    }

    public String generateSearchCriteria(BusinessLogicRequest request) {
        return processTemplate("search-criteria.ftl", createDataModel(request));
    }

    public String generateSearchResult(BusinessLogicRequest request) {
        return processTemplate("search-result.ftl", createDataModel(request));
    }

    public String generatePaginationUtil(BusinessLogicRequest request) {
        return processTemplate("pagination-util.ftl", createDataModel(request));
    }

    // Batch processing generation
    public String generateBatchProcessor(BusinessLogicRequest request) {
        return processTemplate("batch-processor.ftl", createDataModel(request));
    }

    public String generateBatchConfig(BusinessLogicRequest request) {
        return processTemplate("batch-config.ftl", createDataModel(request));
    }

    public String generateJobListener(BusinessLogicRequest request) {
        return processTemplate("job-listener.ftl", createDataModel(request));
    }

    // Workflow generation
    public String generateWorkflowEngine(BusinessLogicRequest request) {
        return processTemplate("workflow-engine.ftl", createDataModel(request));
    }

    public String generateStateMachine(BusinessLogicRequest request) {
        return processTemplate("state-machine.ftl", createDataModel(request));
    }

    public String generateWorkflowSteps(BusinessLogicRequest request) {
        return processTemplate("workflow-steps.ftl", createDataModel(request));
    }

    // Notification generation
    public String generateNotificationService(BusinessLogicRequest request) {
        return processTemplate("notification-service.ftl", createDataModel(request));
    }

    public String generateEmailTemplate(BusinessLogicRequest request) {
        return processTemplate("email-template.ftl", createDataModel(request));
    }

    public String generateSmsTemplate(BusinessLogicRequest request) {
        return processTemplate("sms-template.ftl", createDataModel(request));
    }

    public String generateNotificationQueueHandler(BusinessLogicRequest request) {
        return processTemplate("notification-queue-handler.ftl", createDataModel(request));
    }

    // Integration generation
    public String generateRestClient(BusinessLogicRequest request) {
        return processTemplate("rest-client.ftl", createDataModel(request));
    }

    public String generateCircuitBreaker(BusinessLogicRequest request) {
        return processTemplate("circuit-breaker.ftl", createDataModel(request));
    }

    public String generateRetryConfig(BusinessLogicRequest request) {
        return processTemplate("retry-config.ftl", createDataModel(request));
    }

    // Report generation
    public String generateReportService(BusinessLogicRequest request) {
        return processTemplate("report-service.ftl", createDataModel(request));
    }

    public String generateReportBuilder(BusinessLogicRequest request) {
        return processTemplate("report-builder.ftl", createDataModel(request));
    }

    public String generatePdfExporter(BusinessLogicRequest request) {
        return processTemplate("pdf-exporter.ftl", createDataModel(request));
    }

    public String generateExcelExporter(BusinessLogicRequest request) {
        return processTemplate("excel-exporter.ftl", createDataModel(request));
    }

    // File processing generation
    public String generateFileHandler(BusinessLogicRequest request) {
        return processTemplate("file-handler.ftl", createDataModel(request));
    }

    public String generateFileValidator(BusinessLogicRequest request) {
        return processTemplate("file-validator.ftl", createDataModel(request));
    }

    public String generateStorageService(BusinessLogicRequest request) {
        return processTemplate("storage-service.ftl", createDataModel(request));
    }

    // Async generation
    public String generateAsyncService(BusinessLogicRequest request) {
        return processTemplate("async-service.ftl", createDataModel(request));
    }

    public String generateAsyncConfig(BusinessLogicRequest request) {
        return processTemplate("async-config.ftl", createDataModel(request));
    }

    public String generateCompletionHandler(BusinessLogicRequest request) {
        return processTemplate("completion-handler.ftl", createDataModel(request));
    }

    // Event-driven generation
    public String generateEventPublisher(BusinessLogicRequest request) {
        return processTemplate("event-publisher.ftl", createDataModel(request));
    }

    public String generateEventListener(BusinessLogicRequest request) {
        return processTemplate("event-listener.ftl", createDataModel(request));
    }

    public String generateEventStore(BusinessLogicRequest request) {
        return processTemplate("event-store.ftl", createDataModel(request));
    }

    // Documentation and test generation
    public String generateDocumentation(BusinessLogicRequest request) {
        return processTemplate("documentation.ftl", createDataModel(request));
    }

    public String generateUnitTest(BusinessLogicRequest request) {
        return processTemplate("unit-test.ftl", createDataModel(request));
    }

    public String generateIntegrationTest(BusinessLogicRequest request) {
        return processTemplate("integration-test.ftl", createDataModel(request));
    }

    // Helper methods
    private Map<String, Object> createDataModel(BusinessLogicRequest request) {
        Map<String, Object> model = new HashMap<>();
        model.put("packageName", request.getPackageName());
        model.put("entityName", request.getEntityName());
        model.put("fields", request.getFields());
        model.put("businessRules", request.getBusinessRules());
        model.put("validations", request.getValidations());
        return model;
    }

    private String processTemplate(String templateName, Map<String, Object> dataModel) {
        try {
            Template template = freemarkerConfig.getTemplate(templateName);
            StringWriter writer = new StringWriter();
            template.process(dataModel, writer);
            return writer.toString();
        } catch (Exception e) {
            log.error("Error processing template: " + templateName, e);
            throw new RuntimeException("Failed to process template: " + templateName, e);
        }
    }
}
EOF

# Create DTOs for Business Logic
cat > src/main/java/com/gigapress/backend/dto/BusinessLogicRequest.java << 'EOF'
package com.gigapress.backend.dto;

import com.gigapress.backend.model.BusinessLogicPattern;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class BusinessLogicRequest {
    private String entityName;
    private String packageName;
    private BusinessLogicPattern.PatternType patternType;
    private List<FieldDefinition> fields;
    private List<BusinessRule> businessRules;
    private List<ValidationRule> validations;
    private Map<String, Object> additionalConfig;
    
    @Data
    public static class FieldDefinition {
        private String name;
        private String type;
        private boolean required;
        private boolean unique;
        private String defaultValue;
        private List<String> constraints;
    }
    
    @Data
    public static class BusinessRule {
        private String name;
        private String description;
        private String condition;
        private String action;
        private int priority;
    }
    
    @Data
    public static class ValidationRule {
        private String fieldName;
        private String validationType;
        private String errorMessage;
        private Map<String, Object> parameters;
    }
}
EOF

cat > src/main/java/com/gigapress/backend/dto/GeneratedBusinessLogic.java << 'EOF'
package com.gigapress.backend.dto;

import com.gigapress.backend.model.BusinessLogicPattern;
import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class GeneratedBusinessLogic {
    private BusinessLogicPattern.PatternType patternType;
    private Map<String, String> generatedCode;
    private String documentation;
    private Map<String, String> tests;
    private Map<String, String> configurations;
    private ExecutionPlan executionPlan;
    
    @Data
    @Builder
    public static class ExecutionPlan {
        private String description;
        private int estimatedComplexity;
        private Map<String, String> dependencies;
        private Map<String, String> deploymentInstructions;
    }
}
EOF

# Create Validation Service
cat > src/main/java/com/gigapress/backend/service/ValidationService.java << 'EOF'
package com.gigapress.backend.service;

import com.gigapress.backend.dto.ApiSpecification;
import com.gigapress.backend.dto.BusinessLogicRequest;
import com.gigapress.backend.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ValidationService {

    public void validateApiSpecification(ApiSpecification spec) {
        List<String> errors = new ArrayList<>();
        
        if (spec.getApiName() == null || spec.getApiName().isEmpty()) {
            errors.add("API name is required");
        }
        
        if (spec.getEntityName() == null || spec.getEntityName().isEmpty()) {
            errors.add("Entity name is required");
        }
        
        if (spec.getPackageName() == null || spec.getPackageName().isEmpty()) {
            errors.add("Package name is required");
        }
        
        if (!errors.isEmpty()) {
            throw new ValidationException("API specification validation failed", errors);
        }
    }

    public void validateBusinessLogicRequest(BusinessLogicRequest request) {
        List<String> errors = new ArrayList<>();
        
        if (request.getEntityName() == null || request.getEntityName().isEmpty()) {
            errors.add("Entity name is required");
        }
        
        if (request.getPatternType() == null) {
            errors.add("Pattern type is required");
        }
        
        if (request.getFields() == null || request.getFields().isEmpty()) {
            errors.add("At least one field is required");
        }
        
        // Validate fields
        if (request.getFields() != null) {
            for (int i = 0; i < request.getFields().size(); i++) {
                BusinessLogicRequest.FieldDefinition field = request.getFields().get(i);
                if (field.getName() == null || field.getName().isEmpty()) {
                    errors.add("Field name is required for field at index " + i);
                }
                if (field.getType() == null || field.getType().isEmpty()) {
                    errors.add("Field type is required for field " + field.getName());
                }
            }
        }
        
        if (!errors.isEmpty()) {
            throw new ValidationException("Business logic request validation failed", errors);
        }
    }
}
EOF

# Create Exception classes
cat > src/main/java/com/gigapress/backend/exception/ValidationException.java << 'EOF'
package com.gigapress.backend.exception;

import lombok.Getter;
import java.util.List;

@Getter
public class ValidationException extends RuntimeException {
    private final List<String> errors;

    public ValidationException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }
}
EOF

# Create Global Exception Handler
cat > src/main/java/com/gigapress/backend/exception/GlobalExceptionHandler.java << 'EOF'
package com.gigapress.backend.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(ValidationException e) {
        log.error("Validation error: {}", e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Validation failed");
        response.put("message", e.getMessage());
        response.put("errors", e.getErrors());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        log.error("Unexpected error", e);
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Internal server error");
        response.put("message", "An unexpected error occurred");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
EOF

# Create Enhanced Controller
cat > src/main/java/com/gigapress/backend/controller/BusinessLogicController.java << 'EOF'
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
EOF

# Create business logic template directory
mkdir -p src/main/resources/templates/business-logic

# Create CRUD Service Template
cat > src/main/resources/templates/business-logic/crud-service.ftl << 'EOF'
package ${packageName}.service;

import ${packageName}.entity.${entityName};
import ${packageName}.dto.${entityName}CreateDto;
import ${packageName}.dto.${entityName}UpdateDto;
import ${packageName}.dto.${entityName}ResponseDto;
import ${packageName}.repository.${entityName}Repository;
import ${packageName}.mapper.${entityName}Mapper;
import ${packageName}.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ${entityName}Service {

    private final ${entityName}Repository repository;
    private final ${entityName}Mapper mapper;
    
    private static final String CACHE_NAME = "${entityName?lower_case}_cache";

    @Transactional
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public ${entityName}ResponseDto create(@Valid ${entityName}CreateDto createDto) {
        log.info("Creating new ${entityName}");
        
        ${entityName} entity = mapper.toEntity(createDto);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        
        <#list businessRules as rule>
        // Business Rule: ${rule.name}
        // ${rule.description}
        if (${rule.condition}) {
            ${rule.action}
        }
        </#list>
        
        ${entityName} saved = repository.save(entity);
        log.info("Created ${entityName} with id: {}", saved.getId());
        
        return mapper.toResponseDto(saved);
    }

    @Cacheable(value = CACHE_NAME, key = "#id")
    public ${entityName}ResponseDto findById(Long id) {
        log.info("Finding ${entityName} by id: {}", id);
        
        ${entityName} entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("${entityName} not found with id: " + id));
                
        return mapper.toResponseDto(entity);
    }

    public Page<${entityName}ResponseDto> findAll(Pageable pageable) {
        log.info("Finding all ${entityName}s with pagination");
        
        return repository.findAll(pageable)
                .map(mapper::toResponseDto);
    }

    public List<${entityName}ResponseDto> findAll() {
        log.info("Finding all ${entityName}s");
        
        return repository.findAll().stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = CACHE_NAME, key = "#id")
    public ${entityName}ResponseDto update(Long id, @Valid ${entityName}UpdateDto updateDto) {
        log.info("Updating ${entityName} with id: {}", id);
        
        ${entityName} entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("${entityName} not found with id: " + id));
        
        mapper.updateEntityFromDto(updateDto, entity);
        entity.setUpdatedAt(LocalDateTime.now());
        
        <#list businessRules as rule>
        // Business Rule: ${rule.name}
        // ${rule.description}
        if (${rule.condition}) {
            ${rule.action}
        }
        </#list>
        
        ${entityName} updated = repository.save(entity);
        log.info("Updated ${entityName} with id: {}", updated.getId());
        
        return mapper.toResponseDto(updated);
    }

    @Transactional
    @CacheEvict(value = CACHE_NAME, key = "#id")
    public void delete(Long id) {
        log.info("Deleting ${entityName} with id: {}", id);
        
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("${entityName} not found with id: " + id);
        }
        
        repository.deleteById(id);
        log.info("Deleted ${entityName} with id: {}", id);
    }

    @Transactional
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void deleteAll() {
        log.warn("Deleting all ${entityName}s");
        repository.deleteAll();
    }

    public long count() {
        return repository.count();
    }

    public boolean existsById(Long id) {
        return repository.existsById(id);
    }
}
EOF

# Create Entity Template
cat > src/main/resources/templates/business-logic/entity.ftl << 'EOF'
package ${packageName}.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
<#list fields as field>
<#if field.type == "LocalDate" || field.type == "LocalDateTime">
import java.time.${field.type};
</#if>
</#list>

@Entity
@Table(name = "${entityName?lower_case}s")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"createdAt", "updatedAt"})
@EqualsAndHashCode(of = "id")
public class ${entityName} {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

<#list fields as field>
    <#if field.unique>
    @Column(unique = true<#if field.required>, nullable = false</#if>)
    <#elseif field.required>
    @Column(nullable = false)
    <#else>
    @Column
    </#if>
    private ${field.type} ${field.name};

</#list>
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}
EOF

# Create Mapper Template
cat > src/main/resources/templates/business-logic/mapper.ftl << 'EOF'
package ${packageName}.mapper;

import ${packageName}.entity.${entityName};
import ${packageName}.dto.${entityName}CreateDto;
import ${packageName}.dto.${entityName}UpdateDto;
import ${packageName}.dto.${entityName}ResponseDto;
import org.springframework.stereotype.Component;

@Component
public class ${entityName}Mapper {

    public ${entityName} toEntity(${entityName}CreateDto dto) {
        if (dto == null) {
            return null;
        }

        return ${entityName}.builder()
<#list fields as field>
                .${field.name}(dto.get${field.name?cap_first}())
</#list>
                .build();
    }

    public ${entityName}ResponseDto toResponseDto(${entityName} entity) {
        if (entity == null) {
            return null;
        }

        ${entityName}ResponseDto dto = new ${entityName}ResponseDto();
        dto.setId(entity.getId());
<#list fields as field>
        dto.set${field.name?cap_first}(entity.get${field.name?cap_first}());
</#list>
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        
        return dto;
    }

    public void updateEntityFromDto(${entityName}UpdateDto dto, ${entityName} entity) {
        if (dto == null || entity == null) {
            return;
        }

<#list fields as field>
        if (dto.get${field.name?cap_first}() != null) {
            entity.set${field.name?cap_first}(dto.get${field.name?cap_first}());
        }
</#list>
    }
}
EOF

# Create Search Service Template
cat > src/main/resources/templates/business-logic/search-service.ftl << 'EOF'
package ${packageName}.service;

import ${packageName}.dto.${entityName}SearchCriteria;
import ${packageName}.dto.${entityName}SearchResult;
import ${packageName}.entity.${entityName};
import ${packageName}.repository.${entityName}Repository;
import ${packageName}.specification.${entityName}Specification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ${entityName}SearchService {

    private final ${entityName}Repository repository;
    private final ${entityName}Specification specificationBuilder;

    public ${entityName}SearchResult search(${entityName}SearchCriteria criteria) {
        log.info("Searching ${entityName}s with criteria: {}", criteria);
        
        // Build specification
        Specification<${entityName}> spec = specificationBuilder.build(criteria);
        
        // Build pageable
        Pageable pageable = buildPageable(criteria);
        
        // Execute search
        Page<${entityName}> page = repository.findAll(spec, pageable);
        
        // Build result
        return ${entityName}SearchResult.builder()
                .content(page.getContent())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
    
    private Pageable buildPageable(${entityName}SearchCriteria criteria) {
        List<Sort.Order> orders = new ArrayList<>();
        
        if (criteria.getSortBy() != null && !criteria.getSortBy().isEmpty()) {
            for (String sortField : criteria.getSortBy()) {
                Sort.Direction direction = criteria.isDescending() ? 
                    Sort.Direction.DESC : Sort.Direction.ASC;
                orders.add(new Sort.Order(direction, sortField));
            }
        } else {
            orders.add(Sort.Order.desc("createdAt"));
        }
        
        return PageRequest.of(
            criteria.getPage(), 
            criteria.getSize(), 
            Sort.by(orders)
        );
    }
}
EOF

# Update the KafkaProducerService with new method
cat >> src/main/java/com/gigapress/backend/service/KafkaProducerService.java << 'EOF'

    public void sendBusinessLogicGeneratedEvent(GeneratedBusinessLogic businessLogic) {
        Map<String, Object> event = new HashMap<>();
        event.put("patternType", businessLogic.getPatternType());
        event.put("timestamp", System.currentTimeMillis());
        event.put("status", "COMPLETED");
        
        log.info("Sending business logic generated event: {}", event);
        kafkaTemplate.send("business-logic-events", event);
    }
EOF

# Create Integration Test Base
cat > src/test/java/com/gigapress/backend/BackendServiceIntegrationTest.java << 'EOF'
package com.gigapress.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class BackendServiceIntegrationTest {

    @Test
    void contextLoads() {
        // Test that the application context loads successfully
    }
}
EOF

# Create test properties
cat > src/main/resources/application-test.properties << 'EOF'
# Test Configuration
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
spring.kafka.bootstrap-servers=localhost:9092
spring.data.redis.host=localhost
spring.data.redis.port=6379
logging.level.com.gigapress=DEBUG
EOF

echo "✅ Backend Service Step 2 Enhancement completed!"
echo ""
echo "📋 Added components:"
echo "  - Business Logic Generation Service with 10 pattern types"
echo "  - Business Logic Template Engine"
echo "  - Enhanced DTOs and Models"
echo "  - Validation Service"
echo "  - Global Exception Handler"
echo "  - Business Logic Controller"
echo "  - Multiple template files for different patterns"
echo ""
echo "🎯 Supported Business Logic Patterns:"
echo "  1. CRUD Operations"
echo "  2. Search and Filter"
echo "  3. Batch Processing"
echo "  4. Workflow Management"
echo "  5. Notification System"
echo "  6. External Integration"
echo "  7. Report Generation"
echo "  8. File Processing"
echo "  9. Async Operations"
echo "  10. Event-Driven Logic"
echo ""
echo "Next: Run './gradlew bootRun' to start the enhanced service"