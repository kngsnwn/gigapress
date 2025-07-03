package ${packageName}.batch;

import ${packageName}.entity.${entityName};
import ${packageName}.service.${entityName}Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class ${entityName}BatchProcessor {

    private final ${entityName}Service service;
    private static final int BATCH_SIZE = 100;

    @Async
    public CompletableFuture<BatchResult> processBatch(List<${entityName}> items) {
        log.info("Starting batch processing for {} items", items.size());
        
        BatchResult result = new BatchResult();
        AtomicInteger processed = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);
        
        items.stream()
            .parallel()
            .forEach(item -> {
                try {
                    processItem(item);
                    processed.incrementAndGet();
                    
                    if (processed.get() % BATCH_SIZE == 0) {
                        log.info("Processed {} items so far", processed.get());
                    }
                } catch (Exception e) {
                    log.error("Failed to process item: {}", item.getId(), e);
                    failed.incrementAndGet();
                    result.addError(item.getId(), e.getMessage());
                }
            });
        
        result.setTotalProcessed(processed.get());
        result.setTotalFailed(failed.get());
        result.setSuccess(failed.get() == 0);
        
        log.info("Batch processing completed. Processed: {}, Failed: {}", 
                processed.get(), failed.get());
        
        return CompletableFuture.completedFuture(result);
    }
    
    private void processItem(${entityName} item) {
        // Process individual item
        // Apply business logic here
<#list businessRules as rule>
        // ${rule.description}
        if (${rule.condition}) {
            ${rule.action}
        }
</#list>
    }
    
    @lombok.Data
    public static class BatchResult {
        private boolean success;
        private int totalProcessed;
        private int totalFailed;
        private java.util.Map<Long, String> errors = new java.util.HashMap<>();
        
        public void addError(Long itemId, String error) {
            errors.put(itemId, error);
        }
    }
}
