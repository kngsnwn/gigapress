package com.gigapress.domainschema.schema.mapping.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Generated file information")
public class GeneratedFileResponse {
    
    @Schema(description = "File name", example = "User.java")
    private String fileName;
    
    @Schema(description = "File path", example = "com/example/entities/User.java")
    private String filePath;
    
    @Schema(description = "File type", example = "ENTITY")
    private String fileType;
    
    @Schema(description = "File content preview")
    private String contentPreview;
    
    @Schema(description = "Full content available")
    private boolean fullContentAvailable;
    
    @Schema(description = "File size in bytes")
    private long fileSize;
}
