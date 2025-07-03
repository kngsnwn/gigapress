package com.gigapress.dynamicupdate.dto;

import com.gigapress.dynamicupdate.domain.ComponentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComponentRequest {
    @NotBlank
    private String componentId;
    
    @NotBlank
    private String name;
    
    @NotNull
    private ComponentType type;
    
    @NotBlank
    private String version;
    
    @NotBlank
    private String projectId;
    
    private String metadata;
}
