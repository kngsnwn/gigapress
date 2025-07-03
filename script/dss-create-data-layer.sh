#!/bin/bash

# Data Layer Creation Script for Domain/Schema Service - Correct Path Version
set -e

echo "🎯 Creating Data Layer for Domain/Schema Service..."

# Define the correct path
SERVICE_PATH="services/domain-schema-service"

# Check if domain-schema-service directory exists
if [ ! -d "$SERVICE_PATH" ]; then
    echo "❌ Error: $SERVICE_PATH directory not found!"
    echo "Current directory: $(pwd)"
    echo "Please ensure you're in the gigapress root directory."
    exit 1
fi

# Navigate to the project directory
cd "$SERVICE_PATH"
echo "✅ Navigated to: $(pwd)"

# Check if src directory exists
if [ ! -d "src/main/java/com/gigapress/domainschema" ]; then
    echo "❌ Error: Project structure is incomplete!"
    echo "Please ensure previous steps (1-4) have been completed successfully."
    exit 1
fi

# Create additional database migrations
echo "🗄️ Creating additional database migrations..."
cd src/main/resources/db/migration

# Create V2 migration for additional tables and constraints
cat > V2__add_constraints_and_indexes.sql << 'EOF'
-- Add foreign key constraints with proper naming
ALTER TABLE domain_schema.requirements 
    ADD CONSTRAINT fk_requirements_project_status 
    CHECK (status IN ('PENDING', 'ANALYZING', 'ANALYZED', 'IMPLEMENTED', 'VERIFIED', 'REJECTED'));

ALTER TABLE domain_schema.projects 
    ADD CONSTRAINT fk_projects_status 
    CHECK (status IN ('CREATED', 'ANALYZING', 'DESIGNING', 'SCHEMA_GENERATION', 'COMPLETED', 'FAILED', 'ARCHIVED'));

-- Add composite unique constraints
ALTER TABLE domain_schema.domain_models 
    ADD CONSTRAINT uk_domain_models_project UNIQUE (project_id);

ALTER TABLE domain_schema.schema_designs 
    ADD CONSTRAINT uk_schema_designs_project UNIQUE (project_id);

-- Add performance indexes
CREATE INDEX idx_requirements_type ON domain_schema.requirements(type);
CREATE INDEX idx_requirements_priority ON domain_schema.requirements(priority);
CREATE INDEX idx_domain_entities_type ON domain_schema.domain_entities(entity_type);
CREATE INDEX idx_schema_designs_database_type ON domain_schema.schema_designs(database_type);

-- Add updated_at trigger function
CREATE OR REPLACE FUNCTION domain_schema.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply updated_at trigger to all tables
CREATE TRIGGER update_projects_updated_at BEFORE UPDATE ON domain_schema.projects
    FOR EACH ROW EXECUTE FUNCTION domain_schema.update_updated_at_column();

CREATE TRIGGER update_requirements_updated_at BEFORE UPDATE ON domain_schema.requirements
    FOR EACH ROW EXECUTE FUNCTION domain_schema.update_updated_at_column();

CREATE TRIGGER update_domain_models_updated_at BEFORE UPDATE ON domain_schema.domain_models
    FOR EACH ROW EXECUTE FUNCTION domain_schema.update_updated_at_column();

CREATE TRIGGER update_domain_entities_updated_at BEFORE UPDATE ON domain_schema.domain_entities
    FOR EACH ROW EXECUTE FUNCTION domain_schema.update_updated_at_column();

CREATE TRIGGER update_schema_designs_updated_at BEFORE UPDATE ON domain_schema.schema_designs
    FOR EACH ROW EXECUTE FUNCTION domain_schema.update_updated_at_column();

CREATE TRIGGER update_table_designs_updated_at BEFORE UPDATE ON domain_schema.table_designs
    FOR EACH ROW EXECUTE FUNCTION domain_schema.update_updated_at_column();
EOF

# Create V3 migration for audit tables
cat > V3__create_audit_tables.sql << 'EOF'
-- Create audit schema
CREATE SCHEMA IF NOT EXISTS audit_schema;

-- Create project audit table
CREATE TABLE IF NOT EXISTS audit_schema.project_audit (
    audit_id BIGSERIAL PRIMARY KEY,
    project_id VARCHAR(255) NOT NULL,
    action VARCHAR(50) NOT NULL,
    changed_by VARCHAR(255),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    old_values JSONB,
    new_values JSONB
);

CREATE INDEX idx_project_audit_project_id ON audit_schema.project_audit(project_id);
CREATE INDEX idx_project_audit_changed_at ON audit_schema.project_audit(changed_at);

-- Create analysis history table
CREATE TABLE IF NOT EXISTS domain_schema.analysis_history (
    id BIGSERIAL PRIMARY KEY,
    project_id VARCHAR(255) NOT NULL,
    analysis_type VARCHAR(50) NOT NULL,
    input_data JSONB NOT NULL,
    output_data JSONB,
    status VARCHAR(50) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    error_message TEXT,
    CONSTRAINT fk_analysis_history_project FOREIGN KEY (project_id) 
        REFERENCES domain_schema.projects(project_id) ON DELETE CASCADE
);

CREATE INDEX idx_analysis_history_project_id ON domain_schema.analysis_history(project_id);
CREATE INDEX idx_analysis_history_type ON domain_schema.analysis_history(analysis_type);
CREATE INDEX idx_analysis_history_status ON domain_schema.analysis_history(status);
EOF

# Create V4 migration for generated code storage
cat > V4__create_generated_code_tables.sql << 'EOF'
-- Create table for storing generated entity code
CREATE TABLE IF NOT EXISTS domain_schema.generated_entities (
    id BIGSERIAL PRIMARY KEY,
    project_id VARCHAR(255) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    file_content TEXT NOT NULL,
    file_size BIGINT NOT NULL,
    checksum VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_generated_entities_project FOREIGN KEY (project_id) 
        REFERENCES domain_schema.projects(project_id) ON DELETE CASCADE
);

CREATE INDEX idx_generated_entities_project_id ON domain_schema.generated_entities(project_id);
CREATE INDEX idx_generated_entities_file_type ON domain_schema.generated_entities(file_type);

-- Create table for template storage
CREATE TABLE IF NOT EXISTS domain_schema.code_templates (
    id BIGSERIAL PRIMARY KEY,
    template_name VARCHAR(255) NOT NULL UNIQUE,
    template_type VARCHAR(50) NOT NULL,
    template_content TEXT NOT NULL,
    description TEXT,
    variables JSONB,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_code_templates_type ON domain_schema.code_templates(template_type);
CREATE INDEX idx_code_templates_active ON domain_schema.code_templates(is_active);
EOF

# Navigate back to java directory
cd ../../../java/com/gigapress/domainschema

# Create custom repository implementations
echo "📦 Creating custom repository implementations..."
mkdir -p domain/common/repository/impl

# Custom ProjectRepository implementation
cat > domain/common/repository/impl/ProjectRepositoryCustom.java << 'EOF'
package com.gigapress.domainschema.domain.common.repository.impl;

import com.gigapress.domainschema.domain.common.entity.Project;
import com.gigapress.domainschema.domain.common.entity.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ProjectRepositoryCustom {
    
    Page<Project> findProjectsWithFilters(Map<String, Object> filters, Pageable pageable);
    
    List<Project> findProjectsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    Map<ProjectStatus, Long> getProjectStatusStatistics();
    
    void updateProjectStatusBulk(List<String> projectIds, ProjectStatus newStatus);
}
EOF

# Custom ProjectRepository implementation
cat > domain/common/repository/impl/ProjectRepositoryImpl.java << 'EOF'
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
EOF

# Create Data initialization
echo "📦 Creating data initialization..."
mkdir -p domain/common/init

# DataInitializer
cat > domain/common/init/DataInitializer.java << 'EOF'
package com.gigapress.domainschema.domain.common.init;

import com.gigapress.domainschema.domain.common.entity.ProjectType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DataInitializer implements CommandLineRunner {
    
    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing development data...");
        
        // Initialize code templates
        initializeCodeTemplates();
        
        // Initialize sample projects (optional)
        // initializeSampleProjects();
        
        log.info("Development data initialization completed");
    }
    
    private void initializeCodeTemplates() {
        log.info("Initializing code templates...");
        // Template initialization will be implemented in the next step
    }
}
EOF

# Create Repository Tests
echo "📦 Creating repository tests..."
mkdir -p domain-schema-service/src/test/java/com/gigapress/domainschema/domain/common/repository

# ProjectRepositoryTest
cat > domain/common/repository/ProjectRepositoryTest.java << 'EOF'
package com.gigapress.domainschema.domain.common.repository;

import com.gigapress.domainschema.domain.common.entity.Project;
import com.gigapress.domainschema.domain.common.entity.ProjectStatus;
import com.gigapress.domainschema.domain.common.entity.ProjectType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ProjectRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    private Project testProject;
    
    @BeforeEach
    void setUp() {
        testProject = Project.builder()
                .projectId("test_proj_001")
                .name("Test Project")
                .description("Test Description")
                .projectType(ProjectType.WEB_APPLICATION)
                .status(ProjectStatus.CREATED)
                .build();
        
        entityManager.persistAndFlush(testProject);
    }
    
    @Test
    void findByProjectId_ShouldReturnProject() {
        // When
        Optional<Project> found = projectRepository.findByProjectId("test_proj_001");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Project");
    }
    
    @Test
    void existsByProjectId_ShouldReturnTrue_WhenProjectExists() {
        // When
        boolean exists = projectRepository.existsByProjectId("test_proj_001");
        
        // Then
        assertThat(exists).isTrue();
    }
    
    @Test
    void findByStatus_ShouldReturnProjects() {
        // Given
        Project anotherProject = Project.builder()
                .projectId("test_proj_002")
                .name("Another Project")
                .projectType(ProjectType.REST_API)
                .status(ProjectStatus.CREATED)
                .build();
        entityManager.persistAndFlush(anotherProject);
        
        // When
        Page<Project> projects = projectRepository.findByStatus(
                ProjectStatus.CREATED, PageRequest.of(0, 10));
        
        // Then
        assertThat(projects.getContent()).hasSize(2);
        assertThat(projects.getTotalElements()).isEqualTo(2);
    }
}
EOF

# Create test resources
echo "📦 Creating test resources..."
cd ../../../../resources

# Create test SQL data
mkdir -p sql
cat > sql/test-data.sql << 'EOF'
-- Test data for integration tests
INSERT INTO domain_schema.projects (id, project_id, name, description, project_type, status, created_at, updated_at, version)
VALUES 
    (1000, 'test_proj_sample', 'Sample Test Project', 'A sample project for testing', 'WEB_APPLICATION', 'CREATED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO domain_schema.requirements (id, title, description, type, priority, status, project_id, created_at, updated_at, version)
VALUES 
    (2000, 'Sample Requirement', 'A sample requirement for testing', 'FUNCTIONAL', 'HIGH', 'PENDING', 1000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
EOF

# Update test application properties
cat >> application-test.properties << 'EOF'

# Test data initialization
spring.sql.init.mode=always
spring.sql.init.data-locations=classpath:sql/test-data.sql

# Testcontainers
spring.testcontainers.enabled=true

# Logging for tests
logging.level.org.springframework.test=INFO
logging.level.org.testcontainers=INFO
EOF

# Create production database configuration
echo "📦 Creating production database configuration..."
mkdir -p domain-schema-service/src/main/resources

cat > application-prod.properties << 'EOF'
# Production Database Configuration
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/gigapress_domain}
spring.datasource.username=${DB_USERNAME:gigapress}
spring.datasource.password=${DB_PASSWORD:gigapress123}

# JPA Configuration for production
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Flyway Configuration
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true

# Kafka Configuration for production
spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}

# Redis Configuration for production
spring.data.redis.host=${REDIS_HOST:localhost}
spring.data.redis.port=${REDIS_PORT:6379}
spring.data.redis.password=${REDIS_PASSWORD:redis123}

# Logging for production
logging.level.com.gigapress.domainschema=INFO
logging.level.org.springframework=WARN
logging.level.org.hibernate=WARN
EOF

# Create development database configuration
cat > application-dev.properties << 'EOF'
# Development Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/gigapress_domain
spring.datasource.username=gigapress
spring.datasource.password=gigapress123

# JPA Configuration for development
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Enable SQL logging in development
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# Development specific settings
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
EOF

# Go back to project root (domain-schema-service)
cd ../../../../../../..

# Create Docker Compose for development database
echo "🐳 Creating Docker Compose for database..."
cat > docker-compose-db.yml << 'EOF'
version: '3.8'

services:
  postgres-domain:
    image: postgres:15-alpine
    container_name: gigapress-postgres-domain
    environment:
      POSTGRES_DB: gigapress_domain
      POSTGRES_USER: gigapress
      POSTGRES_PASSWORD: gigapress123
    ports:
      - "5432:5432"
    volumes:
      - postgres_domain_data:/var/lib/postgresql/data
    networks:
      - gigapress-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U gigapress -d gigapress_domain"]
      interval: 10s
      timeout: 5s
      retries: 5

  pgadmin:
    image: dpage/pgadmin4:latest
    container_name: gigapress-pgadmin
    environment:
      PGADMIN_DEFAULT_EMAIL: admin@gigapress.ai
      PGADMIN_DEFAULT_PASSWORD: admin123
    ports:
      - "5050:80"
    depends_on:
      - postgres-domain
    networks:
      - gigapress-network

volumes:
  postgres_domain_data:

networks:
  gigapress-network:
    external: true
EOF

# Create database initialization script
echo "📝 Creating database initialization script..."
cat > init-database.sh << 'EOF'
#!/bin/bash

echo "🗄️ Initializing Domain/Schema Service Database..."

# Start PostgreSQL container
echo "Starting PostgreSQL container..."
docker-compose -f docker-compose-db.yml up -d postgres-domain

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to be ready..."
sleep 10

# Run Flyway migrations
echo "Running database migrations..."
cd services/domain-schema-service
./gradlew flywayMigrate

echo "✅ Database initialization completed!"
echo ""
echo "📊 Database Info:"
echo "  - Host: localhost:5432"
echo "  - Database: gigapress_domain"
echo "  - Username: gigapress"
echo "  - Password: gigapress123"
echo ""
echo "🔧 PgAdmin available at: http://localhost:5050"
echo "  - Email: admin@gigapress.ai"
echo "  - Password: admin123"
EOF

chmod +x init-database.sh

# Update build.gradle to include Flyway plugin
echo "📦 Updating build.gradle with Flyway plugin..."

# Add Flyway to build.gradle if not already present
if ! grep -q "org.flywaydb.flyway" build.gradle; then
    sed -i "/plugins {/a\\    id 'org.flywaydb.flyway' version '9.22.3'" build.gradle
    
    # Add Flyway configuration
    cat >> build.gradle << 'EOF'

// Flyway configuration
flyway {
    url = 'jdbc:postgresql://localhost:5432/gigapress_domain'
    user = 'gigapress'
    password = 'gigapress123'
    schemas = ['domain_schema']
    locations = ['classpath:db/migration']
}
EOF
fi

echo "✅ Data Layer created successfully!"
echo ""
echo "📋 Created:"
echo "  - Database Migrations:"
echo "    - V1: Initial schema (existing)"
echo "    - V2: Constraints and indexes"
echo "    - V3: Audit tables"
echo "    - V4: Generated code storage"
echo "  - Custom Repository Implementations:"
echo "    - ProjectRepositoryCustom with advanced queries"
echo "    - Criteria API implementation"
echo "  - Test Infrastructure:"
echo "    - Repository tests"
echo "    - Test configuration (H2 in-memory DB)"
echo "  - Environment Configurations:"
echo "    - application-dev.properties"
echo "    - application-prod.properties"
echo "    - application-test.properties"
echo "  - Database Tools:"
echo "    - docker-compose-db.yml (PostgreSQL + PgAdmin)"
echo "    - init-database.sh script"
echo ""
echo "🎯 Next steps:"
echo "1. Initialize the database: ./init-database.sh"
echo "2. Run the service: cd services/domain-schema-service && ./gradlew bootRun"
echo "3. Access PgAdmin at: http://localhost:5050"