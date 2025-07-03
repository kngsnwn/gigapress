#!/bin/bash

# Backend Service Setup Script for GigaPress Project
# This script creates the complete Backend Service (Port 8084)

echo "🚀 Creating GigaPress Backend Service..."

# Base directory
BASE_DIR="services/backend-service"
mkdir -p $BASE_DIR
cd $BASE_DIR

# Create Gradle build file
cat > build.gradle << 'EOF'
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.4'
}

group = 'com.gigapress'
version = '1.0.0'
sourceCompatibility = '17'

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    
    // Kafka
    implementation 'org.springframework.kafka:spring-kafka'
    
    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.3'
    
    // OpenAPI Documentation
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'
    
    // Template Engine for Code Generation
    implementation 'org.freemarker:freemarker:2.3.32'
    
    // Database
    runtimeOnly 'com.h2database:h2'
    runtimeOnly 'org.postgresql:postgresql'
    
    // Development Tools
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
    
    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.kafka:spring-kafka-test'
    testImplementation 'org.springframework.security:spring-security-test'
}

tasks.named('test') {
    useJUnitPlatform()
}
EOF

# Create settings.gradle
cat > settings.gradle << 'EOF'
rootProject.name = 'backend-service'
EOF

# Create directory structure
mkdir -p src/main/{java,resources}/com/gigapress/backend/{config,controller,service,repository,model,dto,security,template,event,client,exception,util}
mkdir -p src/test/java/com/gigapress/backend

# Create application.properties
cat > src/main/resources/application.properties << 'EOF'
# Server Configuration
server.port=8084
spring.application.name=backend-service

# Database Configuration
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

# Redis Configuration
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=redis123

# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=backend-service
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=*
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer

# JWT Configuration
jwt.secret=gigapress-backend-service-secret-key-2025-very-long-and-secure
jwt.expiration=86400000

# Service URLs
service.mcp-server.url=http://localhost:8082
service.domain-schema.url=http://localhost:8083
service.dynamic-update.url=http://localhost:8081

# OpenAPI Configuration
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.enabled=true

# Logging
logging.level.com.gigapress=DEBUG
logging.level.org.springframework.kafka=INFO
EOF

# Create Main Application Class
cat > src/main/java/com/gigapress/backend/BackendServiceApplication.java << 'EOF'
package com.gigapress.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableKafka
@EnableCaching
@EnableAsync
public class BackendServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendServiceApplication.class, args);
    }
}
EOF

# Create Security Configuration
cat > src/main/java/com/gigapress/backend/config/SecurityConfig.java << 'EOF'
package com.gigapress.backend.config;

import com.gigapress.backend.security.JwtAuthenticationFilter;
import com.gigapress.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**", "/swagger-ui/**", "/api-docs/**", "/actuator/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
EOF

# Create JWT Token Provider
cat > src/main/java/com/gigapress/backend/security/JwtTokenProvider.java << 'EOF'
package com.gigapress.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpiration);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(authToken);
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }
}
EOF

# Create JWT Authentication Filter
cat > src/main/java/com/gigapress/backend/security/JwtAuthenticationFilter.java << 'EOF'
package com.gigapress.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = getTokenFromRequest(request);

        if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
            String username = tokenProvider.getUsernameFromToken(token);
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(username, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
EOF

# Create API Generation Service
cat > src/main/java/com/gigapress/backend/service/ApiGenerationService.java << 'EOF'
package com.gigapress.backend.service;

import com.gigapress.backend.dto.ApiSpecification;
import com.gigapress.backend.dto.GeneratedApi;
import com.gigapress.backend.template.ApiTemplateEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiGenerationService {

    private final ApiTemplateEngine templateEngine;
    private final KafkaProducerService kafkaProducerService;

    public GeneratedApi generateApiEndpoints(ApiSpecification specification) {
        log.info("Generating API endpoints for: {}", specification.getApiName());
        
        try {
            // Generate controller code
            String controllerCode = templateEngine.generateController(specification);
            
            // Generate service code
            String serviceCode = templateEngine.generateService(specification);
            
            // Generate repository code
            String repositoryCode = templateEngine.generateRepository(specification);
            
            // Generate DTO classes
            Map<String, String> dtoClasses = templateEngine.generateDtos(specification);
            
            // Create response
            GeneratedApi generatedApi = GeneratedApi.builder()
                    .apiName(specification.getApiName())
                    .controllerCode(controllerCode)
                    .serviceCode(serviceCode)
                    .repositoryCode(repositoryCode)
                    .dtoClasses(dtoClasses)
                    .build();
            
            // Send event to Kafka
            kafkaProducerService.sendApiGeneratedEvent(generatedApi);
            
            return generatedApi;
            
        } catch (Exception e) {
            log.error("Error generating API endpoints", e);
            throw new RuntimeException("Failed to generate API endpoints", e);
        }
    }
}
EOF

# Create API Template Engine
cat > src/main/java/com/gigapress/backend/template/ApiTemplateEngine.java << 'EOF'
package com.gigapress.backend.template;

import com.gigapress.backend.dto.ApiSpecification;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ApiTemplateEngine {

    private final Configuration freemarkerConfig;

    public ApiTemplateEngine() {
        this.freemarkerConfig = new Configuration(Configuration.VERSION_2_3_32);
        this.freemarkerConfig.setClassForTemplateLoading(this.getClass(), "/templates");
        this.freemarkerConfig.setDefaultEncoding("UTF-8");
    }

    public String generateController(ApiSpecification spec) {
        try {
            Template template = freemarkerConfig.getTemplate("controller.ftl");
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("packageName", spec.getPackageName());
            dataModel.put("entityName", spec.getEntityName());
            dataModel.put("apiPath", spec.getApiPath());
            
            StringWriter writer = new StringWriter();
            template.process(dataModel, writer);
            return writer.toString();
        } catch (Exception e) {
            log.error("Error generating controller", e);
            throw new RuntimeException("Failed to generate controller", e);
        }
    }

    public String generateService(ApiSpecification spec) {
        try {
            Template template = freemarkerConfig.getTemplate("service.ftl");
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("packageName", spec.getPackageName());
            dataModel.put("entityName", spec.getEntityName());
            
            StringWriter writer = new StringWriter();
            template.process(dataModel, writer);
            return writer.toString();
        } catch (Exception e) {
            log.error("Error generating service", e);
            throw new RuntimeException("Failed to generate service", e);
        }
    }

    public String generateRepository(ApiSpecification spec) {
        try {
            Template template = freemarkerConfig.getTemplate("repository.ftl");
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("packageName", spec.getPackageName());
            dataModel.put("entityName", spec.getEntityName());
            
            StringWriter writer = new StringWriter();
            template.process(dataModel, writer);
            return writer.toString();
        } catch (Exception e) {
            log.error("Error generating repository", e);
            throw new RuntimeException("Failed to generate repository", e);
        }
    }

    public Map<String, String> generateDtos(ApiSpecification spec) {
        Map<String, String> dtos = new HashMap<>();
        
        // Generate request DTO
        String requestDto = generateDto(spec, "request");
        dtos.put(spec.getEntityName() + "Request", requestDto);
        
        // Generate response DTO
        String responseDto = generateDto(spec, "response");
        dtos.put(spec.getEntityName() + "Response", responseDto);
        
        return dtos;
    }

    private String generateDto(ApiSpecification spec, String type) {
        try {
            Template template = freemarkerConfig.getTemplate("dto.ftl");
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("packageName", spec.getPackageName());
            dataModel.put("entityName", spec.getEntityName());
            dataModel.put("dtoType", type);
            dataModel.put("fields", spec.getFields());
            
            StringWriter writer = new StringWriter();
            template.process(dataModel, writer);
            return writer.toString();
        } catch (Exception e) {
            log.error("Error generating DTO", e);
            throw new RuntimeException("Failed to generate DTO", e);
        }
    }
}
EOF

# Create Kafka Producer Service
cat > src/main/java/com/gigapress/backend/service/KafkaProducerService.java << 'EOF'
package com.gigapress.backend.service;

import com.gigapress.backend.dto.GeneratedApi;
import com.gigapress.backend.event.ApiGeneratedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "api-generation-events";

    public void sendApiGeneratedEvent(GeneratedApi generatedApi) {
        ApiGeneratedEvent event = new ApiGeneratedEvent();
        event.setApiName(generatedApi.getApiName());
        event.setTimestamp(System.currentTimeMillis());
        event.setStatus("COMPLETED");
        
        log.info("Sending API generated event: {}", event);
        kafkaTemplate.send(TOPIC, event);
    }
}
EOF

# Create DTOs
cat > src/main/java/com/gigapress/backend/dto/ApiSpecification.java << 'EOF'
package com.gigapress.backend.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ApiSpecification {
    private String apiName;
    private String entityName;
    private String packageName;
    private String apiPath;
    private List<FieldSpecification> fields;
    private Map<String, String> operations;
    private AuthenticationRequirement authentication;
    
    @Data
    public static class FieldSpecification {
        private String name;
        private String type;
        private boolean required;
        private String validation;
    }
    
    @Data
    public static class AuthenticationRequirement {
        private boolean required;
        private String type;
        private List<String> roles;
    }
}
EOF

cat > src/main/java/com/gigapress/backend/dto/GeneratedApi.java << 'EOF'
package com.gigapress.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class GeneratedApi {
    private String apiName;
    private String controllerCode;
    private String serviceCode;
    private String repositoryCode;
    private Map<String, String> dtoClasses;
    private String openApiSpec;
}
EOF

# Create Event classes
cat > src/main/java/com/gigapress/backend/event/ApiGeneratedEvent.java << 'EOF'
package com.gigapress.backend.event;

import lombok.Data;

@Data
public class ApiGeneratedEvent {
    private String apiName;
    private Long timestamp;
    private String status;
}
EOF

# Create Controller
cat > src/main/java/com/gigapress/backend/controller/ApiGenerationController.java << 'EOF'
package com.gigapress.backend.controller;

import com.gigapress.backend.dto.ApiSpecification;
import com.gigapress.backend.dto.GeneratedApi;
import com.gigapress.backend.service.ApiGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/generation")
@RequiredArgsConstructor
@Tag(name = "API Generation", description = "API Generation endpoints")
public class ApiGenerationController {

    private final ApiGenerationService apiGenerationService;

    @PostMapping("/generate")
    @Operation(summary = "Generate API endpoints", description = "Generate REST API endpoints based on specification")
    public ResponseEntity<GeneratedApi> generateApi(@RequestBody ApiSpecification specification) {
        GeneratedApi generatedApi = apiGenerationService.generateApiEndpoints(specification);
        return ResponseEntity.ok(generatedApi);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the service is running")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Backend Service is running");
    }
}
EOF

# Create Template files directory
mkdir -p src/main/resources/templates

# Create Controller Template
cat > src/main/resources/templates/controller.ftl << 'EOF'
package ${packageName}.controller;

import ${packageName}.dto.${entityName}Request;
import ${packageName}.dto.${entityName}Response;
import ${packageName}.service.${entityName}Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${apiPath}")
@RequiredArgsConstructor
@Tag(name = "${entityName}", description = "${entityName} management APIs")
public class ${entityName}Controller {

    private final ${entityName}Service ${entityName?uncap_first}Service;

    @GetMapping
    @Operation(summary = "Get all ${entityName}s")
    public ResponseEntity<List<${entityName}Response>> getAll() {
        return ResponseEntity.ok(${entityName?uncap_first}Service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ${entityName} by ID")
    public ResponseEntity<${entityName}Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(${entityName?uncap_first}Service.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create new ${entityName}")
    public ResponseEntity<${entityName}Response> create(@RequestBody ${entityName}Request request) {
        return ResponseEntity.ok(${entityName?uncap_first}Service.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update ${entityName}")
    public ResponseEntity<${entityName}Response> update(@PathVariable Long id, @RequestBody ${entityName}Request request) {
        return ResponseEntity.ok(${entityName?uncap_first}Service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete ${entityName}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ${entityName?uncap_first}Service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
EOF

# Create Service Template
cat > src/main/resources/templates/service.ftl << 'EOF'
package ${packageName}.service;

import ${packageName}.dto.${entityName}Request;
import ${packageName}.dto.${entityName}Response;
import ${packageName}.repository.${entityName}Repository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ${entityName}Service {

    private final ${entityName}Repository ${entityName?uncap_first}Repository;

    public List<${entityName}Response> findAll() {
        log.info("Finding all ${entityName}s");
        // Implementation here
        return List.of();
    }

    public ${entityName}Response findById(Long id) {
        log.info("Finding ${entityName} by id: {}", id);
        // Implementation here
        return new ${entityName}Response();
    }

    public ${entityName}Response create(${entityName}Request request) {
        log.info("Creating new ${entityName}");
        // Implementation here
        return new ${entityName}Response();
    }

    public ${entityName}Response update(Long id, ${entityName}Request request) {
        log.info("Updating ${entityName} with id: {}", id);
        // Implementation here
        return new ${entityName}Response();
    }

    public void delete(Long id) {
        log.info("Deleting ${entityName} with id: {}", id);
        // Implementation here
    }
}
EOF

# Create Repository Template
cat > src/main/resources/templates/repository.ftl << 'EOF'
package ${packageName}.repository;

import ${packageName}.entity.${entityName};
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ${entityName}Repository extends JpaRepository<${entityName}, Long> {
    // Custom query methods can be added here
}
EOF

# Create DTO Template
cat > src/main/resources/templates/dto.ftl << 'EOF'
package ${packageName}.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class ${entityName}${dtoType?cap_first} {
<#list fields as field>
    <#if field.required>
    @NotNull(message = "${field.name} is required")
    </#if>
    <#if field.validation??>
    ${field.validation}
    </#if>
    private ${field.type} ${field.name};
</#list>
}
EOF

# Create Gradle Wrapper
cat > gradlew << 'EOF'
#!/bin/sh
GRADLE_VERSION=8.5
exec gradle "$@"
EOF
chmod +x gradlew

# Create Docker file
cat > Dockerfile << 'EOF'
FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EOF

# Create README
cat > README.md << 'EOF'
# GigaPress Backend Service

## Overview
Backend Service for GigaPress project running on port 8084.

## Features
- API endpoint generation
- Business logic implementation
- JWT-based authentication
- Service integration with other microservices
- Kafka event handling

## API Endpoints
- POST /api/generation/generate - Generate API endpoints
- GET /api/generation/health - Health check

## Running the Service
```bash
./gradlew bootRun
```

## Building
```bash
./gradlew build
```

## Dependencies
- Domain/Schema Service (port 8083)
- MCP Server (port 8082)
- Dynamic Update Engine (port 8081)
- Kafka (port 9092)
- Redis (port 6379)
EOF

echo "✅ Backend Service structure created successfully!"
echo ""
echo "📁 Directory structure:"
find . -type f -name "*.java" -o -name "*.properties" -o -name "build.gradle" | head -20
echo ""
echo "Next steps:"
echo "1. cd services/backend-service"
echo "2. ./gradlew build"
echo "3. ./gradlew bootRun"
echo ""
echo "The service will be available at http://localhost:8084"
echo "Swagger UI: http://localhost:8084/swagger-ui.html"