package com.gigapress.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class GeneratedApi {
    private String apiName;
    private String controllerCode;
    private String serviceCode;
    private String repositoryCode;
    private Map<String, String> dtoClasses;
    private String openApiSpec;
}
