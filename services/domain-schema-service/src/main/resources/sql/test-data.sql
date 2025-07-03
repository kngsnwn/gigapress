-- Test data for integration tests
INSERT INTO domain_schema.projects (id, project_id, name, description, project_type, status, created_at, updated_at, version)
VALUES 
    (1000, 'test_proj_sample', 'Sample Test Project', 'A sample project for testing', 'WEB_APPLICATION', 'CREATED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO domain_schema.requirements (id, title, description, type, priority, status, project_id, created_at, updated_at, version)
VALUES 
    (2000, 'Sample Requirement', 'A sample requirement for testing', 'FUNCTIONAL', 'HIGH', 'PENDING', 1000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
