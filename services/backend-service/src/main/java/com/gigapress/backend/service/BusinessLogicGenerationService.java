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

        GeneratedBusinessLogic result;  // 기본 생성자 호출 제거하고 선언만

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
