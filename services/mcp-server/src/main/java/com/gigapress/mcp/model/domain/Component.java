package com.gigapress.mcp.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Component {
    
    private String componentId;
    private String componentName;
    private ComponentType type;
    private String version;
    private String location;
    private List<String> dependencies;
    private List<String> dependents;
    private Map<String, Object> configuration;
    private ComponentStatus status;
    private LocalDateTime lastModified;
    private Map<String, Object> metadata;
    
    public enum ComponentType {
        FRONTEND,
        BACKEND,
        DATABASE,
        API,
        SERVICE,
        LIBRARY,
        CONFIGURATION,
        INFRASTRUCTURE
    }
    
    public enum ComponentStatus {
        ACTIVE,
        DEPRECATED,
        MAINTENANCE,
        ARCHIVED
    }
}
