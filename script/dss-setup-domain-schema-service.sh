#!/bin/bash

# Domain/Schema Service Setup Script
# This script creates the complete project structure with Gradle

set -e

echo "🚀 Creating Domain/Schema Service..."

# Create project directory
PROJECT_NAME="domain-schema-service"
mkdir -p $PROJECT_NAME
cd $PROJECT_NAME

# Create directory structure
echo "📁 Creating project structure..."
mkdir -p src/main/java/com/gigapress/domainschema/{config,domain/{analysis/{controller,service,model,repository},common/{entity,event,exception}},schema/{design/{controller,service,model,repository},mapping/{service,model}},integration/{kafka/{producer,consumer},mcp/client}}
mkdir -p src/main/resources/db/migration
mkdir -p src/test/java/com/gigapress/domainschema

# Create build.gradle
echo "📦 Creating build.gradle..."
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
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    
    // Kafka
    implementation 'org.springframework.kafka:spring-kafka'
    
    // Database
    runtimeOnly 'org.postgresql:postgresql'
    testImplementation 'com.h2database:h2'
    
    // Flyway
    implementation 'org.flywaydb:flyway-core'
    
    // OpenAPI Documentation
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'
    
    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    // MapStruct
    implementation 'org.mapstruct:mapstruct:1.5.5.Final'
    annotationProcessor 'org.mapstruct:mapstruct-processor:1.5.5.Final'
    
    // Jackson
    implementation 'com.fasterxml.jackson.datatype:jackson-datatype-jsr310'
    
    // Test Dependencies
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.kafka:spring-kafka-test'
}

tasks.named('test') {
    useJUnitPlatform()
}

// Add MapStruct processor configuration
compileJava {
    options.annotationProcessorPath = configurations.annotationProcessor
}
EOF

# Create gradle wrapper
echo "🔧 Creating Gradle wrapper..."
cat > settings.gradle << 'EOF'
rootProject.name = 'domain-schema-service'
EOF

# Create application.properties
echo "⚙️ Creating application.properties..."
cat > src/main/resources/application.properties << 'EOF'
# Server Configuration
server.port=8083
spring.application.name=domain-schema-service

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/gigapress_domain
spring.datasource.username=gigapress
spring.datasource.password=gigapress123
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.default_schema=domain_schema

# Flyway Configuration
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration

# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=domain-schema-service
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=*
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer

# Redis Configuration
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=redis123
spring.cache.type=redis
spring.cache.redis.time-to-live=3600000

# Actuator Configuration
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=always

# OpenAPI Configuration
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui
springdoc.swagger-ui.operations-sorter=method

# Logging Configuration
logging.level.com.gigapress.domainschema=DEBUG
logging.level.org.springframework.kafka=INFO
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# MCP Server Integration
mcp.server.url=http://localhost:8082
mcp.server.timeout=30000

# Dynamic Update Engine Integration
update.engine.url=http://localhost:8081
update.engine.timeout=30000
EOF

# Create Main Application Class
echo "🚀 Creating main application class..."
cat > src/main/java/com/gigapress/domainschema/DomainSchemaServiceApplication.java << 'EOF'
package com.gigapress.domainschema;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableKafka
@EnableCaching
@EnableAsync
@EnableScheduling
public class DomainSchemaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DomainSchemaServiceApplication.class, args);
    }
}
EOF

# Create OpenAPI Configuration
echo "📄 Creating OpenAPI configuration..."
cat > src/main/java/com/gigapress/domainschema/config/OpenApiConfig.java << 'EOF'
package com.gigapress.domainschema.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Domain/Schema Service API")
                        .description("AI-powered domain model and schema generation service")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("GigaPress Team")
                                .email("team@gigapress.ai")));
    }
}
EOF

# Create Kafka Configuration
echo "📡 Creating Kafka configuration..."
cat > src/main/java/com/gigapress/domainschema/config/KafkaConfig.java << 'EOF'
package com.gigapress.domainschema.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    
    @Bean
    public NewTopic domainAnalyzedTopic() {
        return TopicBuilder.name("domain-analyzed")
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic schemaGeneratedTopic() {
        return TopicBuilder.name("schema-generated")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
EOF

# Create Redis Configuration
echo "💾 Creating Redis configuration..."
cat > src/main/java/com/gigapress/domainschema/config/RedisConfig.java << 'EOF'
package com.gigapress.domainschema.config;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .disableCachingNullValues();
                
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
EOF

# Create JPA Configuration
echo "🗄️ Creating JPA configuration..."
cat > src/main/java/com/gigapress/domainschema/config/JpaConfig.java << 'EOF'
package com.gigapress.domainschema.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.gigapress.domainschema")
@EnableJpaAuditing
@EnableTransactionManagement
public class JpaConfig {
}
EOF

# Create Health Check Controller
echo "❤️ Creating health check endpoint..."
cat > src/main/java/com/gigapress/domainschema/domain/common/HealthController.java << 'EOF'
package com.gigapress.domainschema.domain.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class HealthController {
    
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
            "status", "UP",
            "service", "domain-schema-service",
            "version", "1.0.0"
        );
    }
}
EOF

# Create Dockerfile
echo "🐳 Creating Dockerfile..."
cat > Dockerfile << 'EOF'
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle clean build -x test

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/build/libs/domain-schema-service-1.0.0.jar app.jar

# Add wait-for-it script for service dependencies
RUN apt-get update && apt-get install -y wget
RUN wget https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh
RUN chmod +x wait-for-it.sh

EXPOSE 8083

ENTRYPOINT ["./wait-for-it.sh", "kafka:9092", "--", "./wait-for-it.sh", "redis:6379", "--", "java", "-jar", "app.jar"]
EOF

# Create .gitignore
echo "📝 Creating .gitignore..."
cat > .gitignore << 'EOF'
# Gradle
.gradle/
build/
!gradle/wrapper/gradle-wrapper.jar
!**/src/main/**/build/
!**/src/test/**/build/

# IDE
.idea/
*.iws
*.iml
*.ipr
out/
!**/src/main/**/out/
!**/src/test/**/out/
.vscode/
*.swp
*.swo

# OS
.DS_Store
Thumbs.db

# Logs
logs/
*.log

# Package Files
*.jar
*.war
*.nar
*.ear
*.zip
*.tar.gz
*.rar

# Environment
.env
.env.local
EOF

# Create README.md
echo "📚 Creating README.md..."
cat > README.md << 'EOF'
# Domain/Schema Service

## Overview
Domain/Schema Service는 GigaPress 시스템의 핵심 서비스로, 자연어 요구사항을 분석하여 도메인 모델과 데이터베이스 스키마를 자동으로 생성합니다.

## 주요 기능
- **Requirements Analysis**: 자연어 요구사항 분석
- **Domain Model Generation**: DDD 기반 도메인 모델 생성
- **Database Schema Design**: 최적화된 DB 스키마 설계
- **Entity Relationship Mapping**: JPA 엔티티 및 관계 매핑

## 기술 스택
- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- PostgreSQL
- Apache Kafka
- Redis
- Flyway (DB Migration)

## 실행 방법

### Prerequisites
- Java 17+
- Docker & Docker Compose (인프라 서비스용)

### 로컬 실행
```bash
# PostgreSQL 데이터베이스 생성
createdb gigapress_domain

# 애플리케이션 실행
./gradlew bootRun
```

### Docker로 실행
```bash
# 이미지 빌드
docker build -t gigapress/domain-schema-service:latest .

# 컨테이너 실행
docker run -d \
  --name domain-schema-service \
  --network gigapress-network \
  -p 8083:8083 \
  -e SPRING_PROFILES_ACTIVE=dev \
  gigapress/domain-schema-service:latest
```

## API Documentation
- Swagger UI: http://localhost:8083/swagger-ui
- OpenAPI Spec: http://localhost:8083/api-docs
EOF

# Initialize Gradle wrapper
echo "🔨 Initializing Gradle wrapper..."
gradle wrapper --gradle-version 8.5

# Create initial migration
echo "🗄️ Creating initial database migration..."
cat > src/main/resources/db/migration/V1__init_schema.sql << 'EOF'
-- Create schema
CREATE SCHEMA IF NOT EXISTS domain_schema;

-- Create sequences
CREATE SEQUENCE IF NOT EXISTS domain_schema.project_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS domain_schema.requirement_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS domain_schema.domain_model_seq START WITH 1 INCREMENT BY 50;

-- Create base tables
CREATE TABLE IF NOT EXISTS domain_schema.projects (
    id BIGINT PRIMARY KEY,
    project_id VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    project_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_projects_project_id ON domain_schema.projects(project_id);
CREATE INDEX idx_projects_status ON domain_schema.projects(status);
EOF

echo "✅ Domain/Schema Service project created successfully!"
echo ""
echo "📋 Next steps:"
echo "1. Create PostgreSQL database: createdb gigapress_domain"
echo "2. Run the service: ./gradlew bootRun"
echo "3. Access Swagger UI: http://localhost:8083/swagger-ui"
echo ""
echo "🔗 Service will be available at: http://localhost:8083"