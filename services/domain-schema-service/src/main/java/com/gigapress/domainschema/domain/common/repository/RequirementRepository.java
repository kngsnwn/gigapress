package com.gigapress.domainschema.domain.common.repository;

import com.gigapress.domainschema.domain.common.entity.Requirement;
import com.gigapress.domainschema.domain.common.entity.RequirementStatus;
import com.gigapress.domainschema.domain.common.entity.RequirementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequirementRepository extends JpaRepository<Requirement, Long> {
    
    List<Requirement> findByProjectProjectId(String projectId);
    
    List<Requirement> findByProjectProjectIdAndStatus(String projectId, RequirementStatus status);
    
    List<Requirement> findByProjectProjectIdAndType(String projectId, RequirementType type);
    
    @Query("SELECT r FROM Requirement r WHERE r.project.projectId = :projectId ORDER BY r.priority.order ASC")
    List<Requirement> findByProjectIdOrderByPriority(@Param("projectId") String projectId);
    
    long countByProjectProjectId(String projectId);
}
