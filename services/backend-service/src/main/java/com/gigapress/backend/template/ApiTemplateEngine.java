package com.gigapress.backend.template;

import com.gigapress.backend.dto.ApiSpecification;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ApiTemplateEngine {

    private final Configuration freemarkerConfig;

    public ApiTemplateEngine() {
        this.freemarkerConfig = new Configuration(Configuration.VERSION_2_3_32);
        this.freemarkerConfig.setClassForTemplateLoading(this.getClass(), "/templates");
        this.freemarkerConfig.setDefaultEncoding("UTF-8");
    }

    public String generateController(ApiSpecification spec) {
        try {
            Template template = freemarkerConfig.getTemplate("controller.ftl");
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("packageName", spec.getPackageName());
            dataModel.put("entityName", spec.getEntityName());
            dataModel.put("apiPath", spec.getApiPath());
            
            StringWriter writer = new StringWriter();
            template.process(dataModel, writer);
            return writer.toString();
        } catch (Exception e) {
            log.error("Error generating controller", e);
            throw new RuntimeException("Failed to generate controller", e);
        }
    }

    public String generateService(ApiSpecification spec) {
        try {
            Template template = freemarkerConfig.getTemplate("service.ftl");
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("packageName", spec.getPackageName());
            dataModel.put("entityName", spec.getEntityName());
            
            StringWriter writer = new StringWriter();
            template.process(dataModel, writer);
            return writer.toString();
        } catch (Exception e) {
            log.error("Error generating service", e);
            throw new RuntimeException("Failed to generate service", e);
        }
    }

    public String generateRepository(ApiSpecification spec) {
        try {
            Template template = freemarkerConfig.getTemplate("repository.ftl");
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("packageName", spec.getPackageName());
            dataModel.put("entityName", spec.getEntityName());
            
            StringWriter writer = new StringWriter();
            template.process(dataModel, writer);
            return writer.toString();
        } catch (Exception e) {
            log.error("Error generating repository", e);
            throw new RuntimeException("Failed to generate repository", e);
        }
    }

    public Map<String, String> generateDtos(ApiSpecification spec) {
        Map<String, String> dtos = new HashMap<>();
        
        // Generate request DTO
        String requestDto = generateDto(spec, "request");
        dtos.put(spec.getEntityName() + "Request", requestDto);
        
        // Generate response DTO
        String responseDto = generateDto(spec, "response");
        dtos.put(spec.getEntityName() + "Response", responseDto);
        
        return dtos;
    }

    private String generateDto(ApiSpecification spec, String type) {
        try {
            Template template = freemarkerConfig.getTemplate("dto.ftl");
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("packageName", spec.getPackageName());
            dataModel.put("entityName", spec.getEntityName());
            dataModel.put("dtoType", type);
            dataModel.put("fields", spec.getFields());
            
            StringWriter writer = new StringWriter();
            template.process(dataModel, writer);
            return writer.toString();
        } catch (Exception e) {
            log.error("Error generating DTO", e);
            throw new RuntimeException("Failed to generate DTO", e);
        }
    }
}
