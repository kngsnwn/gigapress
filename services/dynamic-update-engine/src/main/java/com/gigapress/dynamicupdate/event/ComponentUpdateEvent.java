package com.gigapress.dynamicupdate.event;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class ComponentUpdateEvent {
    private String eventId;
    private String componentId;
    private String projectId;
    private UpdateType updateType;
    private String previousVersion;
    private String newVersion;
    private Map<String, Object> changes;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private String userId;
    private String reason;
    
    public enum UpdateType {
        CREATE,
        UPDATE,
        DELETE,
        VERSION_CHANGE,
        DEPENDENCY_CHANGE,
        CONFIGURATION_CHANGE
    }
}
