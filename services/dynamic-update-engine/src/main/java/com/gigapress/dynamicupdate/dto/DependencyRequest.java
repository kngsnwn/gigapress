package com.gigapress.dynamicupdate.dto;

import com.gigapress.dynamicupdate.domain.DependencyType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DependencyRequest {
    @NotBlank
    private String targetComponentId;
    
    @NotNull
    private DependencyType type;
    
    private String metadata;
}
