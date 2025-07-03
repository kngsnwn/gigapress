package com.gigapress.domainschema.domain.common.repository;

import com.gigapress.domainschema.domain.common.entity.Project;
import com.gigapress.domainschema.domain.common.entity.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    Optional<Project> findByProjectId(String projectId);
    
    boolean existsByProjectId(String projectId);
    
    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);
    
    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.requirements WHERE p.projectId = :projectId")
    Optional<Project> findByProjectIdWithRequirements(@Param("projectId") String projectId);
    
    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = :status")
    long countByStatus(@Param("status") ProjectStatus status);
    
    List<Project> findTop10ByOrderByCreatedAtDesc();
}
