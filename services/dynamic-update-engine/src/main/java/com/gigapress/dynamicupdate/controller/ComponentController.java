package com.gigapress.dynamicupdate.controller;

import com.gigapress.dynamicupdate.domain.Component;
import com.gigapress.dynamicupdate.domain.ComponentType;
import com.gigapress.dynamicupdate.domain.DependencyType;
import com.gigapress.dynamicupdate.dto.ComponentRequest;
import com.gigapress.dynamicupdate.dto.DependencyRequest;
import com.gigapress.dynamicupdate.dto.UpdateRequest;
import com.gigapress.dynamicupdate.service.ComponentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/components")
@RequiredArgsConstructor
public class ComponentController {
    
    private final ComponentService componentService;
    
    @PostMapping
    public ResponseEntity<Component> createComponent(@Valid @RequestBody ComponentRequest request) {
        log.info("Creating component: {}", request.getName());
        
        Component component = Component.builder()
                .componentId(request.getComponentId())
                .name(request.getName())
                .type(request.getType())
                .version(request.getVersion())
                .projectId(request.getProjectId())
                .metadata(request.getMetadata())
                .build();
        
        Component created = componentService.createComponent(component);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping("/{componentId}")
    public ResponseEntity<Component> getComponent(@PathVariable String componentId) {
        return componentService.findByComponentId(componentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{componentId}")
    public ResponseEntity<Component> updateComponent(
            @PathVariable String componentId,
            @Valid @RequestBody UpdateRequest request) {
        
        log.info("Updating component: {}", componentId);
        Component updated = componentService.updateComponent(componentId, request.getUpdates());
        return ResponseEntity.ok(updated);
    }
    
    @PostMapping("/{componentId}/dependencies")
    public ResponseEntity<Void> addDependency(
            @PathVariable String componentId,
            @Valid @RequestBody DependencyRequest request) {
        
        log.info("Adding dependency: {} -> {}", componentId, request.getTargetComponentId());
        componentService.addDependency(
                componentId, 
                request.getTargetComponentId(), 
                request.getType()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @GetMapping("/{componentId}/dependencies")
    public ResponseEntity<Set<Component>> getDependencies(@PathVariable String componentId) {
        Set<Component> dependencies = componentService.getDirectDependencies(componentId);
        return ResponseEntity.ok(dependencies);
    }
    
    @GetMapping("/{componentId}/dependents")
    public ResponseEntity<Set<Component>> getDependents(@PathVariable String componentId) {
        Set<Component> dependents = componentService.getDirectDependents(componentId);
        return ResponseEntity.ok(dependents);
    }
    
    @GetMapping("/{componentId}/impact-analysis")
    public ResponseEntity<Set<Component>> getImpactAnalysis(@PathVariable String componentId) {
        Set<Component> affected = componentService.getAllAffectedComponents(componentId);
        return ResponseEntity.ok(affected);
    }
    
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Component>> getProjectComponents(@PathVariable String projectId) {
        List<Component> components = componentService.findByProjectId(projectId);
        return ResponseEntity.ok(components);
    }
}
