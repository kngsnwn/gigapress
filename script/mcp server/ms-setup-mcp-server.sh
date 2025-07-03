#!/bin/bash

# MCP Server Setup Script
# This script creates the complete MCP Server project structure

echo "🚀 Setting up MCP Server for GigaPress project..."

# Create base directory
BASE_DIR="services/mcp-server"
mkdir -p $BASE_DIR

# Create directory structure
echo "📁 Creating directory structure..."
mkdir -p $BASE_DIR/src/main/java/com/gigapress/mcp/{config,controller,service,client,model,event}
mkdir -p $BASE_DIR/src/main/resources
mkdir -p $BASE_DIR/src/test/java/com/gigapress/mcp
mkdir -p $BASE_DIR/gradle/wrapper

# Create build.gradle
echo "📝 Creating build.gradle..."
cat > $BASE_DIR/build.gradle << 'EOF'
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.4'
}

group = 'com.gigapress'
version = '0.0.1-SNAPSHOT'
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
    // Spring Boot
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    
    // Kafka
    implementation 'org.springframework.kafka:spring-kafka'
    
    // Redis
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    implementation 'org.apache.commons:commons-pool2'
    
    // WebClient
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    
    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    // JSON Processing
    implementation 'com.fasterxml.jackson.core:jackson-databind'
    implementation 'com.fasterxml.jackson.datatype:jackson-datatype-jsr310'
    
    // Apache Commons
    implementation 'org.apache.commons:commons-lang3:3.12.0'
    
    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.kafka:spring-kafka-test'
    testImplementation 'io.projectreactor:reactor-test'
}

tasks.named('test') {
    useJUnitPlatform()
}

springBoot {
    buildInfo()
}
EOF

# Create settings.gradle
echo "📝 Creating settings.gradle..."
cat > $BASE_DIR/settings.gradle << 'EOF'
rootProject.name = 'mcp-server'
EOF

# Create gradle.properties
echo "📝 Creating gradle.properties..."
cat > $BASE_DIR/gradle.properties << 'EOF'
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.caching=true
EOF

# Create application.properties
echo "📝 Creating application.properties..."
cat > $BASE_DIR/src/main/resources/application.properties << 'EOF'
# Server Configuration
server.port=8082
server.servlet.context-path=/
spring.application.name=mcp-server

# Actuator Configuration
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=always

# Redis Configuration
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=redis123
spring.data.redis.timeout=60000ms
spring.data.redis.lettuce.pool.max-active=8
spring.data.redis.lettuce.pool.max-idle=8
spring.data.redis.lettuce.pool.min-idle=0

# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=mcp-server-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=*
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer

# Dynamic Update Engine Client Configuration
dynamic-update-engine.base-url=http://localhost:8081
dynamic-update-engine.connect-timeout=5000
dynamic-update-engine.read-timeout=30000

# Logging Configuration
logging.level.root=INFO
logging.level.com.gigapress.mcp=DEBUG
logging.level.org.springframework.kafka=INFO
logging.level.org.springframework.data.redis=INFO
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n

# Jackson Configuration
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.default-property-inclusion=non_null
EOF

# Create logback-spring.xml
echo "📝 Creating logback-spring.xml..."
cat > $BASE_DIR/src/main/resources/logback-spring.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    
    <property name="LOG_FILE" value="${LOG_FILE:-${LOG_PATH:-${LOG_TEMP:-${java.io.tmpdir:-/tmp}}}/spring.log}"/>
    
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${CONSOLE_LOG_PATTERN}</pattern>
            <charset>utf8</charset>
        </encoder>
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <encoder>
            <pattern>${FILE_LOG_PATTERN}</pattern>
            <charset>utf8</charset>
        </encoder>
        <file>${LOG_FILE}</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_FILE}.%d{yyyy-MM-dd}.gz</fileNamePattern>
            <maxHistory>7</maxHistory>
        </rollingPolicy>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
EOF

# Create McpServerApplication.java
echo "📝 Creating McpServerApplication.java..."
cat > $BASE_DIR/src/main/java/com/gigapress/mcp/McpServerApplication.java << 'EOF'
package com.gigapress.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class McpServerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }
}
EOF

# Create KafkaConfig.java
echo "📝 Creating KafkaConfig.java..."
cat > $BASE_DIR/src/main/java/com/gigapress/mcp/config/KafkaConfig.java << 'EOF'
package com.gigapress.mcp.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }
    
    @Bean
    public NewTopic projectGenerationTopic() {
        return TopicBuilder.name("project-generation")
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic componentUpdateTopic() {
        return TopicBuilder.name("component-update")
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic changeAnalysisTopic() {
        return TopicBuilder.name("change-analysis")
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic validationResultTopic() {
        return TopicBuilder.name("validation-result")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
EOF

# Create RedisConfig.java
echo "📝 Creating RedisConfig.java..."
cat > $BASE_DIR/src/main/java/com/gigapress/mcp/config/RedisConfig.java << 'EOF'
package com.gigapress.mcp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Use JSON serializer for values
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        GenericJackson2JsonRedisSerializer jsonSerializer = 
            new GenericJackson2JsonRedisSerializer(objectMapper);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        return template;
    }
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(60))
            .disableCachingNullValues()
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer()
                )
            );
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
}
EOF

# Create WebClientConfig.java
echo "📝 Creating WebClientConfig.java..."
cat > $BASE_DIR/src/main/java/com/gigapress/mcp/config/WebClientConfig.java << 'EOF'
package com.gigapress.mcp.config;

import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {
    
    @Value("${dynamic-update-engine.base-url}")
    private String dynamicUpdateEngineBaseUrl;
    
    @Value("${dynamic-update-engine.connect-timeout}")
    private int connectTimeout;
    
    @Value("${dynamic-update-engine.read-timeout}")
    private int readTimeout;
    
    @Bean
    public WebClient dynamicUpdateEngineWebClient() {
        HttpClient httpClient = HttpClient.create()
            .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
            .responseTimeout(Duration.ofMillis(readTimeout))
            .doOnConnected(conn -> 
                conn.addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
            );
        
        return WebClient.builder()
            .baseUrl(dynamicUpdateEngineBaseUrl)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }
}
EOF

# Create a simple health check endpoint placeholder
echo "📝 Creating placeholder HealthController.java..."
cat > $BASE_DIR/src/main/java/com/gigapress/mcp/controller/HealthController.java << 'EOF'
package com.gigapress.mcp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class HealthController {
    
    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
            "service", "MCP Server",
            "status", "UP",
            "timestamp", LocalDateTime.now(),
            "message", "MCP Server is ready to handle tool requests"
        );
    }
}
EOF

# Create gradle wrapper files
echo "📝 Creating Gradle wrapper..."
cat > $BASE_DIR/gradlew << 'EOF'
#!/bin/sh
exec ./gradle/wrapper/gradle-wrapper.jar "$@"
EOF
chmod +x $BASE_DIR/gradlew

# Create .gitignore
echo "📝 Creating .gitignore..."
cat > $BASE_DIR/.gitignore << 'EOF'
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
.vscode/
.settings/
.project
.classpath

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

# Project specific
/bin/
/target/
EOF

echo "✅ MCP Server project structure created successfully!"
echo ""
echo "📋 Next steps:"
echo "1. Navigate to the project directory: cd $BASE_DIR"
echo "2. Build the project: ./gradlew build"
echo "3. Run the server: ./gradlew bootRun"
echo ""
echo "🔗 The server will be available at http://localhost:8082"
echo ""
echo "📝 Project structure created:"
echo "- Build configuration (Gradle)"
echo "- Spring Boot application setup"
echo "- Kafka configuration with topics"
echo "- Redis configuration for caching"
echo "- WebClient for Dynamic Update Engine communication"
echo "- Basic health check endpoint"