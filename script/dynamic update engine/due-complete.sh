#!/bin/bash

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${BLUE}🚀 Completing Dynamic Update Engine Implementation${NC}"
echo "=================================================="

# Base package path
BASE_PATH="src/main/java/com/gigapress/dynamicupdate"
TEST_PATH="src/test/java/com/gigapress/dynamicupdate"

# 1. Create Exception Classes
echo -e "\n${YELLOW}📝 Creating exception classes...${NC}"

mkdir -p $BASE_PATH/exception

# ComponentNotFoundException.java
cat > $BASE_PATH/exception/ComponentNotFoundException.java << 'EOF'
package com.gigapress.dynamicupdate.exception;

public class ComponentNotFoundException extends RuntimeException {
    public ComponentNotFoundException(String message) {
        super(message);
    }
    
    public ComponentNotFoundException(String componentId, Throwable cause) {
        super("Component not found: " + componentId, cause);
    }
}
EOF

# CircularDependencyException.java
cat > $BASE_PATH/exception/CircularDependencyException.java << 'EOF'
package com.gigapress.dynamicupdate.exception;

public class CircularDependencyException extends RuntimeException {
    private final String sourceComponentId;
    private final String targetComponentId;
    
    public CircularDependencyException(String sourceComponentId, String targetComponentId) {
        super(String.format("Circular dependency detected between %s and %s", 
                sourceComponentId, targetComponentId));
        this.sourceComponentId = sourceComponentId;
        this.targetComponentId = targetComponentId;
    }
    
    public String getSourceComponentId() {
        return sourceComponentId;
    }
    
    public String getTargetComponentId() {
        return targetComponentId;
    }
}
EOF

# DependencyConflictException.java
cat > $BASE_PATH/exception/DependencyConflictException.java << 'EOF'
package com.gigapress.dynamicupdate.exception;

import java.util.Set;

public class DependencyConflictException extends RuntimeException {
    private final Set<String> conflictingComponents;
    
    public DependencyConflictException(String message, Set<String> conflictingComponents) {
        super(message);
        this.conflictingComponents = conflictingComponents;
    }
    
    public Set<String> getConflictingComponents() {
        return conflictingComponents;
    }
}
EOF

# GlobalExceptionHandler.java
cat > $BASE_PATH/exception/GlobalExceptionHandler.java << 'EOF'
package com.gigapress.dynamicupdate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.KafkaException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ComponentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleComponentNotFound(
            ComponentNotFoundException ex, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Component Not Found")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(CircularDependencyException.class)
    public ResponseEntity<ErrorResponse> handleCircularDependency(
            CircularDependencyException ex, WebRequest request) {
        Map<String, Object> details = new HashMap<>();
        details.put("sourceComponent", ex.getSourceComponentId());
        details.put("targetComponent", ex.getTargetComponentId());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Circular Dependency Detected")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .details(details)
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }
    
    @ExceptionHandler(DependencyConflictException.class)
    public ResponseEntity<ErrorResponse> handleDependencyConflict(
            DependencyConflictException ex, WebRequest request) {
        Map<String, Object> details = new HashMap<>();
        details.put("conflictingComponents", ex.getConflictingComponents());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Dependency Conflict")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .details(details)
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Invalid request parameters")
                .path(request.getDescription(false))
                .details(validationErrors)
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(KafkaException.class)
    public ResponseEntity<ErrorResponse> handleKafkaException(
            KafkaException ex, WebRequest request) {
        log.error("Kafka error occurred", ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .error("Message Processing Error")
                .message("Unable to process message queue operation")
                .path(request.getDescription(false))
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error occurred", ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred")
                .path(request.getDescription(false))
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
EOF

# ErrorResponse.java
cat > $BASE_PATH/exception/ErrorResponse.java << 'EOF'
package com.gigapress.dynamicupdate.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class ErrorResponse {
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, Object> details;
}
EOF

echo -e "${GREEN}✅ Exception classes created!${NC}"

# 2. Create Swagger Configuration
echo -e "\n${YELLOW}📝 Creating Swagger configuration...${NC}"

# Update build.gradle to include Swagger dependency
cat >> build.gradle << 'EOF'

// Add Swagger dependency
dependencies {
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'
}
EOF

# SwaggerConfig.java
cat > $BASE_PATH/config/SwaggerConfig.java << 'EOF'
package com.gigapress.dynamicupdate.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    
    @Value("${server.port}")
    private String serverPort;
    
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Dynamic Update Engine API")
                        .description("API for managing component dependencies and update propagation in GigaPress")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("GigaPress Team")
                                .email("team@gigapress.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server")
                ));
    }
}
EOF

# Add Swagger properties to application.properties
cat >> src/main/resources/application.properties << 'EOF'

# Swagger/OpenAPI Configuration
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operations-sorter=method
springdoc.swagger-ui.tags-sorter=alpha
EOF

echo -e "${GREEN}✅ Swagger configuration created!${NC}"

# 3. Create Health Check Components
echo -e "\n${YELLOW}📝 Creating health check components...${NC}"

# HealthIndicator implementations
cat > $BASE_PATH/health/Neo4jHealthIndicator.java << 'EOF'
package com.gigapress.dynamicupdate.health;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class Neo4jHealthIndicator implements HealthIndicator {
    
    private final Driver neo4jDriver;
    
    public Neo4jHealthIndicator(Driver neo4jDriver) {
        this.neo4jDriver = neo4jDriver;
    }
    
    @Override
    public Health health() {
        try (Session session = neo4jDriver.session()) {
            session.run("RETURN 1").consume();
            return Health.up()
                    .withDetail("database", "Neo4j")
                    .withDetail("status", "Connected")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", "Neo4j")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
EOF

cat > $BASE_PATH/health/KafkaHealthIndicator.java << 'EOF'
package com.gigapress.dynamicupdate.health;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class KafkaHealthIndicator implements HealthIndicator {
    
    private final KafkaAdmin kafkaAdmin;
    
    public KafkaHealthIndicator(KafkaAdmin kafkaAdmin) {
        this.kafkaAdmin = kafkaAdmin;
    }
    
    @Override
    public Health health() {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            DescribeClusterResult clusterResult = adminClient.describeCluster();
            String clusterId = clusterResult.clusterId().get(3, TimeUnit.SECONDS);
            int nodeCount = clusterResult.nodes().get(3, TimeUnit.SECONDS).size();
            
            return Health.up()
                    .withDetail("clusterId", clusterId)
                    .withDetail("nodeCount", nodeCount)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
EOF

# Update KafkaConfig to include KafkaAdmin bean
cat >> $BASE_PATH/config/KafkaConfig.java << 'EOF'

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }
EOF

echo -e "${GREEN}✅ Health check components created!${NC}"

# 4. Create Integration Tests
echo -e "\n${YELLOW}📝 Creating integration tests...${NC}"

mkdir -p $TEST_PATH/{repository,service,controller}

# Test configuration
cat > src/test/resources/application-test.properties << 'EOF'
# Test Configuration
spring.neo4j.uri=bolt://localhost:7687
spring.neo4j.authentication.username=neo4j
spring.neo4j.authentication.password=password123

spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=test-group

spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=redis123

logging.level.com.gigapress=DEBUG
EOF

# ComponentRepositoryTest.java
cat > $TEST_PATH/repository/ComponentRepositoryTest.java << 'EOF'
package com.gigapress.dynamicupdate.repository;

import com.gigapress.dynamicupdate.domain.Component;
import com.gigapress.dynamicupdate.domain.ComponentStatus;
import com.gigapress.dynamicupdate.domain.ComponentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataNeo4jTest
@ActiveProfiles("test")
class ComponentRepositoryTest {
    
    @Autowired
    private ComponentRepository componentRepository;
    
    @BeforeEach
    void setUp() {
        componentRepository.deleteAll();
    }
    
    @Test
    void shouldSaveAndFindComponent() {
        // Given
        Component component = Component.builder()
                .componentId("comp-123")
                .name("Test Component")
                .type(ComponentType.BACKEND)
                .version("1.0.0")
                .projectId("proj-123")
                .status(ComponentStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // When
        Component saved = componentRepository.save(component);
        Optional<Component> found = componentRepository.findByComponentId("comp-123");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Component");
        assertThat(found.get().getType()).isEqualTo(ComponentType.BACKEND);
    }
    
    @Test
    void shouldFindDependents() {
        // Given
        Component comp1 = createComponent("comp-1", "Component 1");
        Component comp2 = createComponent("comp-2", "Component 2");
        componentRepository.save(comp1);
        componentRepository.save(comp2);
        
        // Create dependency: comp2 depends on comp1
        comp2.addDependency(comp1, com.gigapress.dynamicupdate.domain.DependencyType.COMPILE);
        componentRepository.save(comp2);
        
        // When
        Set<Component> dependents = componentRepository.findDependents("comp-1");
        
        // Then
        assertThat(dependents).hasSize(1);
        assertThat(dependents.iterator().next().getComponentId()).isEqualTo("comp-2");
    }
    
    private Component createComponent(String id, String name) {
        return Component.builder()
                .componentId(id)
                .name(name)
                .type(ComponentType.BACKEND)
                .version("1.0.0")
                .projectId("proj-123")
                .status(ComponentStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
EOF

# ComponentServiceTest.java
cat > $TEST_PATH/service/ComponentServiceTest.java << 'EOF'
package com.gigapress.dynamicupdate.service;

import com.gigapress.dynamicupdate.domain.Component;
import com.gigapress.dynamicupdate.domain.ComponentStatus;
import com.gigapress.dynamicupdate.domain.ComponentType;
import com.gigapress.dynamicupdate.domain.DependencyType;
import com.gigapress.dynamicupdate.exception.CircularDependencyException;
import com.gigapress.dynamicupdate.repository.ComponentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComponentServiceTest {
    
    @Mock
    private ComponentRepository componentRepository;
    
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @InjectMocks
    private ComponentService componentService;
    
    @Test
    void shouldCreateComponent() {
        // Given
        Component component = createTestComponent("comp-123", "Test Component");
        when(componentRepository.save(any(Component.class))).thenReturn(component);
        
        // When
        Component result = componentService.createComponent(component);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getComponentId()).isEqualTo("comp-123");
        verify(kafkaTemplate, times(1)).send(anyString(), any());
    }
    
    @Test
    void shouldPreventCircularDependency() {
        // Given
        Component comp1 = createTestComponent("comp-1", "Component 1");
        Component comp2 = createTestComponent("comp-2", "Component 2");
        
        // Create circular dependency scenario
        comp1.addDependency(comp2, DependencyType.COMPILE);
        comp2.addDependency(comp1, DependencyType.COMPILE);
        
        when(componentRepository.findByComponentId("comp-1")).thenReturn(Optional.of(comp1));
        when(componentRepository.findByComponentId("comp-2")).thenReturn(Optional.of(comp2));
        
        // When & Then
        assertThatThrownBy(() -> 
            componentService.addDependency("comp-1", "comp-2", DependencyType.COMPILE)
        ).isInstanceOf(RuntimeException.class)
         .hasMessageContaining("Circular dependency detected");
    }
    
    private Component createTestComponent(String id, String name) {
        return Component.builder()
                .componentId(id)
                .name(name)
                .type(ComponentType.BACKEND)
                .version("1.0.0")
                .projectId("proj-123")
                .status(ComponentStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
EOF

# ComponentControllerTest.java
cat > $TEST_PATH/controller/ComponentControllerTest.java << 'EOF'
package com.gigapress.dynamicupdate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigapress.dynamicupdate.domain.Component;
import com.gigapress.dynamicupdate.domain.ComponentStatus;
import com.gigapress.dynamicupdate.domain.ComponentType;
import com.gigapress.dynamicupdate.dto.ComponentRequest;
import com.gigapress.dynamicupdate.service.ComponentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ComponentController.class)
class ComponentControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private ComponentService componentService;
    
    @Test
    void shouldCreateComponent() throws Exception {
        // Given
        ComponentRequest request = new ComponentRequest(
                "comp-123",
                "Test Component",
                ComponentType.BACKEND,
                "1.0.0",
                "proj-123",
                null
        );
        
        Component component = Component.builder()
                .componentId(request.getComponentId())
                .name(request.getName())
                .type(request.getType())
                .version(request.getVersion())
                .projectId(request.getProjectId())
                .status(ComponentStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        when(componentService.createComponent(any(Component.class))).thenReturn(component);
        
        // When & Then
        mockMvc.perform(post("/api/components")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.componentId").value("comp-123"))
                .andExpect(jsonPath("$.name").value("Test Component"));
    }
    
    @Test
    void shouldGetComponent() throws Exception {
        // Given
        Component component = Component.builder()
                .componentId("comp-123")
                .name("Test Component")
                .type(ComponentType.BACKEND)
                .version("1.0.0")
                .projectId("proj-123")
                .status(ComponentStatus.ACTIVE)
                .build();
        
        when(componentService.findByComponentId("comp-123")).thenReturn(Optional.of(component));
        
        // When & Then
        mockMvc.perform(get("/api/components/comp-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.componentId").value("comp-123"))
                .andExpect(jsonPath("$.name").value("Test Component"));
    }
    
    @Test
    void shouldReturn404WhenComponentNotFound() throws Exception {
        // Given
        when(componentService.findByComponentId("comp-999")).thenReturn(Optional.empty());
        
        // When & Then
        mockMvc.perform(get("/api/components/comp-999"))
                .andExpect(status().isNotFound());
    }
}
EOF

echo -e "${GREEN}✅ Integration tests created!${NC}"

# 5. Create Gradle Wrapper
echo -e "\n${YELLOW}📝 Creating Gradle wrapper...${NC}"

# Create gradle wrapper properties
mkdir -p gradle/wrapper
cat > gradle/wrapper/gradle-wrapper.properties << 'EOF'
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.5-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
EOF

# Create gradlew script
cat > gradlew << 'EOF'
#!/bin/sh
echo "Gradle wrapper not fully configured."
echo "Please run: gradle wrapper --gradle-version 8.5 --distribution-type all"
echo "Or download gradle and run the wrapper task."
EOF
chmod +x gradlew

# Create gradlew.bat for Windows
cat > gradlew.bat << 'EOF'
@echo off
echo Gradle wrapper not fully configured.
echo Please run: gradle wrapper --gradle-version 8.5 --distribution-type all
echo Or download gradle and run the wrapper task.
EOF

echo -e "${GREEN}✅ Gradle wrapper placeholder created!${NC}"

# 6. Update Service with exception handling
echo -e "\n${YELLOW}📝 Updating ComponentService with proper exception handling...${NC}"

# Update the service to use custom exceptions
sed -i 's/new RuntimeException("Component not found: " + componentId)/new ComponentNotFoundException(componentId)/g' $BASE_PATH/service/ComponentService.java
sed -i 's/new RuntimeException("Circular dependency detected")/new CircularDependencyException(sourceId, targetId)/g' $BASE_PATH/service/ComponentService.java

# Add import statements
sed -i '1a\
import com.gigapress.dynamicupdate.exception.ComponentNotFoundException;\
import com.gigapress.dynamicupdate.exception.CircularDependencyException;' $BASE_PATH/service/ComponentService.java

# 7. Create run scripts
echo -e "\n${YELLOW}📝 Creating run scripts...${NC}"

# Create build and run script
cat > build-and-run.sh << 'EOF'
#!/bin/bash

echo "🏗️ Building Dynamic Update Engine..."

# Check if gradle wrapper exists
if [ ! -f "./gradlew" ]; then
    echo "⚠️ Gradle wrapper not found. Please run: gradle wrapper --gradle-version 8.5"
    exit 1
fi

# Clean and build
./gradlew clean build

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo "🚀 Starting application..."
    ./gradlew bootRun
else
    echo "❌ Build failed!"
    exit 1
fi
EOF
chmod +x build-and-run.sh

# Create test script
cat > run-tests.sh << 'EOF'
#!/bin/bash

echo "🧪 Running tests..."

# Check if gradle wrapper exists
if [ ! -f "./gradlew" ]; then
    echo "⚠️ Gradle wrapper not found. Please run: gradle wrapper --gradle-version 8.5"
    exit 1
fi

# Run tests
./gradlew test

if [ $? -eq 0 ]; then
    echo "✅ All tests passed!"
else
    echo "❌ Some tests failed!"
    exit 1
fi
EOF
chmod +x run-tests.sh

echo -e "${GREEN}✅ Run scripts created!${NC}"

# Final summary
echo -e "\n${BLUE}========================================${NC}"
echo -e "${GREEN}✨ Dynamic Update Engine implementation completed!${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo "📁 Created files:"
echo "  Exception Handling:"
echo "    - ComponentNotFoundException.java"
echo "    - CircularDependencyException.java"
echo "    - DependencyConflictException.java"
echo "    - GlobalExceptionHandler.java"
echo "    - ErrorResponse.java"
echo ""
echo "  API Documentation:"
echo "    - SwaggerConfig.java"
echo ""
echo "  Health Checks:"
echo "    - Neo4jHealthIndicator.java"
echo "    - KafkaHealthIndicator.java"
echo ""
echo "  Tests:"
echo "    - ComponentRepositoryTest.java"
echo "    - ComponentServiceTest.java"
echo "    - ComponentControllerTest.java"
echo "    - application-test.properties"
echo ""
echo "  Scripts:"
echo "    - build-and-run.sh"
echo "    - run-tests.sh"
echo ""
echo "🚀 Next steps:"
echo "  1. Install Gradle and generate wrapper:"
echo "     ${YELLOW}gradle wrapper --gradle-version 8.5 --distribution-type all${NC}"
echo "  2. Build the project:"
echo "     ${YELLOW}./gradlew build${NC}"
echo "  3. Run tests:"
echo "     ${YELLOW}./run-tests.sh${NC}"
echo "  4. Start the application:"
echo "     ${YELLOW}./build-and-run.sh${NC}"
echo ""
echo "📋 API Documentation will be available at:"
echo "  - Swagger UI: http://localhost:8081/swagger-ui.html"
echo "  - API Docs: http://localhost:8081/api-docs"
echo ""
echo "📊 Health check endpoint:"
echo "  - http://localhost:8081/actuator/health"