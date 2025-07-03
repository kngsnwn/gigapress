package com.gigapress.mcp.controller;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HealthStatus {
    private String status;
    private String service;
    private String version;
}