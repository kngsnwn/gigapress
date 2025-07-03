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
