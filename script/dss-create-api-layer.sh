#!/bin/bash

# API Layer Creation Script for Domain/Schema Service
set -e

echo "🎯 Creating API Layer for Domain/Schema Service..."

# Navigate to the project directory
cd services/domain-schema-service/src/main/java/com/gigapress/domainschema

# Create DTOs directory structure
echo "📦 Creating DTO structure..."
mkdir -p domain/analysis/{dto/{request,response},mapper}
mkdir -p schema/design/{dto/{request,response},mapper}
mkdir -p schema/mapping/{dto/{request,response},mapper}

# Create Common Response DTOs
echo "📦 Creating Common Response DTOs..."
mkdir -p domain/common/dto

# ApiResponse DTO
cat > domain/common/dto/ApiResponse.java << 'EOF'
package com.gigapress.domainschema.domain.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    @Builder.Default
    private boolean success = true;
    
    private T data;
    
    private String message;
    
    private String error;
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    private String path;
    
    // Static factory methods
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }
    
    public static <T> ApiResponse<T> error(String error) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(error)
                .build();
    }
    
    public static <T> ApiResponse<T> error(String error, String path) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(error)
                .path(path)
                .build();
    }
}
EOF

# PageResponse DTO
cat > domain/common/dto/PageResponse.java << 'EOF'
package com.gigapress.domainschema.domain.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean empty;
}
EOF

# Create Analysis Request DTOs
echo "📦 Creating Analysis Request DTOs..."

# CreateProjectRequest
cat > domain/analysis/dto/request/CreateProjectRequest.java << 'EOF'
package com.gigapress.domainschema.domain.analysis.dto.request;

import com.gigapress.domainschema.domain.common.entity.ProjectType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new project")
public class CreateProjectRequest {
    
    @NotBlank(message = "Project name is required")
    @Size(min = 3, max = 100, message = "Project name must be between 3 and 100 characters")
    @Schema(description = "Name of the project", example = "E-Commerce Platform")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Schema(description = "Detailed description of the project", example = "A modern e-commerce platform with user authentication, product catalog, and payment processing")
    private String description;
    
    @NotNull(message = "Project type is required")
    @Schema(description = "Type of the project", example = "WEB_APPLICATION")
    private ProjectType projectType;
}
EOF

# AnalyzeRequirementsRequest
cat > domain/analysis/dto/request/AnalyzeRequirementsRequest.java << 'EOF'
package com.gigapress.domainschema.domain.analysis.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to analyze project requirements")
public class AnalyzeRequirementsRequest {
    
    @NotBlank(message = "Project ID is required")
    @Schema(description = "ID of the project", example = "proj_123456")
    private String projectId;
    
    @NotBlank(message = "Natural language requirements are required")
    @Size(min = 10, max = 10000, message = "Requirements must be between 10 and 10000 characters")
    @Schema(description = "Natural language description of requirements", 
            example = "Users should be able to register and login. Products should have categories and reviews.")
    private String naturalLanguageRequirements;
    
    @Schema(description = "Additional context or constraints", example = ["Must support mobile devices", "Need to handle 1000 concurrent users"])
    private List<String> constraints;
    
    @Schema(description = "Technology preferences", example = {"frontend": "React", "backend": "Spring Boot", "database": "PostgreSQL"})
    private java.util.Map<String, String> technologyPreferences;
}
EOF

# AddRequirementRequest
cat > domain/analysis/dto/request/AddRequirementRequest.java << 'EOF'
package com.gigapress.domainschema.domain.analysis.dto.request;

import com.gigapress.domainschema.domain.common.entity.RequirementPriority;
import com.gigapress.domainschema.domain.common.entity.RequirementType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to add a new requirement")
public class AddRequirementRequest {
    
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    @Schema(description = "Requirement title", example = "User Authentication")
    private String title;
    
    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    @Schema(description = "Detailed requirement description", example = "Users must be able to register with email and password")
    private String description;
    
    @NotNull(message = "Requirement type is required")
    @Schema(description = "Type of requirement", example = "FUNCTIONAL")
    private RequirementType type;
    
    @NotNull(message = "Priority is required")
    @Schema(description = "Requirement priority", example = "HIGH")
    private RequirementPriority priority;
    
    @Schema(description = "Additional metadata")
    private Map<String, String> metadata;
}
EOF

# Create Analysis Response DTOs
echo "📦 Creating Analysis Response DTOs..."

# ProjectResponse
cat > domain/analysis/dto/response/ProjectResponse.java << 'EOF'
package com.gigapress.domainschema.domain.analysis.dto.response;

import com.gigapress.domainschema.domain.common.entity.ProjectStatus;
import com.gigapress.domainschema.domain.common.entity.ProjectType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Project information response")
public class ProjectResponse {
    
    @Schema(description = "Project ID", example = "proj_123456")
    private String projectId;
    
    @Schema(description = "Project name", example = "E-Commerce Platform")
    private String name;
    
    @Schema(description = "Project description")
    private String description;
    
    @Schema(description = "Project type", example = "WEB_APPLICATION")
    private ProjectType projectType;
    
    @Schema(description = "Current project status", example = "ANALYZING")
    private ProjectStatus status;
    
    @Schema(description = "Number of requirements")
    private int requirementCount;
    
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
    
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
EOF

# RequirementResponse
cat > domain/analysis/dto/response/RequirementResponse.java << 'EOF'
package com.gigapress.domainschema.domain.analysis.dto.response;

import com.gigapress.domainschema.domain.common.entity.RequirementPriority;
import com.gigapress.domainschema.domain.common.entity.RequirementStatus;
import com.gigapress.domainschema.domain.common.entity.RequirementType;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Requirement information response")
public class RequirementResponse {
    
    @Schema(description = "Requirement ID")
    private Long id;
    
    @Schema(description = "Requirement title", example = "User Authentication")
    private String title;
    
    @Schema(description = "Detailed description")
    private String description;
    
    @Schema(description = "Requirement type", example = "FUNCTIONAL")
    private RequirementType type;
    
    @Schema(description = "Priority level", example = "HIGH")
    private RequirementPriority priority;
    
    @Schema(description = "Current status", example = "ANALYZED")
    private RequirementStatus status;
    
    @Schema(description = "Additional metadata")
    private Map<String, String> metadata;
    
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
}
EOF

# AnalysisResultResponse
cat > domain/analysis/dto/response/AnalysisResultResponse.java << 'EOF'
package com.gigapress.domainschema.domain.analysis.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requirements analysis result")
public class AnalysisResultResponse {
    
    @Schema(description = "Project ID", example = "proj_123456")
    private String projectId;
    
    @Schema(description = "Analysis summary")
    private String summary;
    
    @Schema(description = "Extracted requirements")
    private List<RequirementResponse> requirements;
    
    @Schema(description = "Identified entities")
    private List<String> identifiedEntities;
    
    @Schema(description = "Suggested relationships")
    private List<String> suggestedRelationships;
    
    @Schema(description = "Technology recommendations")
    private Map<String, String> technologyRecommendations;
    
    @Schema(description = "Analysis confidence score", example = "0.95")
    private Double confidenceScore;
}
EOF

# Create Schema Design Request DTOs
echo "📦 Creating Schema Design Request DTOs..."

# GenerateSchemaRequest
cat > schema/design/dto/request/GenerateSchemaRequest.java << 'EOF'
package com.gigapress.domainschema.schema.design.dto.request;

import com.gigapress.domainschema.domain.common.entity.DatabaseType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to generate database schema")
public class GenerateSchemaRequest {
    
    @NotBlank(message = "Project ID is required")
    @Schema(description = "ID of the project", example = "proj_123456")
    private String projectId;
    
    @NotNull(message = "Database type is required")
    @Schema(description = "Target database type", example = "POSTGRESQL")
    private DatabaseType databaseType;
    
    @Schema(description = "Schema name", example = "ecommerce")
    private String schemaName;
    
    @Schema(description = "Database-specific options")
    private Map<String, Object> databaseOptions;
    
    @Schema(description = "Include audit columns", example = "true")
    @Builder.Default
    private boolean includeAuditColumns = true;
    
    @Schema(description = "Generate indexes", example = "true")
    @Builder.Default
    private boolean generateIndexes = true;
}
EOF

# Create Schema Design Response DTOs
echo "📦 Creating Schema Design Response DTOs..."

# SchemaDesignResponse
cat > schema/design/dto/response/SchemaDesignResponse.java << 'EOF'
package com.gigapress.domainschema.schema.design.dto.response;

import com.gigapress.domainschema.domain.common.entity.DatabaseType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Database schema design response")
public class SchemaDesignResponse {
    
    @Schema(description = "Schema design ID")
    private Long id;
    
    @Schema(description = "Project ID", example = "proj_123456")
    private String projectId;
    
    @Schema(description = "Schema name", example = "ecommerce")
    private String schemaName;
    
    @Schema(description = "Database type", example = "POSTGRESQL")
    private DatabaseType databaseType;
    
    @Schema(description = "Number of tables")
    private int tableCount;
    
    @Schema(description = "Table designs")
    private List<TableDesignResponse> tables;
    
    @Schema(description = "DDL script preview")
    private String ddlScriptPreview;
    
    @Schema(description = "Full DDL script available")
    private boolean fullDdlAvailable;
    
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
}
EOF

# TableDesignResponse
cat > schema/design/dto/response/TableDesignResponse.java << 'EOF'
package com.gigapress.domainschema.schema.design.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Table design information")
public class TableDesignResponse {
    
    @Schema(description = "Table name", example = "users")
    private String tableName;
    
    @Schema(description = "Table description")
    private String description;
    
    @Schema(description = "Column count")
    private int columnCount;
    
    @Schema(description = "Index count")
    private int indexCount;
    
    @Schema(description = "Column designs")
    private List<ColumnDesignResponse> columns;
    
    @Schema(description = "Index designs")
    private List<IndexDesignResponse> indexes;
}
EOF

# ColumnDesignResponse
cat > schema/design/dto/response/ColumnDesignResponse.java << 'EOF'
package com.gigapress.domainschema.schema.design.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Column design information")
public class ColumnDesignResponse {
    
    @Schema(description = "Column name", example = "user_id")
    private String columnName;
    
    @Schema(description = "Data type", example = "BIGINT")
    private String dataType;
    
    @Schema(description = "Column length")
    private Integer length;
    
    @Schema(description = "Is nullable", example = "false")
    private boolean nullable;
    
    @Schema(description = "Is primary key", example = "true")
    private boolean primaryKey;
    
    @Schema(description = "Is unique", example = "true")
    private boolean unique;
    
    @Schema(description = "Default value")
    private String defaultValue;
    
    @Schema(description = "Column comment")
    private String comment;
}
EOF

# IndexDesignResponse
cat > schema/design/dto/response/IndexDesignResponse.java << 'EOF'
package com.gigapress.domainschema.schema.design.dto.response;

import com.gigapress.domainschema.domain.common.entity.IndexType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Index design information")
public class IndexDesignResponse {
    
    @Schema(description = "Index name", example = "idx_users_email")
    private String indexName;
    
    @Schema(description = "Index type", example = "BTREE")
    private IndexType indexType;
    
    @Schema(description = "Is unique index", example = "true")
    private boolean unique;
    
    @Schema(description = "Indexed columns", example = ["email"])
    private List<String> columns;
    
    @Schema(description = "Where clause for partial index")
    private String whereClause;
}
EOF

# Create Entity Mapping Request DTOs
echo "📦 Creating Entity Mapping Request DTOs..."

# GenerateEntitiesRequest
cat > schema/mapping/dto/request/GenerateEntitiesRequest.java << 'EOF'
package com.gigapress.domainschema.schema.mapping.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to generate JPA entities")
public class GenerateEntitiesRequest {
    
    @NotBlank(message = "Project ID is required")
    @Schema(description = "ID of the project", example = "proj_123456")
    private String projectId;
    
    @Schema(description = "Package name for entities", example = "com.example.entities")
    @Builder.Default
    private String packageName = "com.generated.entities";
    
    @Schema(description = "Use Lombok annotations", example = "true")
    @Builder.Default
    private boolean useLombok = true;
    
    @Schema(description = "Generate repository interfaces", example = "true")
    @Builder.Default
    private boolean generateRepositories = true;
    
    @Schema(description = "Include validation annotations", example = "true")
    @Builder.Default
    private boolean includeValidation = true;
    
    @Schema(description = "Additional generation options")
    private Map<String, Object> generationOptions;
}
EOF

# Create Entity Mapping Response DTOs
echo "📦 Creating Entity Mapping Response DTOs..."

# EntityMappingResponse
cat > schema/mapping/dto/response/EntityMappingResponse.java << 'EOF'
package com.gigapress.domainschema.schema.mapping.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "JPA entity mapping response")
public class EntityMappingResponse {
    
    @Schema(description = "Project ID", example = "proj_123456")
    private String projectId;
    
    @Schema(description = "Number of entities generated")
    private int entityCount;
    
    @Schema(description = "Number of repositories generated")
    private int repositoryCount;
    
    @Schema(description = "Generated entity files")
    private List<GeneratedFileResponse> entityFiles;
    
    @Schema(description = "Generated repository files")
    private List<GeneratedFileResponse> repositoryFiles;
    
    @Schema(description = "Generation summary")
    private Map<String, Object> summary;
}
EOF

# GeneratedFileResponse
cat > schema/mapping/dto/response/GeneratedFileResponse.java << 'EOF'
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
EOF

# Create Domain Model Response DTOs
echo "📦 Creating Domain Model Response DTOs..."

# DomainModelResponse
cat > domain/analysis/dto/response/DomainModelResponse.java << 'EOF'
package com.gigapress.domainschema.domain.analysis.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Domain model response")
public class DomainModelResponse {
    
    @Schema(description = "Domain model ID")
    private Long id;
    
    @Schema(description = "Project ID", example = "proj_123456")
    private String projectId;
    
    @Schema(description = "Model name")
    private String name;
    
    @Schema(description = "Model description")
    private String description;
    
    @Schema(description = "Number of entities")
    private int entityCount;
    
    @Schema(description = "Number of relationships")
    private int relationshipCount;
    
    @Schema(description = "Domain entities")
    private List<DomainEntityResponse> entities;
    
    @Schema(description = "Domain relationships")
    private List<DomainRelationshipResponse> relationships;
    
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
}
EOF

# DomainEntityResponse
cat > domain/analysis/dto/response/DomainEntityResponse.java << 'EOF'
package com.gigapress.domainschema.domain.analysis.dto.response;

import com.gigapress.domainschema.domain.common.entity.EntityType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Domain entity information")
public class DomainEntityResponse {
    
    @Schema(description = "Entity ID")
    private Long id;
    
    @Schema(description = "Entity name", example = "User")
    private String name;
    
    @Schema(description = "Entity description")
    private String description;
    
    @Schema(description = "Database table name", example = "users")
    private String tableName;
    
    @Schema(description = "Entity type", example = "AGGREGATE_ROOT")
    private EntityType entityType;
    
    @Schema(description = "Entity attributes")
    private List<DomainAttributeResponse> attributes;
    
    @Schema(description = "Business rules")
    private String businessRules;
}
EOF

# DomainAttributeResponse
cat > domain/analysis/dto/response/DomainAttributeResponse.java << 'EOF'
package com.gigapress.domainschema.domain.analysis.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Domain attribute information")
public class DomainAttributeResponse {
    
    @Schema(description = "Attribute name", example = "email")
    private String name;
    
    @Schema(description = "Field name in code", example = "email")
    private String fieldName;
    
    @Schema(description = "Data type", example = "String")
    private String dataType;
    
    @Schema(description = "Is required", example = "true")
    private boolean required;
    
    @Schema(description = "Is unique", example = "true")
    private boolean unique;
    
    @Schema(description = "Field length")
    private Integer length;
    
    @Schema(description = "Default value")
    private String defaultValue;
    
    @Schema(description = "Validation rules")
    private String validationRules;
    
    @Schema(description = "Description")
    private String description;
}
EOF

# DomainRelationshipResponse
cat > domain/analysis/dto/response/DomainRelationshipResponse.java << 'EOF'
package com.gigapress.domainschema.domain.analysis.dto.response;

import com.gigapress.domainschema.domain.common.entity.RelationshipType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Domain relationship information")
public class DomainRelationshipResponse {
    
    @Schema(description = "Source entity name", example = "User")
    private String sourceEntity;
    
    @Schema(description = "Target entity name", example = "Order")
    private String targetEntity;
    
    @Schema(description = "Relationship type", example = "ONE_TO_MANY")
    private RelationshipType relationshipType;
    
    @Schema(description = "Source field name", example = "orders")
    private String sourceFieldName;
    
    @Schema(description = "Target field name", example = "user")
    private String targetFieldName;
    
    @Schema(description = "Is bidirectional", example = "true")
    private boolean bidirectional;
    
    @Schema(description = "Cascade type", example = "ALL")
    private String cascadeType;
    
    @Schema(description = "Fetch type", example = "LAZY")
    private String fetchType;
    
    @Schema(description = "Description")
    private String description;
}
EOF

# Create Controllers
echo "📦 Creating Controllers..."

# ProjectController
cat > domain/analysis/controller/ProjectController.java << 'EOF'
package com.gigapress.domainschema.domain.analysis.controller;

import com.gigapress.domainschema.domain.analysis.dto.request.CreateProjectRequest;
import com.gigapress.domainschema.domain.analysis.dto.response.ProjectResponse;
import com.gigapress.domainschema.domain.common.dto.ApiResponse;
import com.gigapress.domainschema.domain.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Project Management", description = "APIs for managing projects")
public class ProjectController {
    
    // Service injection will be added in Step 4
    
    @PostMapping
    @Operation(summary = "Create a new project", description = "Creates a new project for domain and schema analysis")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody CreateProjectRequest request) {
        log.info("Creating new project: {}", request.getName());
        
        // TODO: Implement service call
        ProjectResponse response = ProjectResponse.builder()
                .projectId("proj_" + System.currentTimeMillis())
                .name(request.getName())
                .description(request.getDescription())
                .projectType(request.getProjectType())
                .build();
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Project created successfully"));
    }
    
    @GetMapping("/{projectId}")
    @Operation(summary = "Get project by ID", description = "Retrieves project details by project ID")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProject(
            @Parameter(description = "Project ID", example = "proj_123456")
            @PathVariable String projectId) {
        log.info("Fetching project: {}", projectId);
        
        // TODO: Implement service call
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @GetMapping
    @Operation(summary = "List all projects", description = "Retrieves a paginated list of all projects")
    public ResponseEntity<ApiResponse<PageResponse<ProjectResponse>>> listProjects(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String status) {
        log.info("Listing projects with status: {}", status);
        
        // TODO: Implement service call
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @DeleteMapping("/{projectId}")
    @Operation(summary = "Delete project", description = "Deletes a project and all associated data")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @Parameter(description = "Project ID", example = "proj_123456")
            @PathVariable String projectId) {
        log.info("Deleting project: {}", projectId);
        
        // TODO: Implement service call
        return ResponseEntity.ok(ApiResponse.success(null, "Project deleted successfully"));
    }
}
EOF

# RequirementsController
cat > domain/analysis/controller/RequirementsController.java << 'EOF'
package com.gigapress.domainschema.domain.analysis.controller;

import com.gigapress.domainschema.domain.analysis.dto.request.AddRequirementRequest;
import com.gigapress.domainschema.domain.analysis.dto.request.AnalyzeRequirementsRequest;
import com.gigapress.domainschema.domain.analysis.dto.response.AnalysisResultResponse;
import com.gigapress.domainschema.domain.analysis.dto.response.RequirementResponse;
import com.gigapress.domainschema.domain.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/requirements")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Requirements Analysis", description = "APIs for analyzing and managing requirements")
public class RequirementsController {
    
    @PostMapping("/analyze")
    @Operation(summary = "Analyze natural language requirements", 
              description = "Analyzes natural language requirements and extracts structured requirements")
    public ResponseEntity<ApiResponse<AnalysisResultResponse>> analyzeRequirements(
            @Valid @RequestBody AnalyzeRequirementsRequest request) {
        log.info("Analyzing requirements for project: {}", request.getProjectId());
        
        // TODO: Implement service call
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, "Requirements analyzed successfully"));
    }
    
    @PostMapping("/{projectId}")
    @Operation(summary = "Add requirement to project", description = "Manually adds a requirement to an existing project")
    public ResponseEntity<ApiResponse<RequirementResponse>> addRequirement(
            @Parameter(description = "Project ID", example = "proj_123456")
            @PathVariable String projectId,
            @Valid @RequestBody AddRequirementRequest request) {
        log.info("Adding requirement to project: {}", projectId);
        
        // TODO: Implement service call
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, "Requirement added successfully"));
    }
    
    @GetMapping("/{projectId}")
    @Operation(summary = "List project requirements", description = "Retrieves all requirements for a project")
    public ResponseEntity<ApiResponse<List<RequirementResponse>>> getProjectRequirements(
            @Parameter(description = "Project ID", example = "proj_123456")
            @PathVariable String projectId) {
        log.info("Fetching requirements for project: {}", projectId);
        
        // TODO: Implement service call
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @PutMapping("/{requirementId}/status")
    @Operation(summary = "Update requirement status", description = "Updates the status of a specific requirement")
    public ResponseEntity<ApiResponse<RequirementResponse>> updateRequirementStatus(
            @Parameter(description = "Requirement ID")
            @PathVariable Long requirementId,
            @RequestParam String status) {
        log.info("Updating requirement {} status to: {}", requirementId, status);
        
        // TODO: Implement service call
        return ResponseEntity.ok(ApiResponse.success(null, "Status updated successfully"));
    }
}
EOF

# DomainModelController
cat > domain/analysis/controller/DomainModelController.java << 'EOF'
package com.gigapress.domainschema.domain.analysis.controller;

import com.gigapress.domainschema.domain.analysis.dto.response.DomainModelResponse;
import com.gigapress.domainschema.domain.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/domain-models")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Domain Model", description = "APIs for managing domain models")
public class DomainModelController {
    
    @PostMapping("/generate/{projectId}")
    @Operation(summary = "Generate domain model", 
              description = "Generates a domain model based on analyzed requirements")
    public ResponseEntity<ApiResponse<DomainModelResponse>> generateDomainModel(
            @Parameter(description = "Project ID", example = "proj_123456")
            @PathVariable String projectId) {
        log.info("Generating domain model for project: {}", projectId);
        
        // TODO: Implement service call
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, "Domain model generated successfully"));
    }
    
    @GetMapping("/{projectId}")
    @Operation(summary = "Get domain model", description = "Retrieves the domain model for a project")
    public ResponseEntity<ApiResponse<DomainModelResponse>> getDomainModel(
            @Parameter(description = "Project ID", example = "proj_123456")
            @PathVariable String projectId) {
        log.info("Fetching domain model for project: {}", projectId);
        
        // TODO: Implement service call
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @PutMapping("/{projectId}/regenerate")
    @Operation(summary = "Regenerate domain model", 
              description = "Regenerates the domain model with updated requirements")
    public ResponseEntity<ApiResponse<DomainModelResponse>> regenerateDomainModel(
            @Parameter(description = "Project ID", example = "proj_123456")
            @PathVariable String projectId) {
        log.info("Regenerating domain model for project: {}", projectId);
        
        // TODO: Implement service call
        return ResponseEntity.ok(ApiResponse.success(null, "Domain model regenerated successfully"));
    }
}
EOF

# SchemaDesignController
cat > schema/design/controller/SchemaDesignController.java << 'EOF'
package com.gigapress.domainschema.schema.design.controller;

import com.gigapress.domainschema.domain.common.dto.ApiResponse;
import com.gigapress.domainschema.schema.design.dto.request.GenerateSchemaRequest;
import com.gigapress.domainschema.schema.design.dto.response.SchemaDesignResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/schema-designs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Schema Design", description = "APIs for database schema design and generation")
public class SchemaDesignController {
    
    @PostMapping
    @Operation(summary = "Generate database schema", 
              description = "Generates database schema based on domain model")
    public ResponseEntity<ApiResponse<SchemaDesignResponse>> generateSchema(
            @Valid @RequestBody GenerateSchemaRequest request) {
        log.info("Generating schema for project: {}", request.getProjectId());
        
        // TODO: Implement service call
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, "Schema generated successfully"));
    }
    
    @GetMapping("/{projectId}")
    @Operation(summary = "Get schema design", description = "Retrieves the schema design for a project")
    public ResponseEntity<ApiResponse<SchemaDesignResponse>> getSchemaDesign(
            @Parameter(description = "Project ID", example = "proj_123456")
            @PathVariable String projectId) {
        log.info("Fetching schema design for project: {}", projectId);
        
        // TODO: Implement service call
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @GetMapping("/{projectId}/ddl")
    @Operation(summary = "Get DDL script", description = "Downloads the complete DDL script for the schema")
    public ResponseEntity<String> getDdlScript(
            @Parameter(description = "Project ID", example = "proj_123456")
            @PathVariable String projectId) {
        log.info("Fetching DDL script for project: {}", projectId);
        
        // TODO: Implement service call
        String ddlScript = "-- DDL Script placeholder";
        
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header("Content-Disposition", "attachment; filename=\"schema_" + projectId + ".sql\"")
                .body(ddlScript);
    }
    
    @GetMapping("/{projectId}/migration")
    @Operation(summary = "Get migration script", 
              description = "Downloads the database migration script")
    public ResponseEntity<String> getMigrationScript(
            @Parameter(description = "Project ID", example = "proj_123456")
            @PathVariable String projectId) {
        log.info("Fetching migration script for project: {}", projectId);
        
        // TODO: Implement service call
        String migrationScript = "-- Migration Script placeholder";
        
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header("Content-Disposition", "attachment; filename=\"migration_" + projectId + ".sql\"")
                .body(migrationScript);
    }
}
EOF

# EntityMappingController
cat > schema/mapping/controller/EntityMappingController.java << 'EOF'
package com.gigapress.domainschema.schema.mapping.controller;

import com.gigapress.domainschema.domain.common.dto.ApiResponse;
import com.gigapress.domainschema.schema.mapping.dto.request.GenerateEntitiesRequest;
import com.gigapress.domainschema.schema.mapping.dto.response.EntityMappingResponse;
import com.gigapress.domainschema.schema.mapping.dto.response.GeneratedFileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/entity-mappings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Entity Mapping", description = "APIs for JPA entity generation and mapping")
public class EntityMappingController {
    
    @PostMapping
    @Operation(summary = "Generate JPA entities", 
              description = "Generates JPA entities based on schema design")
    public ResponseEntity<ApiResponse<EntityMappingResponse>> generateEntities(
            @Valid @RequestBody GenerateEntitiesRequest request) {
        log.info("Generating entities for project: {}", request.getProjectId());
        
        // TODO: Implement service call
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, "Entities generated successfully"));
    }
    
    @GetMapping("/{projectId}")
    @Operation(summary = "Get entity mappings", 
              description = "Retrieves entity mapping information for a project")
    public ResponseEntity<ApiResponse<EntityMappingResponse>> getEntityMappings(
            @Parameter(description = "Project ID", example = "proj_123456")
            @PathVariable String projectId) {
        log.info("Fetching entity mappings for project: {}", projectId);
        
        // TODO: Implement service call
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @GetMapping("/{projectId}/files/{fileName}")
    @Operation(summary = "Download entity file", 
              description = "Downloads a specific generated entity or repository file")
    public ResponseEntity<String> downloadEntityFile(
            @Parameter(description = "Project ID", example = "proj_123456")
            @PathVariable String projectId,
            @Parameter(description = "File name", example = "User.java")
            @PathVariable String fileName) {
        log.info("Downloading file {} for project: {}", fileName, projectId);
        
        // TODO: Implement service call
        String fileContent = "// Generated file content placeholder";
        
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                .body(fileContent);
    }
    
    @GetMapping("/{projectId}/zip")
    @Operation(summary = "Download all entities as ZIP", 
              description = "Downloads all generated entities and repositories as a ZIP file")
    public ResponseEntity<byte[]> downloadAllEntities(
            @Parameter(description = "Project ID", example = "proj_123456")
            @PathVariable String projectId) {
        log.info("Downloading all entities for project: {}", projectId);
        
        // TODO: Implement service call
        byte[] zipContent = new byte[0];
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"entities_" + projectId + ".zip\"")
                .body(zipContent);
    }
}
EOF

# Create Global Exception Handler
echo "📦 Creating Global Exception Handler..."
cat > domain/common/GlobalExceptionHandler.java << 'EOF'
package com.gigapress.domainschema.domain.common;

import com.gigapress.domainschema.domain.common.dto.ApiResponse;
import com.gigapress.domainschema.domain.common.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleProjectNotFoundException(
            ProjectNotFoundException ex, WebRequest request) {
        log.error("Project not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), request.getDescription(false)));
    }
    
    @ExceptionHandler(InvalidRequirementException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequirementException(
            InvalidRequirementException ex, WebRequest request) {
        log.error("Invalid requirement: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), request.getDescription(false)));
    }
    
    @ExceptionHandler(DomainModelGenerationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainModelGenerationException(
            DomainModelGenerationException ex, WebRequest request) {
        log.error("Domain model generation failed: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ex.getMessage(), request.getDescription(false)));
    }
    
    @ExceptionHandler(SchemaGenerationException.class)
    public ResponseEntity<ApiResponse<Void>> handleSchemaGenerationException(
            SchemaGenerationException ex, WebRequest request) {
        log.error("Schema generation failed: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ex.getMessage(), request.getDescription(false)));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, String>>builder()
                        .success(false)
                        .error("Validation failed")
                        .data(errors)
                        .build());
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error occurred", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred", request.getDescription(false)));
    }
}
EOF

echo "✅ API Layer created successfully!"
echo ""
echo "📋 Created:"
echo "  - Common DTOs: ApiResponse, PageResponse"
echo "  - Request DTOs for all endpoints"
echo "  - Response DTOs for all endpoints"
echo "  - Controllers:"
echo "    - ProjectController"
echo "    - RequirementsController"
echo "    - DomainModelController"
echo "    - SchemaDesignController"
echo "    - EntityMappingController"
echo "  - Global Exception Handler"
echo ""
echo "🎯 Next step: Create Service Layer (Business Logic)"