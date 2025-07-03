package com.gigapress.domainschema.util;

import com.gigapress.domainschema.domain.analysis.dto.request.*;
import com.gigapress.domainschema.domain.common.entity.*;

import java.util.*;

public class TestDataBuilder {
    
    public static CreateProjectRequest createProjectRequest() {
        return CreateProjectRequest.builder()
                .name("Test Project " + UUID.randomUUID().toString().substring(0, 8))
                .description("Test project description")
                .projectType(ProjectType.WEB_APPLICATION)
                .build();
    }
    
    public static AnalyzeRequirementsRequest analyzeRequirementsRequest(String projectId) {
        return AnalyzeRequirementsRequest.builder()
                .projectId(projectId)
                .naturalLanguageRequirements("Users should be able to perform CRUD operations")
                .constraints(Arrays.asList("RESTful API", "JWT Authentication"))
                .technologyPreferences(Map.of("backend", "Spring Boot", "database", "PostgreSQL"))
                .build();
    }
    
    public static AddRequirementRequest addRequirementRequest() {
        return AddRequirementRequest.builder()
                .title("Test Requirement")
                .description("This is a test requirement")
                .type(RequirementType.FUNCTIONAL)
                .priority(RequirementPriority.MEDIUM)
                .metadata(Map.of("test", "true"))
                .build();
    }
    
    public static Project createProject() {
        return Project.builder()
                .projectId("test_" + UUID.randomUUID().toString().substring(0, 8))
                .name("Test Project")
                .description("Test Description")
                .projectType(ProjectType.WEB_APPLICATION)
                .status(ProjectStatus.CREATED)
                .build();
    }
    
    public static Requirement createRequirement(Project project) {
        return Requirement.builder()
                .title("Test Requirement")
                .description("Test requirement description")
                .type(RequirementType.FUNCTIONAL)
                .priority(RequirementPriority.HIGH)
                .status(RequirementStatus.PENDING)
                .project(project)
                .build();
    }
}
