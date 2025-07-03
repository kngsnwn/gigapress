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
