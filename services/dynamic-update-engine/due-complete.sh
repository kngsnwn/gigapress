#!/bin/bash

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${BLUE}🚀 Completing Dynamic Update Engine Implementation${NC}"
echo "=================================================="

# Check if we're in the right directory
if [ ! -f "build.gradle" ]; then
    echo -e "${RED}❌ Error: build.gradle not found!${NC}"
    echo "Please run this script from the dynamic-update-engine directory"
    echo "Current directory: $(pwd)"
    exit 1
fi

# Base package path
BASE_PATH="src/main/java/com/gigapress/dynamicupdate"
TEST_PATH="src/test/java/com/gigapress/dynamicupdate"

# Check if base directories exist
if [ ! -d "$BASE_PATH" ]; then
    echo -e "${YELLOW}⚠️ Base package directory not found. Creating it...${NC}"
    mkdir -p $BASE_PATH
fi

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

# 2. Create Health Check Components
echo -e "\n${YELLOW}📝 Creating health check components...${NC}"

mkdir -p $BASE_PATH/health

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
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class KafkaHealthIndicator implements HealthIndicator {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Override
    public Health health() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        
        try (AdminClient adminClient = AdminClient.create(configs)) {
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

echo -e "${GREEN}✅ Health check components created!${NC}"

# 3. Update Swagger properties
echo -e "\n${YELLOW}📝 Adding Swagger properties...${NC}"

# Check if properties already contain swagger config
if ! grep -q "springdoc.api-docs.path" src/main/resources/application.properties; then
cat >> src/main/resources/application.properties << 'EOF'

# Swagger/OpenAPI Configuration
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operations-sorter=method
springdoc.swagger-ui.tags-sorter=alpha
EOF
fi

# 4. Create Swagger Configuration
echo -e "\n${YELLOW}📝 Creating Swagger configuration...${NC}"

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

# 5. Check if ComponentService exists and update it
echo -e "\n${YELLOW}📝 Checking for ComponentService...${NC}"

if [ -f "$BASE_PATH/service/ComponentService.java" ]; then
    echo -e "${GREEN}Found ComponentService.java, updating with exception handling...${NC}"
    
    # Create a temporary file with updated content
    cp $BASE_PATH/service/ComponentService.java $BASE_PATH/service/ComponentService.java.bak
    
    # Add imports if not present
    if ! grep -q "import com.gigapress.dynamicupdate.exception.ComponentNotFoundException;" $BASE_PATH/service/ComponentService.java; then
        sed -i '1a\
import com.gigapress.dynamicupdate.exception.ComponentNotFoundException;\
import com.gigapress.dynamicupdate.exception.CircularDependencyException;' $BASE_PATH/service/ComponentService.java
    fi
    
    # Replace exception throws
    sed -i 's/new RuntimeException("Component not found: " + componentId)/new ComponentNotFoundException(componentId)/g' $BASE_PATH/service/ComponentService.java
    sed -i 's/new RuntimeException("Source component not found: " + sourceId)/new ComponentNotFoundException("Source component not found: " + sourceId)/g' $BASE_PATH/service/ComponentService.java
    sed -i 's/new RuntimeException("Target component not found: " + targetId)/new ComponentNotFoundException("Target component not found: " + targetId)/g' $BASE_PATH/service/ComponentService.java
    sed -i 's/new RuntimeException("Circular dependency detected")/new CircularDependencyException(sourceId, targetId)/g' $BASE_PATH/service/ComponentService.java
    
    echo -e "${GREEN}✅ ComponentService updated with custom exceptions${NC}"
else
    echo -e "${YELLOW}⚠️ ComponentService.java not found. Make sure to run create-domain-services.sh first!${NC}"
fi

# 6. Update build.gradle to include Swagger dependency
echo -e "\n${YELLOW}📝 Updating build.gradle with Swagger dependency...${NC}"

if ! grep -q "springdoc-openapi" build.gradle; then
    # Find the dependencies block and add swagger
    sed -i '/dependencies {/a\
    // Swagger/OpenAPI\
    implementation "org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0"' build.gradle
    echo -e "${GREEN}✅ Swagger dependency added to build.gradle${NC}"
else
    echo -e "${GREEN}✅ Swagger dependency already exists in build.gradle${NC}"
fi

# 7. Create run scripts
echo -e "\n${YELLOW}📝 Creating run scripts...${NC}"

# Create build and run script
cat > build-and-run.sh << 'EOF'
#!/bin/bash

echo "🏗️ Building Dynamic Update Engine..."

# Check if gradle wrapper exists
if [ -f "./gradlew" ]; then
    echo "Using gradle wrapper..."
    GRADLE_CMD="./gradlew"
else
    echo "Gradle wrapper not found, using system gradle..."
    GRADLE_CMD="gradle"
fi

# Clean and build
$GRADLE_CMD clean build

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo "🚀 Starting application..."
    $GRADLE_CMD bootRun
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
if [ -f "./gradlew" ]; then
    echo "Using gradle wrapper..."
    GRADLE_CMD="./gradlew"
else
    echo "Gradle wrapper not found, using system gradle..."
    GRADLE_CMD="gradle"
fi

# Run tests
$GRADLE_CMD test

if [ $? -eq 0 ]; then
    echo "✅ All tests passed!"
else
    echo "❌ Some tests failed!"
    exit 1
fi
EOF
chmod +x run-tests.sh

echo -e "${GREEN}✅ Run scripts created!${NC}"

# Create gradle wrapper generation script
cat > generate-wrapper.sh << 'EOF'
#!/bin/bash

echo "📦 Generating Gradle Wrapper..."

if command -v gradle &> /dev/null; then
    gradle wrapper --gradle-version 8.5 --distribution-type all
    echo "✅ Gradle wrapper generated successfully!"
else
    echo "❌ Gradle is not installed. Please install Gradle first."
    echo "Visit: https://gradle.org/install/"
fi
EOF
chmod +x generate-wrapper.sh

# Final summary
echo -e "\n${BLUE}========================================${NC}"
echo -e "${GREEN}✨ Dynamic Update Engine additional components created!${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo "📁 Created files:"
echo "  - Exception handling classes"
echo "  - Health check indicators"
echo "  - Swagger configuration"
echo "  - Run scripts"
echo ""
echo "🔍 Current status:"
ls -la $BASE_PATH/
echo ""
echo "🚀 Next steps:"
echo "  1. Generate Gradle wrapper (if not exists):"
echo "     ${YELLOW}./generate-wrapper.sh${NC}"
echo "  2. Build the project:"
echo "     ${YELLOW}./build-and-run.sh${NC}"
echo ""
echo "📋 When running, access:"
echo "  - API Documentation: http://localhost:8081/swagger-ui.html"
echo "  - Health Check: http://localhost:8081/actuator/health"