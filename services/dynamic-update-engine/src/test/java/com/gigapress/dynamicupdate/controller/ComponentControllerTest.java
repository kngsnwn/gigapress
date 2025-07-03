package com.gigapress.dynamicupdate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigapress.dynamicupdate.config.TestConfig;
import com.gigapress.dynamicupdate.domain.Component;
import com.gigapress.dynamicupdate.domain.ComponentStatus;
import com.gigapress.dynamicupdate.domain.ComponentType;
import com.gigapress.dynamicupdate.dto.ComponentRequest;
import com.gigapress.dynamicupdate.exception.GlobalExceptionHandler;
import com.gigapress.dynamicupdate.service.ComponentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ComponentController.class)
@ContextConfiguration(classes = {
    ComponentController.class,
    GlobalExceptionHandler.class,
    TestConfig.class
})
@Import(JacksonAutoConfiguration.class)
class ComponentControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private ComponentService componentService;
    
    @Test
    void shouldCreateComponent() throws Exception {
        // Given
        ComponentRequest request = new ComponentRequest(
                "comp-123",
                "Test Component",
                ComponentType.BACKEND,
                "1.0.0",
                "proj-123",
                null
        );
        
        Component component = Component.builder()
                .componentId(request.getComponentId())
                .name(request.getName())
                .type(request.getType())
                .version(request.getVersion())
                .projectId(request.getProjectId())
                .status(ComponentStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        when(componentService.createComponent(any(Component.class))).thenReturn(component);
        
        // When & Then
        mockMvc.perform(post("/api/components")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.componentId").value("comp-123"))
                .andExpect(jsonPath("$.name").value("Test Component"));
    }
    
    @Test
    void shouldGetComponent() throws Exception {
        // Given
        Component component = Component.builder()
                .componentId("comp-123")
                .name("Test Component")
                .type(ComponentType.BACKEND)
                .version("1.0.0")
                .projectId("proj-123")
                .status(ComponentStatus.ACTIVE)
                .build();
        
        when(componentService.findByComponentId("comp-123")).thenReturn(Optional.of(component));
        
        // When & Then
        mockMvc.perform(get("/api/components/comp-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.componentId").value("comp-123"))
                .andExpect(jsonPath("$.name").value("Test Component"));
    }
    
    @Test
    void shouldReturn404WhenComponentNotFound() throws Exception {
        // Given
        when(componentService.findByComponentId("comp-999")).thenReturn(Optional.empty());
        
        // When & Then
        mockMvc.perform(get("/api/components/comp-999"))
                .andExpect(status().isNotFound());
    }
}
