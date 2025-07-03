package com.gigapress.dynamicupdate.controller;

import com.gigapress.dynamicupdate.domain.Component;
import com.gigapress.dynamicupdate.domain.ComponentStatus;
import com.gigapress.dynamicupdate.domain.ComponentType;
import com.gigapress.dynamicupdate.service.ComponentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimpleComponentControllerTest {
    
    @Mock
    private ComponentService componentService;
    
    @InjectMocks
    private ComponentController componentController;
    
    @Test
    void shouldGetComponent() {
        // Given
        String componentId = "comp-123";
        Component component = Component.builder()
                .componentId(componentId)
                .name("Test Component")
                .type(ComponentType.BACKEND)
                .version("1.0.0")
                .projectId("proj-123")
                .status(ComponentStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        when(componentService.findByComponentId(componentId)).thenReturn(Optional.of(component));
        
        // When
        ResponseEntity<Component> response = componentController.getComponent(componentId);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getComponentId()).isEqualTo(componentId);
    }
    
    @Test
    void shouldReturn404WhenComponentNotFound() {
        // Given
        String componentId = "comp-999";
        when(componentService.findByComponentId(componentId)).thenReturn(Optional.empty());
        
        // When
        ResponseEntity<Component> response = componentController.getComponent(componentId);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }
}
