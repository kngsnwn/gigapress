package com.gigapress.dynamicupdate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRequest {
    @NotNull
    private Map<String, Object> updates;
}
