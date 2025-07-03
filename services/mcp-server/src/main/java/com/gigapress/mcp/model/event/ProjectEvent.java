package com.gigapress.mcp.model.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEvent {
    
    @JsonProperty("event_id")
    private String eventId;
    
    @JsonProperty("event_type")
    private EventType eventType;
    
    @JsonProperty("project_id")
    private String projectId;
    
    @JsonProperty("component_id")
    private String componentId;
    
    @JsonProperty("payload")
    private Map<String, Object> payload;
    
    @JsonProperty("source_service")
    private String sourceService;
    
    @JsonProperty("timestamp")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    @JsonProperty("correlation_id")
    private String correlationId;
    
    @JsonProperty("metadata")
    private Map<String, String> metadata;
    
    public enum EventType {
        PROJECT_CREATED,
        PROJECT_UPDATED,
        PROJECT_DELETED,
        COMPONENT_ADDED,
        COMPONENT_UPDATED,
        COMPONENT_REMOVED,
        DEPENDENCY_CHANGED,
        VALIDATION_COMPLETED,
        ANALYSIS_COMPLETED,
        ERROR_OCCURRED
    }
}
