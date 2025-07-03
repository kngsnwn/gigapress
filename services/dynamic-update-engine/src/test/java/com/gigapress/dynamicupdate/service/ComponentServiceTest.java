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
        
        // Set up mock to simulate that comp2 already depends on comp1
        when(componentRepository.findByComponentId("comp-1")).thenReturn(Optional.of(comp1));
        when(componentRepository.findByComponentId("comp-2")).thenReturn(Optional.of(comp2));
        
        // Simulate circular dependency: comp2 -> comp1, and trying to add comp1 -> comp2
        comp2.addDependency(comp1, DependencyType.COMPILE);
        
        // When & Then
        assertThatThrownBy(() -> 
            componentService.addDependency("comp-1", "comp-2", DependencyType.COMPILE)
        ).isInstanceOf(CircularDependencyException.class)
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
