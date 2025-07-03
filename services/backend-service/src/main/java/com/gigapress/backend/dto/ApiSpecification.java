package com.gigapress.backend.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ApiSpecification {
    private String apiName;
    private String entityName;
    private String packageName;
    private String apiPath;
    private List<FieldSpecification> fields;
    private Map<String, String> operations;
    private AuthenticationRequirement authentication;
    
    @Data
    public static class FieldSpecification {
        private String name;
        private String type;
        private boolean required;
        private String validation;
    }
    
    @Data
    public static class AuthenticationRequirement {
        private boolean required;
        private String type;
        private List<String> roles;
    }
}
