package com.gigapress.mcp.service;

import com.gigapress.mcp.client.DynamicUpdateEngineClient;
import com.gigapress.mcp.event.EventProducer;
import com.gigapress.mcp.model.event.ProjectEvent;
import com.gigapress.mcp.model.request.ComponentUpdateRequest;
import com.gigapress.mcp.model.response.ComponentUpdateResponse;
import com.gigapress.mcp.model.response.ComponentUpdateResponse.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComponentUpdateService {
    
    private final DynamicUpdateEngineClient dynamicUpdateEngineClient;
    private final EventProducer eventProducer;
    
    public Mono<ComponentUpdateResponse> updateComponents(ComponentUpdateRequest request) {
        log.info("Updating components for project: {}", request.getProjectId());
        
        String updateId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        
        return executeUpdates(request, updateId)
            .collectList()
            .map(results -> buildResponse(updateId, request, results, startTime))
            .doOnSuccess(response -> publishUpdateEvents(response, request))
            .doOnError(error -> log.error("Error updating components", error));
    }
    
    private Flux<UpdateResult> executeUpdates(ComponentUpdateRequest request, String updateId) {
        switch (request.getUpdateStrategy()) {
            case INCREMENTAL:
                return executeIncremental(request.getUpdates());
            case BATCH:
                return executeBatch(request.getUpdates());
            case PARALLEL:
                return executeParallel(request.getUpdates());
            default:
                return executeIncremental(request.getUpdates());
        }
    }
    
    private Flux<UpdateResult> executeIncremental(List<ComponentUpdateRequest.ComponentUpdate> updates) {
        return Flux.fromIterable(updates)
            .concatMap(this::performSingleUpdate);
    }
    
    private Flux<UpdateResult> executeBatch(List<ComponentUpdateRequest.ComponentUpdate> updates) {
        return Flux.fromIterable(updates)
            .collectList()
            .flatMapMany(batch -> {
                log.info("Executing batch update for {} components", batch.size());
                return Flux.fromIterable(batch)
                    .flatMap(this::performSingleUpdate);
            });
    }
    
    private Flux<UpdateResult> executeParallel(List<ComponentUpdateRequest.ComponentUpdate> updates) {
        return Flux.fromIterable(updates)
            .parallel()
            .runOn(reactor.core.scheduler.Schedulers.parallel())
            .flatMap(this::performSingleUpdate)
            .sequential();
    }
    
    private Mono<UpdateResult> performSingleUpdate(ComponentUpdateRequest.ComponentUpdate update) {
        return Mono.fromCallable(() -> {
            try {
                // Simulate update logic
                Thread.sleep(100); // Simulate processing time
                
                List<String> modifiedFiles = generateModifiedFiles(update);
                List<String> warnings = validateUpdate(update);
                
                return UpdateResult.builder()
                    .componentId(update.getComponentId())
                    .componentName("Component " + update.getComponentId())
                    .status(warnings.isEmpty() ? UpdateStatus.SUCCESS : UpdateStatus.PARTIAL)
                    .modifiedFiles(modifiedFiles)
                    .warnings(warnings)
                    .metadata(Map.of("version", update.getVersion() != null ? update.getVersion() : "1.0.0"))
                    .build();
                    
            } catch (Exception e) {
                return UpdateResult.builder()
                    .componentId(update.getComponentId())
                    .componentName("Component " + update.getComponentId())
                    .status(UpdateStatus.FAILED)
                    .errorMessage(e.getMessage())
                    .modifiedFiles(new ArrayList<>())
                    .warnings(new ArrayList<>())
                    .metadata(new HashMap<>())
                    .build();
            }
        });
    }
    
    private List<String> generateModifiedFiles(ComponentUpdateRequest.ComponentUpdate update) {
        List<String> files = new ArrayList<>();
        
        switch (update.getUpdateType()) {
            case CREATE:
                files.add(update.getComponentId() + "/index.ts");
                files.add(update.getComponentId() + "/component.tsx");
                files.add(update.getComponentId() + "/styles.css");
                break;
            case MODIFY:
                files.add(update.getComponentId() + "/index.ts");
                if (update.getUpdateContent() != null && update.getUpdateContent().containsKey("styles")) {
                    files.add(update.getComponentId() + "/styles.css");
                }
                break;
            case DELETE:
                files.add("DELETED: " + update.getComponentId() + "/*");
                break;
            case RENAME:
                String newName = (String) update.getUpdateContent().get("newName");
                files.add("RENAMED: " + update.getComponentId() + " -> " + newName);
                break;
            case MOVE:
                String newPath = (String) update.getUpdateContent().get("newPath");
                files.add("MOVED: " + update.getComponentId() + " -> " + newPath);
                break;
            case REFACTOR:
                files.add(update.getComponentId() + "/*");
                break;
        }
        
        return files;
    }
    
    private List<String> validateUpdate(ComponentUpdateRequest.ComponentUpdate update) {
        List<String> warnings = new ArrayList<>();
        
        // Validate dependencies
        if (update.getDependencies() != null && update.getDependencies().size() > 5) {
            warnings.add("Component has many dependencies (" + update.getDependencies().size() + ")");
        }
        
        // Check for breaking changes
        if (update.getUpdateType() == ComponentUpdateRequest.UpdateType.DELETE ||
            update.getUpdateType() == ComponentUpdateRequest.UpdateType.RENAME) {
            warnings.add("This is a breaking change that may affect dependent components");
        }
        
        // Version compatibility
        if (update.getVersion() != null && update.getVersion().startsWith("0.")) {
            warnings.add("Component is using pre-release version");
        }
        
        return warnings;
    }
    
    private ComponentUpdateResponse buildResponse(
            String updateId,
            ComponentUpdateRequest request,
            List<UpdateResult> results,
            long startTime) {
        
        int successCount = (int) results.stream()
            .filter(r -> r.getStatus() == UpdateStatus.SUCCESS)
            .count();
        
        int failedCount = (int) results.stream()
            .filter(r -> r.getStatus() == UpdateStatus.FAILED)
            .count();
        
        int warningCount = results.stream()
            .mapToInt(r -> r.getWarnings().size())
            .sum();
        
        UpdateStatus overallStatus;
        if (failedCount == 0) {
            overallStatus = warningCount > 0 ? UpdateStatus.PARTIAL : UpdateStatus.SUCCESS;
        } else if (successCount == 0) {
            overallStatus = UpdateStatus.FAILED;
        } else {
            overallStatus = UpdateStatus.PARTIAL;
        }
        
        UpdateSummary summary = UpdateSummary.builder()
            .totalComponents(results.size())
            .successfulUpdates(successCount)
            .failedUpdates(failedCount)
            .warningCount(warningCount)
            .totalDurationMs(System.currentTimeMillis() - startTime)
            .build();
        
        return ComponentUpdateResponse.builder()
            .updateId(updateId)
            .projectId(request.getProjectId())
            .overallStatus(overallStatus)
            .updateResults(results)
            .rollbackAvailable(request.isRollbackOnError() && failedCount > 0)
            .updateSummary(summary)
            .build();
    }
    
    private void publishUpdateEvents(ComponentUpdateResponse response, ComponentUpdateRequest request) {
        response.getUpdateResults().forEach(result -> {
            ProjectEvent event = ProjectEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(ProjectEvent.EventType.COMPONENT_UPDATED)
                .projectId(request.getProjectId())
                .componentId(result.getComponentId())
                .sourceService("MCP Server")
                .payload(Map.of(
                    "updateStatus", result.getStatus().toString(),
                    "modifiedFiles", result.getModifiedFiles(),
                    "warnings", result.getWarnings()
                ))
                .correlationId(response.getUpdateId())
                .build();
            
            eventProducer.publishProjectEvent(event);
        });
    }
}
