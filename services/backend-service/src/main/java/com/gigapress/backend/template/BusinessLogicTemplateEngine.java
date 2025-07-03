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
