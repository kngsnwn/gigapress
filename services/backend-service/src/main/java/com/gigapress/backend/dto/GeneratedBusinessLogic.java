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
