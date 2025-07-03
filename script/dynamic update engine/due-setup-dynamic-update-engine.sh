#!/bin/bash

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}🚀 Setting up Dynamic Update Engine Gradle Project${NC}"
echo "=================================================="

# Navigate to service directory
cd services/dynamic-update-engine

# Create build.gradle
echo -e "\n${YELLOW}📝 Creating build.gradle...${NC}"
cat > build.gradle << 'EOF'
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.4'
}

group = 'com.gigapress'
version = '0.0.1-SNAPSHOT'

java {
    sourceCompatibility = '17'
}

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
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    
    // Spring Data
    implementation 'org.springframework.boot:spring-boot-starter-data-neo4j'
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    
    // Kafka
    implementation 'org.springframework.kafka:spring-kafka'
    
    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    // Jackson for JSON processing
    implementation 'com.fasterxml.jackson.core:jackson-databind'
    implementation 'com.fasterxml.jackson.datatype:jackson-datatype-jsr310'
    
    // Validation
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    
    // Development tools
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
    
    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.kafka:spring-kafka-test'
    testImplementation 'org.testcontainers:testcontainers'
    testImplementation 'org.testcontainers:neo4j'
    testImplementation 'org.testcontainers:kafka'
    testImplementation 'org.testcontainers:junit-jupiter'
}

tasks.named('test') {
    useJUnitPlatform()
}

// Gradle Wrapper 설정
wrapper {
    gradleVersion = '8.5'
    distributionType = Wrapper.DistributionType.ALL
}
EOF

# Create settings.gradle
echo -e "\n${YELLOW}📝 Creating settings.gradle...${NC}"
cat > settings.gradle << 'EOF'
rootProject.name = 'dynamic-update-engine'
EOF

# Create directory structure
echo -e "\n${YELLOW}📁 Creating directory structure...${NC}"
mkdir -p src/main/java/com/gigapress/dynamicupdate/{config,domain,repository,service,controller,event,dto,exception}
mkdir -p src/main/resources
mkdir -p src/test/java/com/gigapress/dynamicupdate
mkdir -p src/test/resources

# Create application.properties
echo -e "\n${YELLOW}📝 Creating application.properties...${NC}"
cat > src/main/resources/application.properties << 'EOF'
# Server Configuration
server.port=8081
spring.application.name=dynamic-update-engine

# Neo4j Configuration
spring.neo4j.uri=bolt://localhost:7687
spring.neo4j.authentication.username=neo4j
spring.neo4j.authentication.password=password123

# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=gigapress-update-engine
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
spring.redis.timeout=60000ms
spring.redis.lettuce.pool.max-active=8
spring.redis.lettuce.pool.max-idle=8
spring.redis.lettuce.pool.min-idle=0

# Logging
logging.level.com.gigapress=DEBUG
logging.level.org.springframework.kafka=INFO
logging.level.org.springframework.data.neo4j=DEBUG

# Actuator endpoints
management.endpoints.web.exposure.include=health,info,metrics,kafka
management.endpoint.health.show-details=always

# Jackson configuration
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.deserialization.fail-on-unknown-properties=false
EOF

# Create Main Application class
echo -e "\n${YELLOW}📝 Creating main application class...${NC}"
cat > src/main/java/com/gigapress/dynamicupdate/DynamicUpdateEngineApplication.java << 'EOF'
package com.gigapress.dynamicupdate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableNeo4jRepositories
@EnableKafka
@EnableCaching
public class DynamicUpdateEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(DynamicUpdateEngineApplication.class, args);
    }
}
EOF

# Create .gitignore
echo -e "\n${YELLOW}📝 Creating .gitignore...${NC}"
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

# Spring Boot
target/
!.mvn/wrapper/maven-wrapper.jar
!**/src/main/**/target/
!**/src/test/**/target/
EOF

# Create README
echo -e "\n${YELLOW}📝 Creating README.md...${NC}"
cat > README.md << 'EOF'
# Dynamic Update Engine

## Overview
The Dynamic Update Engine is responsible for managing component dependencies and propagating changes throughout the GigaPress system.

## Features
- Component dependency graph management (Neo4j)
- Event-driven update propagation (Kafka)
- Change impact analysis
- Caching layer (Redis)

## Tech Stack
- Java 17
- Spring Boot 3.2.0
- Spring Data Neo4j
- Spring Kafka
- Spring Data Redis
- Gradle 8.5

## Setup

### Prerequisites
- Java 17+
- Docker & Docker Compose (for infrastructure)
- Infrastructure services running (Neo4j, Kafka, Redis)

### Build
```bash
./gradlew build
```

### Run
```bash
./gradlew bootRun
```

## API Endpoints
- Health check: `GET http://localhost:8081/actuator/health`
- Component dependencies: `GET http://localhost:8081/api/dependencies/{componentId}`
- Update propagation: `POST http://localhost:8081/api/updates`

## Configuration
See `src/main/resources/application.properties` for configuration options.
EOF

echo -e "\n${GREEN}✅ Gradle project structure created successfully!${NC}"
echo -e "\n${BLUE}📋 Next steps:${NC}"
echo "1. Generate Gradle wrapper:"
echo "   ${YELLOW}gradle wrapper --gradle-version 8.5 --distribution-type all${NC}"
echo "2. Build the project:"
echo "   ${YELLOW}./gradlew build${NC}"
echo "3. Run the application:"
echo "   ${YELLOW}./gradlew bootRun${NC}"