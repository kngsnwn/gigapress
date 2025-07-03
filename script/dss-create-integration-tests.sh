#!/bin/bash

# Integration & Testing Script for Domain/Schema Service
set -e

echo "🧪 Creating Integration Tests for Domain/Schema Service..."

# Navigate to test directory
cd services/domain-schema-service/src/test/java/com/gigapress/domainschema

# Create test configuration
echo "📦 Creating test configuration..."
mkdir -p config

# TestConfig
cat > config/TestConfig.java << 'EOF'
package com.gigapress.domainschema.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
@EmbeddedKafka(partitions = 1, topics = {"project-events", "domain-analyzed", "schema-generated"})
public class TestConfig {
    
    @Bean
    @Primary
    public PostgreSQLContainer<?> postgresContainer() {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
                .withDatabaseName("gigapress_test")
                .withUsername("test")
                .withPassword("test");
        container.start();
        return container;
    }
    
    @Bean
    @Primary
    public GenericContainer<?> redisContainer() {
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                .withExposedPorts(6379);
        container.start();
        return container;
    }
}
EOF

# Create Integration Test Base
cat > IntegrationTestBase.java << 'EOF'
package com.gigapress.domainschema;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@EmbeddedKafka(partitions = 1, topics = {"project-events", "domain-analyzed", "schema-generated"})
public abstract class IntegrationTestBase {
    // Common test setup can be added here
}
EOF

# Create Controller Integration Tests
echo "📦 Creating Controller Integration Tests..."
mkdir -p domain/analysis/controller

# ProjectControllerIntegrationTest
cat > domain/analysis/controller/ProjectControllerIntegrationTest.java << 'EOF'
package com.gigapress.domainschema.domain.analysis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigapress.domainschema.IntegrationTestBase;
import com.gigapress.domainschema.domain.analysis.dto.request.CreateProjectRequest;
import com.gigapress.domainschema.domain.common.entity.ProjectType;
import com.gigapress.domainschema.domain.common.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProjectControllerIntegrationTest extends IntegrationTestBase {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @BeforeEach
    void setUp() {
        projectRepository.deleteAll();
    }
    
    @Test
    void createProject_ShouldReturnCreatedProject() throws Exception {
        // Given
        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("E-Commerce Platform")
                .description("A modern e-commerce platform with microservices")
                .projectType(ProjectType.WEB_APPLICATION)
                .build();
        
        // When & Then
        mockMvc.perform(post("/api/v1/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("E-Commerce Platform"))
                .andExpect(jsonPath("$.data.projectType").value("WEB_APPLICATION"))
                .andExpect(jsonPath("$.data.status").value("CREATED"))
                .andExpect(jsonPath("$.data.projectId").isNotEmpty());
    }
    
    @Test
    void createProject_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given
        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("") // Invalid: empty name
                .projectType(null) // Invalid: null type
                .build();
        
        // When & Then
        mockMvc.perform(post("/api/v1/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.data.name").exists())
                .andExpect(jsonPath("$.data.projectType").exists());
    }
    
    @Test
    void getProject_WithExistingProject_ShouldReturnProject() throws Exception {
        // Given
        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("Test Project")
                .projectType(ProjectType.REST_API)
                .build();
        
        String response = mockMvc.perform(post("/api/v1/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        String projectId = objectMapper.readTree(response)
                .path("data")
                .path("projectId")
                .asText();
        
        // When & Then
        mockMvc.perform(get("/api/v1/projects/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectId").value(projectId))
                .andExpect(jsonPath("$.data.name").value("Test Project"));
    }
    
    @Test
    void getProject_WithNonExistentProject_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/projects/{projectId}", "non_existent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value(containsString("Project not found")));
    }
    
    @Test
    void listProjects_ShouldReturnPaginatedList() throws Exception {
        // Given - Create multiple projects
        for (int i = 1; i <= 5; i++) {
            CreateProjectRequest request = CreateProjectRequest.builder()
                    .name("Project " + i)
                    .projectType(ProjectType.WEB_APPLICATION)
                    .build();
            
            mockMvc.perform(post("/api/v1/projects")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        }
        
        // When & Then
        mockMvc.perform(get("/api/v1/projects")
                .param("page", "0")
                .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andExpect(jsonPath("$.data.totalElements").value(5))
                .andExpect(jsonPath("$.data.totalPages").value(2));
    }
}
EOF

# RequirementsControllerIntegrationTest
cat > domain/analysis/controller/RequirementsControllerIntegrationTest.java << 'EOF'
package com.gigapress.domainschema.domain.analysis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigapress.domainschema.IntegrationTestBase;
import com.gigapress.domainschema.domain.analysis.dto.request.AnalyzeRequirementsRequest;
import com.gigapress.domainschema.domain.analysis.dto.request.CreateProjectRequest;
import com.gigapress.domainschema.domain.common.entity.ProjectType;
import com.gigapress.domainschema.integration.mcp.client.McpServerClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RequirementsControllerIntegrationTest extends IntegrationTestBase {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private McpServerClient mcpServerClient;
    
    private String projectId;
    
    @BeforeEach
    void setUp() throws Exception {
        // Create a test project
        CreateProjectRequest projectRequest = CreateProjectRequest.builder()
                .name("Test Project")
                .projectType(ProjectType.WEB_APPLICATION)
                .build();
        
        String response = mockMvc.perform(post("/api/v1/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        projectId = objectMapper.readTree(response)
                .path("data")
                .path("projectId")
                .asText();
    }
    
    @Test
    void analyzeRequirements_ShouldReturnAnalysisResults() throws Exception {
        // Given
        AnalyzeRequirementsRequest request = AnalyzeRequirementsRequest.builder()
                .projectId(projectId)
                .naturalLanguageRequirements("Users should be able to register and login. " +
                        "Products should have categories and customer reviews.")
                .constraints(Arrays.asList("Must support mobile devices", "Handle 1000 concurrent users"))
                .build();
        
        // Mock MCP Server response
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("summary", "E-commerce platform with user management and product catalog");
        
        List<Map<String, Object>> requirements = new ArrayList<>();
        Map<String, Object> req1 = new HashMap<>();
        req1.put("title", "User Registration");
        req1.put("description", "Users should be able to register with email and password");
        req1.put("type", "FUNCTIONAL");
        req1.put("priority", "HIGH");
        req1.put("metadata", new HashMap<>());
        requirements.add(req1);
        
        mockResponse.put("requirements", requirements);
        mockResponse.put("identifiedEntities", Arrays.asList("User", "Product", "Category", "Review"));
        mockResponse.put("suggestedRelationships", Arrays.asList("User-Review", "Product-Category", "Product-Review"));
        mockResponse.put("confidenceScore", 0.95);
        
        Mockito.when(mcpServerClient.analyzeRequirements(any())).thenReturn(mockResponse);
        
        // When & Then
        mockMvc.perform(post("/api/v1/requirements/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectId").value(projectId))
                .andExpect(jsonPath("$.data.summary").exists())
                .andExpect(jsonPath("$.data.requirements").isArray())
                .andExpect(jsonPath("$.data.identifiedEntities").isArray())
                .andExpect(jsonPath("$.data.confidenceScore").value(0.95));
    }
}
EOF

# Create Service Integration Tests
echo "📦 Creating Service Integration Tests..."
mkdir -p domain/analysis/service

# ProjectServiceIntegrationTest
cat > domain/analysis/service/ProjectServiceIntegrationTest.java << 'EOF'
package com.gigapress.domainschema.domain.analysis.service;

import com.gigapress.domainschema.IntegrationTestBase;
import com.gigapress.domainschema.domain.analysis.dto.request.CreateProjectRequest;
import com.gigapress.domainschema.domain.analysis.dto.response.ProjectResponse;
import com.gigapress.domainschema.domain.common.entity.ProjectType;
import com.gigapress.domainschema.domain.common.exception.ProjectNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.test.context.EmbeddedKafka;

import static org.assertj.core.api.Assertions.*;

class ProjectServiceIntegrationTest extends IntegrationTestBase {
    
    @Autowired
    private ProjectService projectService;
    
    @Test
    void createProject_ShouldCreateAndPublishEvent() {
        // Given
        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("Integration Test Project")
                .description("Testing project creation with events")
                .projectType(ProjectType.MICROSERVICE)
                .build();
        
        // When
        ProjectResponse response = projectService.createProject(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getProjectId()).isNotEmpty();
        assertThat(response.getName()).isEqualTo("Integration Test Project");
        assertThat(response.getStatus()).isEqualTo("CREATED");
        
        // Verify project can be retrieved
        ProjectResponse retrieved = projectService.getProject(response.getProjectId());
        assertThat(retrieved.getProjectId()).isEqualTo(response.getProjectId());
    }
    
    @Test
    void deleteProject_ShouldRemoveProject() {
        // Given
        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("Project to Delete")
                .projectType(ProjectType.REST_API)
                .build();
        
        ProjectResponse created = projectService.createProject(request);
        String projectId = created.getProjectId();
        
        // When
        projectService.deleteProject(projectId);
        
        // Then
        assertThatThrownBy(() -> projectService.getProject(projectId))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessageContaining(projectId);
    }
}
EOF

# Create Kafka Integration Tests
echo "📦 Creating Kafka Integration Tests..."
mkdir -p integration/kafka

# KafkaEventIntegrationTest
cat > integration/kafka/KafkaEventIntegrationTest.java << 'EOF'
package com.gigapress.domainschema.integration.kafka;

import com.gigapress.domainschema.IntegrationTestBase;
import com.gigapress.domainschema.domain.common.event.ProjectCreatedEvent;
import com.gigapress.domainschema.integration.kafka.producer.DomainEventProducer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaEventIntegrationTest extends IntegrationTestBase {
    
    @Autowired
    private DomainEventProducer eventProducer;
    
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    
    @Test
    void publishProjectCreatedEvent_ShouldBeConsumed() {
        // Given
        String projectId = "test_proj_123";
        ProjectCreatedEvent event = ProjectCreatedEvent.builder()
                .projectId(projectId)
                .projectName("Test Project")
                .projectType(com.gigapress.domainschema.domain.common.entity.ProjectType.WEB_APPLICATION)
                .description("Test Description")
                .build();
        
        // Setup consumer
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group", "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        
        Consumer<String, ProjectCreatedEvent> consumer = new DefaultKafkaConsumerFactory<>(
                consumerProps, 
                new StringDeserializer(), 
                new JsonDeserializer<>(ProjectCreatedEvent.class))
                .createConsumer();
        
        consumer.subscribe(Collections.singletonList("project-events"));
        
        // When
        eventProducer.publishProjectCreatedEvent(event);
        
        // Then
        ConsumerRecords<String, ProjectCreatedEvent> records = consumer.poll(Duration.ofSeconds(10));
        assertThat(records.count()).isEqualTo(1);
        
        ProjectCreatedEvent received = records.iterator().next().value();
        assertThat(received.getAggregateId()).isEqualTo(projectId);
        assertThat(received.getProjectName()).isEqualTo("Test Project");
        
        consumer.close();
    }
}
EOF

# Create End-to-End Tests
echo "📦 Creating End-to-End Tests..."
mkdir -p e2e

# ProjectLifecycleE2ETest
cat > e2e/ProjectLifecycleE2ETest.java << 'EOF'
package com.gigapress.domainschema.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigapress.domainschema.IntegrationTestBase;
import com.gigapress.domainschema.domain.analysis.dto.request.*;
import com.gigapress.domainschema.domain.common.entity.ProjectType;
import com.gigapress.domainschema.domain.common.entity.RequirementPriority;
import com.gigapress.domainschema.domain.common.entity.RequirementType;
import com.gigapress.domainschema.integration.mcp.client.McpServerClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProjectLifecycleE2ETest extends IntegrationTestBase {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private McpServerClient mcpServerClient;
    
    @Test
    void completeProjectLifecycle_FromCreationToRequirements() throws Exception {
        // Step 1: Create Project
        CreateProjectRequest createRequest = CreateProjectRequest.builder()
                .name("E2E Test E-Commerce Platform")
                .description("Complete e-commerce platform with all features")
                .projectType(ProjectType.WEB_APPLICATION)
                .build();
        
        String createResponse = mockMvc.perform(post("/api/v1/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        String projectId = objectMapper.readTree(createResponse)
                .path("data")
                .path("projectId")
                .asText();
        
        // Step 2: Add Manual Requirement
        AddRequirementRequest addReqRequest = AddRequirementRequest.builder()
                .title("User Authentication")
                .description("Users must be able to register and login securely")
                .type(RequirementType.FUNCTIONAL)
                .priority(RequirementPriority.CRITICAL)
                .build();
        
        mockMvc.perform(post("/api/v1/requirements/{projectId}", projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addReqRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
        
        // Step 3: Analyze Natural Language Requirements
        AnalyzeRequirementsRequest analyzeRequest = AnalyzeRequirementsRequest.builder()
                .projectId(projectId)
                .naturalLanguageRequirements(
                    "The platform should support product catalog with categories. " +
                    "Customers should be able to add items to cart and checkout. " +
                    "Payment processing should support credit cards and PayPal. " +
                    "Order tracking and email notifications are required.")
                .constraints(Arrays.asList(
                    "Must be PCI compliant",
                    "Support 10,000 concurrent users",
                    "Mobile responsive design"))
                .build();
        
        // Mock MCP Server response
        Map<String, Object> mockAnalysis = createMockAnalysisResponse();
        Mockito.when(mcpServerClient.analyzeRequirements(any())).thenReturn(mockAnalysis);
        
        mockMvc.perform(post("/api/v1/requirements/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(analyzeRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.requirements").isArray());
        
        // Step 4: Verify All Requirements
        mockMvc.perform(get("/api/v1/requirements/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(5)); // 1 manual + 4 analyzed
        
        // Step 5: Verify Project Status Updated
        mockMvc.perform(get("/api/v1/projects/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DESIGNING"))
                .andExpect(jsonPath("$.data.requirementCount").value(5));
    }
    
    private Map<String, Object> createMockAnalysisResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("summary", "E-commerce platform with product catalog, cart, checkout, and order management");
        
        List<Map<String, Object>> requirements = new ArrayList<>();
        
        // Requirement 1
        Map<String, Object> req1 = new HashMap<>();
        req1.put("title", "Product Catalog");
        req1.put("description", "Support product catalog with hierarchical categories");
        req1.put("type", "FUNCTIONAL");
        req1.put("priority", "HIGH");
        req1.put("metadata", Map.of("component", "catalog"));
        requirements.add(req1);
        
        // Requirement 2
        Map<String, Object> req2 = new HashMap<>();
        req2.put("title", "Shopping Cart");
        req2.put("description", "Add items to cart with quantity management");
        req2.put("type", "FUNCTIONAL");
        req2.put("priority", "HIGH");
        req2.put("metadata", Map.of("component", "cart"));
        requirements.add(req2);
        
        // Requirement 3
        Map<String, Object> req3 = new HashMap<>();
        req3.put("title", "Payment Processing");
        req3.put("description", "Integrate credit card and PayPal payment methods");
        req3.put("type", "FUNCTIONAL");
        req3.put("priority", "CRITICAL");
        req3.put("metadata", Map.of("component", "payment", "compliance", "PCI"));
        requirements.add(req3);
        
        // Requirement 4
        Map<String, Object> req4 = new HashMap<>();
        req4.put("title", "Order Tracking");
        req4.put("description", "Track order status with email notifications");
        req4.put("type", "FUNCTIONAL");
        req4.put("priority", "MEDIUM");
        req4.put("metadata", Map.of("component", "orders"));
        requirements.add(req4);
        
        response.put("requirements", requirements);
        response.put("identifiedEntities", Arrays.asList(
            "User", "Product", "Category", "Cart", "CartItem", 
            "Order", "OrderItem", "Payment", "Notification"
        ));
        response.put("suggestedRelationships", Arrays.asList(
            "User-Cart", "Cart-CartItem", "CartItem-Product",
            "User-Order", "Order-OrderItem", "OrderItem-Product",
            "Order-Payment", "Order-Notification"
        ));
        response.put("technologyRecommendations", Map.of(
            "frontend", "React with Redux",
            "backend", "Spring Boot",
            "database", "PostgreSQL",
            "payment", "Stripe API",
            "notifications", "SendGrid"
        ));
        response.put("confidenceScore", 0.92);
        
        return response;
    }
}
EOF

# Create Performance Tests
echo "📦 Creating Performance Tests..."
mkdir -p performance

# ProjectServicePerformanceTest
cat > performance/ProjectServicePerformanceTest.java << 'EOF'
package com.gigapress.domainschema.performance;

import com.gigapress.domainschema.IntegrationTestBase;
import com.gigapress.domainschema.domain.analysis.dto.request.CreateProjectRequest;
import com.gigapress.domainschema.domain.analysis.service.ProjectService;
import com.gigapress.domainschema.domain.common.entity.ProjectType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectServicePerformanceTest extends IntegrationTestBase {
    
    @Autowired
    private ProjectService projectService;
    
    @Test
    void createProjects_ConcurrentRequests_ShouldHandleLoad() throws Exception {
        // Given
        int numberOfThreads = 10;
        int projectsPerThread = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        List<Future<List<String>>> futures = new ArrayList<>();
        
        // When
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            Future<List<String>> future = executor.submit(() -> {
                List<String> projectIds = new ArrayList<>();
                try {
                    for (int j = 0; j < projectsPerThread; j++) {
                        CreateProjectRequest request = CreateProjectRequest.builder()
                                .name("Perf Test Project " + threadId + "-" + j)
                                .projectType(ProjectType.WEB_APPLICATION)
                                .build();
                        
                        String projectId = projectService.createProject(request).getProjectId();
                        projectIds.add(projectId);
                    }
                } finally {
                    latch.countDown();
                }
                return projectIds;
            });
            futures.add(future);
        }
        
        // Wait for all threads to complete
        latch.await(30, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();
        
        // Then
        List<String> allProjectIds = new ArrayList<>();
        for (Future<List<String>> future : futures) {
            allProjectIds.addAll(future.get());
        }
        
        assertThat(allProjectIds).hasSize(numberOfThreads * projectsPerThread);
        
        long totalTime = endTime - startTime;
        double avgTimePerProject = (double) totalTime / (numberOfThreads * projectsPerThread);
        
        System.out.println("Performance Test Results:");
        System.out.println("Total projects created: " + allProjectIds.size());
        System.out.println("Total time: " + totalTime + "ms");
        System.out.println("Average time per project: " + avgTimePerProject + "ms");
        
        assertThat(avgTimePerProject).isLessThan(100); // Should create each project in less than 100ms
        
        executor.shutdown();
    }
}
EOF

# Create Test Utilities
echo "📦 Creating Test Utilities..."
mkdir -p util

# TestDataBuilder
cat > util/TestDataBuilder.java << 'EOF'
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
EOF

# Create test resources
echo "📦 Creating test resources..."
cd ../../../../resources

# Create test SQL data
mkdir -p sql
cat > sql/test-data.sql << 'EOF'
-- Test data for integration tests
INSERT INTO domain_schema.projects (id, project_id, name, description, project_type, status, created_at, updated_at, version)
VALUES 
    (1000, 'test_proj_sample', 'Sample Test Project', 'A sample project for testing', 'WEB_APPLICATION', 'CREATED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO domain_schema.requirements (id, title, description, type, priority, status, project_id, created_at, updated_at, version)
VALUES 
    (2000, 'Sample Requirement', 'A sample requirement for testing', 'FUNCTIONAL', 'HIGH', 'PENDING', 1000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
EOF

# Update test application properties
cat >> application-test.properties << 'EOF'

# Test data initialization
spring.sql.init.mode=always
spring.sql.init.data-locations=classpath:sql/test-data.sql

# Testcontainers
spring.testcontainers.enabled=true

# Logging for tests
logging.level.org.springframework.test=INFO
logging.level.org.testcontainers=INFO
EOF

# Create run tests script
echo "🧪 Creating test runner script..."
cd ../../../..

cat > run-tests.sh << 'EOF'
#!/bin/bash

echo "🧪 Running Domain/Schema Service Tests..."

# Set test profile
export SPRING_PROFILES_ACTIVE=test

# Run different test suites
echo "Running unit tests..."
./gradlew test --tests "*Test" --info

echo "Running integration tests..."
./gradlew test --tests "*IntegrationTest" --info

echo "Running E2E tests..."
./gradlew test --tests "*E2ETest" --info

echo "Running performance tests..."
./gradlew test --tests "*PerformanceTest" --info

# Generate test report
echo "Generating test report..."
./gradlew jacocoTestReport

echo "✅ All tests completed!"
echo "📊 Test report available at: build/reports/tests/test/index.html"
echo "📊 Coverage report available at: build/reports/jacoco/test/html/index.html"
EOF

chmod +x run-tests.sh

# Update build.gradle for test coverage
echo "📦 Updating build.gradle for test coverage..."
cd domain-schema-service

# Add JaCoCo plugin
sed -i "/plugins {/a\\    id 'jacoco'" build.gradle

# Add test configuration
cat >> build.gradle << 'EOF'

// Test configuration
test {
    useJUnitPlatform()
    testLogging {
        events "passed", "skipped", "failed"
    }
}

// JaCoCo configuration
jacoco {
    toolVersion = "0.8.8"
}

jacocoTestReport {
    dependsOn test
    reports {
        xml.required = true
        html.required = true
    }
}

// Test dependencies
dependencies {
    testImplementation 'org.testcontainers:testcontainers:1.19.3'
    testImplementation 'org.testcontainers:postgresql:1.19.3'
    testImplementation 'org.testcontainers:kafka:1.19.3'
    testImplementation 'org.testcontainers:junit-jupiter:1.19.3'
    testImplementation 'org.springframework.kafka:spring-kafka-test'
    testImplementation 'com.redis.testcontainers:testcontainers-redis-junit:1.6.4'
}
EOF

echo "✅ Integration and Testing setup completed!"
echo ""
echo "📋 Created:"
echo "  - Test Configuration:"
echo "    - TestConfig with Testcontainers"
echo "    - IntegrationTestBase"
echo "  - Controller Integration Tests:"
echo "    - ProjectControllerIntegrationTest"
echo "    - RequirementsControllerIntegrationTest"
echo "  - Service Integration Tests:"
echo "    - ProjectServiceIntegrationTest"
echo "  - Kafka Integration Tests:"
echo "    - KafkaEventIntegrationTest"
echo "  - End-to-End Tests:"
echo "    - ProjectLifecycleE2ETest"
echo "  - Performance Tests:"
echo "    - ProjectServicePerformanceTest"
echo "  - Test Utilities:"
echo "    - TestDataBuilder"
echo "  - Test Resources:"
echo "    - test-data.sql"
echo "    - Updated application-test.properties"
echo "  - Test Runner Script:"
echo "    - run-tests.sh"
echo ""
echo "🎯 To run tests:"
echo "1. All tests: ./run-tests.sh"
echo "2. Specific test: ./gradlew test --tests 'ProjectControllerIntegrationTest'"
echo "3. With coverage: ./gradlew test jacocoTestReport"