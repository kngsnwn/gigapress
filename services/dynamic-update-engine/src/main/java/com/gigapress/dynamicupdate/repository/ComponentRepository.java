package com.gigapress.dynamicupdate.repository;

import com.gigapress.dynamicupdate.domain.Component;
import com.gigapress.dynamicupdate.domain.ComponentType;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ComponentRepository extends Neo4jRepository<Component, Long> {
    
    Optional<Component> findByComponentId(String componentId);
    
    List<Component> findByProjectId(String projectId);
    
    List<Component> findByType(ComponentType type);
    
    @Query("MATCH (c:Component {componentId: $componentId})-[d:DEPENDS_ON]->(dep:Component) " +
           "RETURN c, collect(d), collect(dep)")
    Optional<Component> findByComponentIdWithDependencies(@Param("componentId") String componentId);
    
    @Query("MATCH (c:Component {componentId: $componentId})<-[d:DEPENDS_ON]-(dep:Component) " +
           "RETURN dep")
    Set<Component> findDependents(@Param("componentId") String componentId);
    
    @Query("MATCH (c:Component {componentId: $componentId})-[d:DEPENDS_ON*1..]->(dep:Component) " +
           "RETURN DISTINCT dep")
    Set<Component> findAllDependencies(@Param("componentId") String componentId);
    
    @Query("MATCH (c:Component {componentId: $componentId})<-[d:DEPENDS_ON*1..]-(dep:Component) " +
           "RETURN DISTINCT dep")
    Set<Component> findAllDependents(@Param("componentId") String componentId);
    
    @Query("MATCH path = (c1:Component {componentId: $sourceId})-[d:DEPENDS_ON*]->(c2:Component {componentId: $targetId}) " +
           "RETURN path LIMIT 1")
    Optional<Object> findDependencyPath(@Param("sourceId") String sourceId, @Param("targetId") String targetId);
    
    @Query("MATCH (c:Component {componentId: $componentId})-[d:DEPENDS_ON]->(c) " +
           "RETURN c")
    Optional<Component> findCircularDependency(@Param("componentId") String componentId);
    
    @Query("MATCH (c:Component) WHERE c.projectId = $projectId " +
           "OPTIONAL MATCH (c)-[d:DEPENDS_ON]->(dep:Component) " +
           "RETURN c, collect(d), collect(dep)")
    List<Component> findProjectComponentsWithDependencies(@Param("projectId") String projectId);
}
