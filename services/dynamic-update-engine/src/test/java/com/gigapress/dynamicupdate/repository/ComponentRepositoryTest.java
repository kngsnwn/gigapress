package com.gigapress.dynamicupdate.repository;

import com.gigapress.dynamicupdate.domain.Component;
import com.gigapress.dynamicupdate.domain.ComponentStatus;
import com.gigapress.dynamicupdate.domain.ComponentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataNeo4jTest
@ActiveProfiles("test")
@EnabledIfSystemProperty(named = "test.neo4j.enabled", matches = "true")
class ComponentRepositoryTest {
    
    @Autowired
    private ComponentRepository componentRepository;
    
    @BeforeEach
    void setUp() {
        componentRepository.deleteAll();
    }
    
    @Test
    void shouldSaveAndFindComponent() {
        // Given
        Component component = Component.builder()
                .componentId("comp-123")
                .name("Test Component")
                .type(ComponentType.BACKEND)
                .version("1.0.0")
                .projectId("proj-123")
                .status(ComponentStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // When
        Component saved = componentRepository.save(component);
        Optional<Component> found = componentRepository.findByComponentId("comp-123");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Component");
        assertThat(found.get().getType()).isEqualTo(ComponentType.BACKEND);
    }
    
    @Test
    void shouldFindDependents() {
        // Given
        Component comp1 = createComponent("comp-1", "Component 1");
        Component comp2 = createComponent("comp-2", "Component 2");
        componentRepository.save(comp1);
        componentRepository.save(comp2);
        
        // Create dependency: comp2 depends on comp1
        comp2.addDependency(comp1, com.gigapress.dynamicupdate.domain.DependencyType.COMPILE);
        componentRepository.save(comp2);
        
        // When
        Set<Component> dependents = componentRepository.findDependents("comp-1");
        
        // Then
        assertThat(dependents).hasSize(1);
        assertThat(dependents.iterator().next().getComponentId()).isEqualTo("comp-2");
    }
    
    private Component createComponent(String id, String name) {
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
