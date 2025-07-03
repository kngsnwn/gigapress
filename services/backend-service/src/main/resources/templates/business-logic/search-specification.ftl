package ${packageName}.specification;

import ${packageName}.dto.${entityName}SearchCriteria;
import ${packageName}.entity.${entityName};
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ${entityName}Specification {

    public Specification<${entityName}> build(${entityName}SearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

<#list fields as field>
            // Search by ${field.name}
            if (criteria.get${field.name?cap_first}() != null) {
    <#if field.type == "String">
                if (criteria.isExactMatch()) {
                    predicates.add(criteriaBuilder.equal(
                        root.get("${field.name}"), criteria.get${field.name?cap_first}()));
                } else {
                    predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("${field.name}")), 
                        "%" + criteria.get${field.name?cap_first}().toLowerCase() + "%"));
                }
    <#else>
                predicates.add(criteriaBuilder.equal(
                    root.get("${field.name}"), criteria.get${field.name?cap_first}()));
    </#if>
            }

</#list>
            // Date range filters
            if (criteria.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("createdAt"), criteria.getStartDate()));
            }
            
            if (criteria.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("createdAt"), criteria.getEndDate()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
