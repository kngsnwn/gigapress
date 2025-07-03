package ${packageName}.service;

import ${packageName}.entity.${entityName};
import ${packageName}.dto.${entityName}CreateDto;
import ${packageName}.dto.${entityName}UpdateDto;
import ${packageName}.dto.${entityName}ResponseDto;
import ${packageName}.repository.${entityName}Repository;
import ${packageName}.mapper.${entityName}Mapper;
import ${packageName}.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ${entityName}Service {

    private final ${entityName}Repository repository;
    private final ${entityName}Mapper mapper;
    
    private static final String CACHE_NAME = "${entityName?lower_case}_cache";

    @Transactional
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public ${entityName}ResponseDto create(@Valid ${entityName}CreateDto createDto) {
        log.info("Creating new ${entityName}");
        
        ${entityName} entity = mapper.toEntity(createDto);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        
        <#list businessRules as rule>
        // Business Rule: ${rule.name}
        // ${rule.description}
        if (${rule.condition}) {
            ${rule.action}
        }
        </#list>
        
        ${entityName} saved = repository.save(entity);
        log.info("Created ${entityName} with id: {}", saved.getId());
        
        return mapper.toResponseDto(saved);
    }

    @Cacheable(value = CACHE_NAME, key = "#id")
    public ${entityName}ResponseDto findById(Long id) {
        log.info("Finding ${entityName} by id: {}", id);
        
        ${entityName} entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("${entityName} not found with id: " + id));
                
        return mapper.toResponseDto(entity);
    }

    public Page<${entityName}ResponseDto> findAll(Pageable pageable) {
        log.info("Finding all ${entityName}s with pagination");
        
        return repository.findAll(pageable)
                .map(mapper::toResponseDto);
    }

    public List<${entityName}ResponseDto> findAll() {
        log.info("Finding all ${entityName}s");
        
        return repository.findAll().stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = CACHE_NAME, key = "#id")
    public ${entityName}ResponseDto update(Long id, @Valid ${entityName}UpdateDto updateDto) {
        log.info("Updating ${entityName} with id: {}", id);
        
        ${entityName} entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("${entityName} not found with id: " + id));
        
        mapper.updateEntityFromDto(updateDto, entity);
        entity.setUpdatedAt(LocalDateTime.now());
        
        <#list businessRules as rule>
        // Business Rule: ${rule.name}
        // ${rule.description}
        if (${rule.condition}) {
            ${rule.action}
        }
        </#list>
        
        ${entityName} updated = repository.save(entity);
        log.info("Updated ${entityName} with id: {}", updated.getId());
        
        return mapper.toResponseDto(updated);
    }

    @Transactional
    @CacheEvict(value = CACHE_NAME, key = "#id")
    public void delete(Long id) {
        log.info("Deleting ${entityName} with id: {}", id);
        
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("${entityName} not found with id: " + id);
        }
        
        repository.deleteById(id);
        log.info("Deleted ${entityName} with id: {}", id);
    }

    @Transactional
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void deleteAll() {
        log.warn("Deleting all ${entityName}s");
        repository.deleteAll();
    }

    public long count() {
        return repository.count();
    }

    public boolean existsById(Long id) {
        return repository.existsById(id);
    }
}
