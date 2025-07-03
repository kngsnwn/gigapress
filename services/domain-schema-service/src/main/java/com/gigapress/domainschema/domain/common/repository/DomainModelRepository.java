package com.gigapress.domainschema.domain.common.repository;

import com.gigapress.domainschema.domain.common.entity.DomainModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DomainModelRepository extends JpaRepository<DomainModel, Long> {
    
    Optional<DomainModel> findByProjectProjectId(String projectId);
    
    @Query("SELECT dm FROM DomainModel dm LEFT JOIN FETCH dm.entities WHERE dm.project.projectId = :projectId")
    Optional<DomainModel> findByProjectIdWithEntities(@Param("projectId") String projectId);
    
    @Query("SELECT dm FROM DomainModel dm LEFT JOIN FETCH dm.relationships WHERE dm.project.projectId = :projectId")
    Optional<DomainModel> findByProjectIdWithRelationships(@Param("projectId") String projectId);
    
    boolean existsByProjectProjectId(String projectId);
}
