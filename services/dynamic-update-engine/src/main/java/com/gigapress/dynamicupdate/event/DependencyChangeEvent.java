package com.gigapress.dynamicupdate.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gigapress.dynamicupdate.domain.DependencyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DependencyChangeEvent {
    private String eventId;
    private String sourceComponentId;
    private String targetComponentId;
    private String projectId;
    private ChangeType changeType;
    private DependencyType dependencyType;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private String metadata;
    
    public enum ChangeType {
        ADDED,
        REMOVED,
        UPDATED
    }
}
