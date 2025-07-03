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
