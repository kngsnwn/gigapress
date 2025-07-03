package com.gigapress.backend.event;

import lombok.Data;

@Data
public class ApiGeneratedEvent {
    private String apiName;
    private Long timestamp;
    private String status;
}
