package com.gigapress.domainschema.domain.common.repository.impl;

import com.gigapress.domainschema.domain.common.entity.Project;
import com.gigapress.domainschema.domain.common.entity.ProjectStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Transactional(readOnly = true)
public class ProjectRepositoryImpl implements ProjectRepositoryCustom {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public Page<Project> findProjectsWithFilters(Map<String, Object> filters, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Project> query = cb.createQuery(Project.class);
        Root<Project> root = query.from(Project.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        // Add filters
        if (filters.containsKey("status")) {
            predicates.add(cb.equal(root.get("status"), filters.get("status")));
        }
        
        if (filters.containsKey("projectType")) {
            predicates.add(cb.equal(root.get("projectType"), filters.get("projectType")));
        }
        
        if (filters.containsKey("searchTerm")) {
            String searchTerm = "%" + filters.get("searchTerm").toString().toLowerCase() + "%";
            predicates.add(cb.or(
                cb.like(cb.lower(root.get("name")), searchTerm),
                cb.like(cb.lower(root.get("description")), searchTerm)
            ));
        }
        
        query.where(predicates.toArray(new Predicate[0]));
        
        // Add sorting
        if (pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            pageable.getSort().forEach(order -> {
                if (order.isAscending()) {
                    orders.add(cb.asc(root.get(order.getProperty())));
                } else {
                    orders.add(cb.desc(root.get(order.getProperty())));
                }
            });
            query.orderBy(orders);
        }
        
        // Execute query with pagination
        TypedQuery<Project> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        
        List<Project> results = typedQuery.getResultList();
        
        // Count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Project> countRoot = countQuery.from(Project.class);
        countQuery.select(cb.count(countRoot));
        countQuery.where(predicates.toArray(new Predicate[0]));
        
        Long total = entityManager.createQuery(countQuery).getSingleResult();
        
        return new PageImpl<>(results, pageable, total);
    }
    
    @Override
    public List<Project> findProjectsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return entityManager.createQuery(
                "SELECT p FROM Project p WHERE p.createdAt BETWEEN :startDate AND :endDate ORDER BY p.createdAt DESC",
                Project.class)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }
    
    @Override
    public Map<ProjectStatus, Long> getProjectStatusStatistics() {
        List<Object[]> results = entityManager.createQuery(
                "SELECT p.status, COUNT(p) FROM Project p GROUP BY p.status",
                Object[].class)
                .getResultList();
        
        Map<ProjectStatus, Long> statistics = new HashMap<>();
        for (Object[] result : results) {
            statistics.put((ProjectStatus) result[0], (Long) result[1]);
        }
        
        return statistics;
    }
    
    @Override
    @Transactional
    public void updateProjectStatusBulk(List<String> projectIds, ProjectStatus newStatus) {
        entityManager.createQuery(
                "UPDATE Project p SET p.status = :status WHERE p.projectId IN :projectIds")
                .setParameter("status", newStatus)
                .setParameter("projectIds", projectIds)
                .executeUpdate();
    }
}
