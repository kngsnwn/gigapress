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
