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
