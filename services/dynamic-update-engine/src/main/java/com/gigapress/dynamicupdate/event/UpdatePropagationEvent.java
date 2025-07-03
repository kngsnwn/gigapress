package com.gigapress.dynamicupdate.event;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class UpdatePropagationEvent {
    private String eventId;
    private String triggerComponentId;
    private String projectId;
    private List<String> affectedComponentIds;
    private PropagationType propagationType;
    private Map<String, Object> updateDetails;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private int propagationDepth;
    private String initiatedBy;
    
    public enum PropagationType {
        CASCADE,
        SELECTIVE,
        FORCED,
        ROLLBACK
    }
}
