package com.gigapress.domainschema.domain.common.repository;

import com.gigapress.domainschema.domain.common.entity.SchemaDesign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchemaDesignRepository extends JpaRepository<SchemaDesign, Long> {
    
    Optional<SchemaDesign> findByProjectProjectId(String projectId);
    
    @Query("SELECT sd FROM SchemaDesign sd LEFT JOIN FETCH sd.tables WHERE sd.project.projectId = :projectId")
    Optional<SchemaDesign> findByProjectIdWithTables(@Param("projectId") String projectId);
    
    boolean existsByProjectProjectId(String projectId);
}
