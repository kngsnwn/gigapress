package com.gigapress.mcp.service;

import com.gigapress.mcp.event.EventProducer;
import com.gigapress.mcp.model.domain.Component;
import com.gigapress.mcp.model.domain.Project;
import com.gigapress.mcp.model.event.ProjectEvent;
import com.gigapress.mcp.model.request.ProjectGenerationRequest;
import com.gigapress.mcp.model.response.ProjectGenerationResponse;
import com.gigapress.mcp.model.response.ProjectGenerationResponse.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectGenerationService {
    
    private final EventProducer eventProducer;
    
    public Mono<ProjectGenerationResponse> generateProject(ProjectGenerationRequest request) {
        log.info("Generating project: {}", request.getProjectName());
        
        String projectId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        
        return Mono.fromCallable(() -> {
            // Generate project structure
            ProjectStructure structure = generateProjectStructure(request);
            
            // Generate components
            List<GeneratedComponent> components = generateComponents(request, projectId);
            
            // Create setup instructions
            SetupInstructions instructions = generateSetupInstructions(request);
            
            long duration = System.currentTimeMillis() - startTime;
            
            return ProjectGenerationResponse.builder()
                .projectId(projectId)
                .projectName(request.getProjectName())
                .generationStatus(GenerationStatus.SUCCESS)
                .projectStructure(structure)
                .generatedComponents(components)
                .setupInstructions(instructions)
                .generationDurationMs(duration)
                .build();
        })
        .doOnSuccess(response -> publishProjectCreatedEvent(response, request))
        .doOnError(error -> log.error("Error generating project", error));
    }
    
    private ProjectStructure generateProjectStructure(ProjectGenerationRequest request) {
        Map<String, List<String>> directoryStructure = new HashMap<>();
        List<String> configFiles = new ArrayList<>();
        Map<String, String> mainEntryPoints = new HashMap<>();
        
        switch (request.getProjectType()) {
            case WEB_APPLICATION -> {
                directoryStructure.put("frontend", List.of("src", "public", "components", "pages"));
                directoryStructure.put("backend", List.of("src", "controllers", "services", "models"));
                directoryStructure.put("database", List.of("migrations", "seeds"));
                
                configFiles.addAll(List.of("package.json", "tsconfig.json", "webpack.config.js"));
                
                mainEntryPoints.put("frontend", "src/index.tsx");
                mainEntryPoints.put("backend", "src/server.ts");
            }
            case API_SERVICE -> {
                directoryStructure.put("src", List.of("controllers", "services", "models", "middleware"));
                directoryStructure.put("tests", List.of("unit", "integration"));
                directoryStructure.put("docs", List.of("api", "schemas"));
                
                configFiles.addAll(List.of("package.json", "tsconfig.json", ".env.example"));
                
                mainEntryPoints.put("api", "src/app.ts");
            }
            case MICROSERVICES -> {
                directoryStructure.put("services", List.of("auth", "user", "product", "order"));
                directoryStructure.put("shared", List.of("models", "utils", "config"));
                directoryStructure.put("infrastructure", List.of("docker", "kubernetes"));
                
                configFiles.addAll(List.of("docker-compose.yml", "lerna.json"));
                
                mainEntryPoints.put("gateway", "gateway/src/index.ts");
            }
            default -> {
                directoryStructure.put("src", List.of("main", "test", "resources"));
                configFiles.add("build.gradle");
                mainEntryPoints.put("main", "src/main/Main.java");
            }
        }
        
        return ProjectStructure.builder()
            .rootPath(request.getProjectName().toLowerCase().replace(" ", "-"))
            .directoryStructure(directoryStructure)
            .configFiles(configFiles)
            .mainEntryPoints(mainEntryPoints)
            .build();
    }
    
    private List<GeneratedComponent> generateComponents(
            ProjectGenerationRequest request, 
            String projectId) {
        
        List<GeneratedComponent> components = new ArrayList<>();
        
        // Generate based on project type and features
        switch (request.getProjectType()) {
            case WEB_APPLICATION -> {
                components.add(createComponent("frontend", "Frontend Application", 
                    "frontend", List.of("App.tsx", "index.tsx", "router.tsx")));
                components.add(createComponent("backend", "Backend API", 
                    "backend", List.of("server.ts", "routes.ts", "database.ts")));
                components.add(createComponent("database", "Database Schema", 
                    "database", List.of("schema.sql", "migrations/")));
            }
            case API_SERVICE -> {
                components.add(createComponent("api", "REST API", 
                    "src", List.of("app.ts", "routes/", "controllers/", "services/")));
                components.add(createComponent("auth", "Authentication Module", 
                    "src/auth", List.of("auth.service.ts", "jwt.strategy.ts")));
            }
            case MICROSERVICES -> {
                components.add(createComponent("gateway", "API Gateway", 
                    "services/gateway", List.of("index.ts", "proxy.ts")));
                components.add(createComponent("auth-service", "Auth Service", 
                    "services/auth", List.of("server.ts", "auth.controller.ts")));
                components.add(createComponent("user-service", "User Service", 
                    "services/user", List.of("server.ts", "user.controller.ts")));
            }
            default -> {
                components.add(createComponent("core", "Core Module", 
                    "src/main", List.of("Main.java", "Application.java")));
            }
        }
        
        // Add components based on requested features
        if (request.getFeatures() != null) {
            for (String feature : request.getFeatures()) {
                components.add(generateFeatureComponent(feature, projectId));
            }
        }
        
        return components;
    }
    
    private GeneratedComponent createComponent(
            String id, 
            String name, 
            String location, 
            List<String> files) {
        
        return GeneratedComponent.builder()
            .componentId(id)
            .componentName(name)
            .componentType("module")
            .location(location)
            .files(files)
            .configuration(new HashMap<>())
            .build();
    }
    
    private GeneratedComponent generateFeatureComponent(String feature, String projectId) {
        String componentId = feature.toLowerCase().replace(" ", "-");
        
        return GeneratedComponent.builder()
            .componentId(componentId)
            .componentName(feature)
            .componentType("feature")
            .location("src/features/" + componentId)
            .files(List.of(
                componentId + ".service.ts",
                componentId + ".controller.ts",
                componentId + ".model.ts"
            ))
            .configuration(Map.of("enabled", true))
            .build();
    }
    
    private SetupInstructions generateSetupInstructions(ProjectGenerationRequest request) {
        List<String> prerequisites = new ArrayList<>();
        List<String> installationSteps = new ArrayList<>();
        Map<String, String> environmentVariables = new HashMap<>();
        String runCommand = "";
        String testCommand = "";
        
        // Add technology-specific instructions
        if (request.getTechnologyStack() != null) {
            var stack = request.getTechnologyStack();
            
            if ("node".equalsIgnoreCase(stack.getBackend()) || 
                "react".equalsIgnoreCase(stack.getFrontend())) {
                prerequisites.add("Node.js 18+ and npm");
                installationSteps.add("npm install");
                runCommand = "npm start";
                testCommand = "npm test";
            }
            
            if ("java".equalsIgnoreCase(stack.getBackend())) {
                prerequisites.add("Java 17+ JDK");
                prerequisites.add("Gradle or Maven");
                installationSteps.add("./gradlew build");
                runCommand = "./gradlew bootRun";
                testCommand = "./gradlew test";
            }
            
            if (stack.getDatabase() != null) {
                prerequisites.add(stack.getDatabase() + " database");
                environmentVariables.put("DATABASE_URL", "connection_string_here");
                environmentVariables.put("DB_USER", "username");
                environmentVariables.put("DB_PASSWORD", "password");
            }
        }
        
        // Common setup steps
        installationSteps.add(0, "Clone the repository");
        installationSteps.add("Copy .env.example to .env and configure");
        
        return SetupInstructions.builder()
            .prerequisites(prerequisites)
            .installationSteps(installationSteps)
            .environmentVariables(environmentVariables)
            .runCommand(runCommand)
            .testCommand(testCommand)
            .build();
    }
    
    private void publishProjectCreatedEvent(
            ProjectGenerationResponse response, 
            ProjectGenerationRequest request) {
        
        ProjectEvent event = ProjectEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .eventType(ProjectEvent.EventType.PROJECT_CREATED)
            .projectId(response.getProjectId())
            .sourceService("MCP Server")
            .payload(Map.of(
                "projectName", response.getProjectName(),
                "projectType", request.getProjectType().toString(),
                "componentCount", response.getGeneratedComponents().size()
            ))
            .build();
        
        eventProducer.publishProjectEvent(event);
    }
}
