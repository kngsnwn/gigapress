#!/bin/bash

# MCP Server Integration Tests and Scripts Creation

echo "🧪 Creating integration tests and scripts for MCP Server..."

BASE_DIR="services/mcp-server"
TEST_DIR="$BASE_DIR/src/test/java/com/gigapress/mcp"
SCRIPT_DIR="$BASE_DIR/scripts"

# Create directories
mkdir -p $TEST_DIR/{integration,service}
mkdir -p $SCRIPT_DIR

# ===== SERVICE TESTS =====

# ChangeAnalysisServiceTest.java
echo "📝 Creating ChangeAnalysisServiceTest.java..."
cat > $TEST_DIR/service/ChangeAnalysisServiceTest.java << 'EOF'
package com.gigapress.mcp.service;

import com.gigapress.mcp.client.DynamicUpdateEngineClient;
import com.gigapress.mcp.event.EventProducer;
import com.gigapress.mcp.model.domain.Component;
import com.gigapress.mcp.model.domain.DependencyGraph;
import com.gigapress.mcp.model.request.ChangeAnalysisRequest;
import com.gigapress.mcp.model.response.ChangeAnalysisResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangeAnalysisServiceTest {
    
    @Mock
    private DynamicUpdateEngineClient dynamicUpdateEngineClient;
    
    @Mock
    private EventProducer eventProducer;
    
    @InjectMocks
    private ChangeAnalysisService changeAnalysisService;
    
    private DependencyGraph testGraph;
    
    @BeforeEach
    void setUp() {
        testGraph = new DependencyGraph();
        testGraph.setNodes(new HashMap<>());
        testGraph.addNode("comp1", Component.builder()
            .componentId("comp1")
            .componentName("Component 1")
            .type(Component.ComponentType.BACKEND)
            .build());
        testGraph.addNode("comp2", Component.builder()
            .componentId("comp2")
            .componentName("Component 2")
            .type(Component.ComponentType.FRONTEND)
            .build());
        testGraph.addEdge("comp1", "comp2", DependencyGraph.EdgeType.DEPENDS_ON);
    }
    
    @Test
    void testAnalyzeChange_Success() {
        // Given
        ChangeAnalysisRequest request = ChangeAnalysisRequest.builder()
            .projectId("test-project")
            .changeDescription("Update API endpoint")
            .changeType(ChangeAnalysisRequest.ChangeType.FEATURE_MODIFY)
            .targetComponents(new String[]{"comp1"})
            .analysisDepth(ChangeAnalysisRequest.AnalysisDepth.NORMAL)
            .build();
        
        when(dynamicUpdateEngineClient.getDependencyGraph(anyString()))
            .thenReturn(Mono.just(testGraph));
        
        // When
        Mono<ChangeAnalysisResponse> result = changeAnalysisService.analyzeChange(request);
        
        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response);
                assertEquals("test-project", response.getProjectId());
                assertNotNull(response.getAnalysisId());
                assertNotNull(response.getImpactSummary());
                assertEquals(2, response.getAffectedComponents().size());
                assertNotNull(response.getRiskAssessment());
                assertNotNull(response.getRecommendations());
                assertFalse(response.getRecommendations().isEmpty());
            })
            .verifyComplete();
        
        verify(eventProducer, times(1)).publishAnalysisEvent(any());
    }
    
    @Test
    void testAnalyzeChange_EmptyGraph() {
        // Given
        ChangeAnalysisRequest request = ChangeAnalysisRequest.builder()
            .projectId("empty-project")
            .changeDescription("Initial setup")
            .changeType(ChangeAnalysisRequest.ChangeType.FEATURE_ADD)
            .build();
        
        when(dynamicUpdateEngineClient.getDependencyGraph(anyString()))
            .thenReturn(Mono.just(new DependencyGraph()));
        
        // When
        Mono<ChangeAnalysisResponse> result = changeAnalysisService.analyzeChange(request);
        
        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response);
                assertEquals(0, response.getAffectedComponents().size());
            })
            .verifyComplete();
    }
}
EOF

# ProjectGenerationServiceTest.java
echo "📝 Creating ProjectGenerationServiceTest.java..."
cat > $TEST_DIR/service/ProjectGenerationServiceTest.java << 'EOF'
package com.gigapress.mcp.service;

import com.gigapress.mcp.event.EventProducer;
import com.gigapress.mcp.model.request.ProjectGenerationRequest;
import com.gigapress.mcp.model.response.ProjectGenerationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectGenerationServiceTest {
    
    @Mock
    private EventProducer eventProducer;
    
    @InjectMocks
    private ProjectGenerationService projectGenerationService;
    
    @Test
    void testGenerateWebApplication() {
        // Given
        ProjectGenerationRequest request = ProjectGenerationRequest.builder()
            .projectName("Test Web App")
            .projectDescription("A test web application")
            .projectType(ProjectGenerationRequest.ProjectType.WEB_APPLICATION)
            .features(List.of("Authentication", "User Management"))
            .technologyStack(ProjectGenerationRequest.TechnologyStack.builder()
                .frontend("react")
                .backend("node")
                .database("postgresql")
                .build())
            .build();
        
        // When
        Mono<ProjectGenerationResponse> result = projectGenerationService.generateProject(request);
        
        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response);
                assertEquals("Test Web App", response.getProjectName());
                assertEquals(ProjectGenerationResponse.GenerationStatus.SUCCESS, 
                    response.getGenerationStatus());
                assertNotNull(response.getProjectStructure());
                assertFalse(response.getGeneratedComponents().isEmpty());
                assertNotNull(response.getSetupInstructions());
                assertTrue(response.getGenerationDurationMs() > 0);
            })
            .verifyComplete();
        
        verify(eventProducer, times(1)).publishProjectEvent(any());
    }
    
    @Test
    void testGenerateMicroservices() {
        // Given
        ProjectGenerationRequest request = ProjectGenerationRequest.builder()
            .projectName("Test Microservices")
            .projectDescription("A microservices architecture")
            .projectType(ProjectGenerationRequest.ProjectType.MICROSERVICES)
            .build();
        
        // When
        Mono<ProjectGenerationResponse> result = projectGenerationService.generateProject(request);
        
        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response);
                assertTrue(response.getProjectStructure().getDirectoryStructure()
                    .containsKey("services"));
                assertTrue(response.getGeneratedComponents().stream()
                    .anyMatch(c -> c.getComponentId().equals("gateway")));
            })
            .verifyComplete();
    }
}
EOF

# ===== INTEGRATION TEST =====

# McpServerIntegrationTest.java
echo "📝 Creating McpServerIntegrationTest.java..."
cat > $TEST_DIR/integration/McpServerIntegrationTest.java << 'EOF'
package com.gigapress.mcp.integration;

import com.gigapress.mcp.model.request.*;
import com.gigapress.mcp.model.response.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class McpServerIntegrationTest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private WebTestClient webTestClient;
    
    @Test
    void testHealthEndpoint() {
        webTestClient.get()
            .uri("/api/tools/health")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.success").isEqualTo(true)
            .jsonPath("$.data.status").isEqualTo("UP");
    }
    
    @Test
    void testCompleteProjectWorkflow() {
        // Step 1: Generate Project
        ProjectGenerationRequest generateRequest = ProjectGenerationRequest.builder()
            .projectName("Integration Test Project")
            .projectDescription("Test project for integration testing")
            .projectType(ProjectGenerationRequest.ProjectType.WEB_APPLICATION)
            .features(List.of("Authentication", "API"))
            .build();
        
        String projectId = webTestClient.post()
            .uri("/api/tools/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(generateRequest)
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.success").isEqualTo(true)
            .jsonPath("$.data.projectId").value(String.class);
        
        assertNotNull(projectId);
        
        // Step 2: Analyze Change Impact
        ChangeAnalysisRequest analysisRequest = ChangeAnalysisRequest.builder()
            .projectId(projectId)
            .changeDescription("Add new user profile feature")
            .changeType(ChangeAnalysisRequest.ChangeType.FEATURE_ADD)
            .build();
        
        webTestClient.mutate()
            .responseTimeout(Duration.ofSeconds(30))
            .build()
            .post()
            .uri("/api/tools/analyze")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(analysisRequest)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.success").isEqualTo(true)
            .jsonPath("$.data.analysisId").isNotEmpty();
        
        // Step 3: Validate Project
        ValidationRequest validationRequest = ValidationRequest.builder()
            .projectId(projectId)
            .validationTypes(List.of(
                ValidationRequest.ValidationType.DEPENDENCY_CONSISTENCY,
                ValidationRequest.ValidationType.CODE_QUALITY
            ))
            .build();
        
        webTestClient.post()
            .uri("/api/tools/validate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(validationRequest)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.success").isEqualTo(true)
            .jsonPath("$.data.validationId").isNotEmpty();
    }
}
EOF

# Create test application.properties
echo "📝 Creating application-test.properties..."
cat > $BASE_DIR/src/test/resources/application-test.properties << 'EOF'
# Test Profile Configuration
server.port=0
spring.profiles.active=test

# Use embedded Redis for tests
spring.data.redis.host=localhost
spring.data.redis.port=6370
spring.data.redis.password=

# Use embedded Kafka for tests
spring.kafka.bootstrap-servers=localhost:9093
spring.kafka.consumer.auto-offset-reset=earliest

# Disable real connections in tests
dynamic-update-engine.base-url=http://localhost:8999
dynamic-update-engine.connect-timeout=1000
dynamic-update-engine.read-timeout=1000

# Logging
logging.level.com.gigapress.mcp=DEBUG
EOF

# ===== DOCKERFILE =====

echo "📝 Creating Dockerfile..."
cat > $BASE_DIR/Dockerfile << 'EOF'
# Build stage
FROM gradle:8-jdk17 AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon
COPY src ./src
RUN gradle build --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Add user for security
RUN addgroup -g 1000 spring && \
    adduser -u 1000 -G spring -s /bin/sh -D spring

# Copy jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# Switch to non-root user
USER spring:spring

# Expose port
EXPOSE 8082

# JVM options for container
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
EOF

# ===== DOCKER COMPOSE =====

echo "📝 Creating docker-compose.yml for MCP Server..."
cat > $BASE_DIR/docker-compose.yml << 'EOF'
version: '3.8'

services:
  mcp-server:
    build:
      context: .
      dockerfile: Dockerfile
    image: gigapress/mcp-server:latest
    container_name: mcp-server
    ports:
      - "8082:8082"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATA_REDIS_HOST=redis
      - SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:29092
      - DYNAMIC_UPDATE_ENGINE_BASE_URL=http://dynamic-update-engine:8081
    depends_on:
      - redis
      - kafka
    networks:
      - gigapress-network
    restart: unless-stopped

# Reference external services from main docker-compose
networks:
  gigapress-network:
    external: true
EOF

# Create application-docker.properties
echo "📝 Creating application-docker.properties..."
cat > $BASE_DIR/src/main/resources/application-docker.properties << 'EOF'
# Docker Profile Configuration
server.port=8082

# Redis Configuration
spring.data.redis.host=${SPRING_DATA_REDIS_HOST:redis}
spring.data.redis.port=6379
spring.data.redis.password=redis123

# Kafka Configuration
spring.kafka.bootstrap-servers=${SPRING_KAFKA_BOOTSTRAP_SERVERS:kafka:29092}

# Dynamic Update Engine Configuration
dynamic-update-engine.base-url=${DYNAMIC_UPDATE_ENGINE_BASE_URL:http://dynamic-update-engine:8081}

# Logging
logging.level.root=INFO
logging.level.com.gigapress.mcp=INFO
EOF

# ===== SCRIPTS =====

# Create build script
echo "📝 Creating build.sh..."
cat > $SCRIPT_DIR/build.sh << 'EOF'
#!/bin/bash

echo "🔨 Building MCP Server..."

# Navigate to project root
cd "$(dirname "$0")/.."

# Clean and build
./gradlew clean build

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo "📦 JAR location: build/libs/"
else
    echo "❌ Build failed!"
    exit 1
fi
EOF
chmod +x $SCRIPT_DIR/build.sh

# Create run script
echo "📝 Creating run.sh..."
cat > $SCRIPT_DIR/run.sh << 'EOF'
#!/bin/bash

echo "🚀 Starting MCP Server..."

# Navigate to project root
cd "$(dirname "$0")/.."

# Check if jar exists
if [ ! -f build/libs/*.jar ]; then
    echo "⚠️  JAR file not found. Building project..."
    ./scripts/build.sh
fi

# Run with dev profile
echo "Starting server on port 8082..."
./gradlew bootRun --args='--spring.profiles.active=dev'
EOF
chmod +x $SCRIPT_DIR/run.sh

# Create docker build script
echo "📝 Creating docker-build.sh..."
cat > $SCRIPT_DIR/docker-build.sh << 'EOF'
#!/bin/bash

echo "🐳 Building Docker image for MCP Server..."

# Navigate to project root
cd "$(dirname "$0")/.."

# Build Docker image
docker build -t gigapress/mcp-server:latest .

if [ $? -eq 0 ]; then
    echo "✅ Docker image built successfully!"
    echo "📦 Image: gigapress/mcp-server:latest"
else
    echo "❌ Docker build failed!"
    exit 1
fi
EOF
chmod +x $SCRIPT_DIR/docker-build.sh

# Create test script
echo "📝 Creating test.sh..."
cat > $SCRIPT_DIR/test.sh << 'EOF'
#!/bin/bash

echo "🧪 Running MCP Server tests..."

# Navigate to project root
cd "$(dirname "$0")/.."

# Run tests
./gradlew test

if [ $? -eq 0 ]; then
    echo "✅ All tests passed!"
    echo "📊 Test report: build/reports/tests/test/index.html"
else
    echo "❌ Tests failed!"
    exit 1
fi
EOF
chmod +x $SCRIPT_DIR/test.sh

# Create API test script
echo "📝 Creating test-api.sh..."
cat > $SCRIPT_DIR/test-api.sh << 'EOF'
#!/bin/bash

# API Test Script for MCP Server

BASE_URL="http://localhost:8082"

echo "🧪 Testing MCP Server APIs..."
echo "================================"

# Test 1: Health Check
echo "1. Testing Health Check..."
curl -s "$BASE_URL/api/tools/health" | jq '.'
echo ""

# Test 2: Generate Project
echo "2. Testing Project Generation..."
PROJECT_RESPONSE=$(curl -s -X POST "$BASE_URL/api/tools/generate" \
  -H "Content-Type: application/json" \
  -d '{
    "project_name": "Test Shopping Mall",
    "project_description": "E-commerce platform with modern features",
    "project_type": "WEB_APPLICATION",
    "technology_stack": {
      "frontend": "react",
      "backend": "node",
      "database": "postgresql"
    },
    "features": ["Authentication", "Product Catalog", "Shopping Cart", "Payment"]
  }')

echo "$PROJECT_RESPONSE" | jq '.'
PROJECT_ID=$(echo "$PROJECT_RESPONSE" | jq -r '.data.project_id')
echo "Generated Project ID: $PROJECT_ID"
echo ""

# Test 3: Analyze Change Impact
echo "3. Testing Change Analysis..."
curl -s -X POST "$BASE_URL/api/tools/analyze" \
  -H "Content-Type: application/json" \
  -d "{
    \"project_id\": \"$PROJECT_ID\",
    \"change_description\": \"Add user review feature\",
    \"change_type\": \"FEATURE_ADD\",
    \"target_components\": [\"backend\", \"frontend\"],
    \"analysis_depth\": \"NORMAL\"
  }" | jq '.'
echo ""

# Test 4: Update Components
echo "4. Testing Component Update..."
curl -s -X PUT "$BASE_URL/api/tools/update" \
  -H "Content-Type: application/json" \
  -d "{
    \"project_id\": \"$PROJECT_ID\",
    \"updates\": [
      {
        \"component_id\": \"auth-service\",
        \"update_type\": \"MODIFY\",
        \"update_content\": {
          \"feature\": \"two-factor-auth\"
        }
      }
    ],
    \"update_strategy\": \"INCREMENTAL\"
  }" | jq '.'
echo ""

# Test 5: Validate Consistency
echo "5. Testing Validation..."
curl -s -X POST "$BASE_URL/api/tools/validate" \
  -H "Content-Type: application/json" \
  -d "{
    \"project_id\": \"$PROJECT_ID\",
    \"validation_types\": [
      \"DEPENDENCY_CONSISTENCY\",
      \"CODE_QUALITY\",
      \"API_CONTRACT\"
    ],
    \"include_warnings\": true
  }" | jq '.'
echo ""

echo "✅ API tests completed!"
echo ""
echo "📚 For interactive API testing, visit:"
echo "   Swagger UI: $BASE_URL/swagger-ui.html"
EOF
chmod +x $SCRIPT_DIR/test-api.sh

# Create complete setup script
echo "📝 Creating setup-complete.sh..."
cat > $SCRIPT_DIR/setup-complete.sh << 'EOF'
#!/bin/bash

echo "🚀 Complete MCP Server Setup"
echo "============================"

# Navigate to project root
cd "$(dirname "$0")/.."

# Step 1: Build the project
echo "Step 1: Building project..."
./scripts/build.sh
if [ $? -ne 0 ]; then exit 1; fi

# Step 2: Run tests
echo -e "\nStep 2: Running tests..."
./scripts/test.sh
if [ $? -ne 0 ]; then exit 1; fi

# Step 3: Build Docker image
echo -e "\nStep 3: Building Docker image..."
./scripts/docker-build.sh
if [ $? -ne 0 ]; then exit 1; fi

echo -e "\n✅ MCP Server setup completed successfully!"
echo ""
echo "📋 Next steps:"
echo "1. Run locally: ./scripts/run.sh"
echo "2. Run with Docker: docker-compose up"
echo "3. Test APIs: ./scripts/test-api.sh"
echo "4. View API docs: http://localhost:8082/swagger-ui.html"
EOF
chmod +x $SCRIPT_DIR/setup-complete.sh

echo "✅ Integration tests and scripts created successfully!"
echo ""
echo "📋 Created components:"
echo "  Tests:"
echo "    - Service unit tests"
echo "    - Integration tests"
echo "    - Test configuration"
echo ""
echo "  Docker:"
echo "    - Dockerfile (multi-stage build)"
echo "    - docker-compose.yml"
echo "    - Docker profile configuration"
echo ""
echo "  Scripts:"
echo "    - build.sh - Build the project"
echo "    - run.sh - Run locally"
echo "    - test.sh - Run tests"
echo "    - docker-build.sh - Build Docker image"
echo "    - test-api.sh - Test API endpoints"
echo "    - setup-complete.sh - Complete setup"