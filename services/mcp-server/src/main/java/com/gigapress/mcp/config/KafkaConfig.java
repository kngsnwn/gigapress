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
