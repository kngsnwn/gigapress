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
