package ${packageName}.service;

import ${packageName}.dto.${entityName}SearchCriteria;
import ${packageName}.dto.${entityName}SearchResult;
import ${packageName}.entity.${entityName};
import ${packageName}.repository.${entityName}Repository;
import ${packageName}.specification.${entityName}Specification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ${entityName}SearchService {

    private final ${entityName}Repository repository;
    private final ${entityName}Specification specificationBuilder;

    public ${entityName}SearchResult search(${entityName}SearchCriteria criteria) {
        log.info("Searching ${entityName}s with criteria: {}", criteria);
        
        // Build specification
        Specification<${entityName}> spec = specificationBuilder.build(criteria);
        
        // Build pageable
        Pageable pageable = buildPageable(criteria);
        
        // Execute search
        Page<${entityName}> page = repository.findAll(spec, pageable);
        
        // Build result
        return ${entityName}SearchResult.builder()
                .content(page.getContent())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
    
    private Pageable buildPageable(${entityName}SearchCriteria criteria) {
        List<Sort.Order> orders = new ArrayList<>();
        
        if (criteria.getSortBy() != null && !criteria.getSortBy().isEmpty()) {
            for (String sortField : criteria.getSortBy()) {
                Sort.Direction direction = criteria.isDescending() ? 
                    Sort.Direction.DESC : Sort.Direction.ASC;
                orders.add(new Sort.Order(direction, sortField));
            }
        } else {
            orders.add(Sort.Order.desc("createdAt"));
        }
        
        return PageRequest.of(
            criteria.getPage(), 
            criteria.getSize(), 
            Sort.by(orders)
        );
    }
}
