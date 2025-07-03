package ${packageName}.service;

import ${packageName}.dto.${entityName}Request;
import ${packageName}.dto.${entityName}Response;
import ${packageName}.repository.${entityName}Repository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ${entityName}Service {

    private final ${entityName}Repository ${entityName?uncap_first}Repository;

    public List<${entityName}Response> findAll() {
        log.info("Finding all ${entityName}s");
        // Implementation here
        return List.of();
    }

    public ${entityName}Response findById(Long id) {
        log.info("Finding ${entityName} by id: {}", id);
        // Implementation here
        return new ${entityName}Response();
    }

    public ${entityName}Response create(${entityName}Request request) {
        log.info("Creating new ${entityName}");
        // Implementation here
        return new ${entityName}Response();
    }

    public ${entityName}Response update(Long id, ${entityName}Request request) {
        log.info("Updating ${entityName} with id: {}", id);
        // Implementation here
        return new ${entityName}Response();
    }

    public void delete(Long id) {
        log.info("Deleting ${entityName} with id: {}", id);
        // Implementation here
    }
}
