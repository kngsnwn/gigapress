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
