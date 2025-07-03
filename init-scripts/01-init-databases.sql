-- Create additional databases for other services
CREATE DATABASE gigapress_backend;
CREATE DATABASE gigapress_frontend;
CREATE DATABASE gigapress_infra;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE gigapress_domain TO gigapress;
GRANT ALL PRIVILEGES ON DATABASE gigapress_backend TO gigapress;
GRANT ALL PRIVILEGES ON DATABASE gigapress_frontend TO gigapress;
GRANT ALL PRIVILEGES ON DATABASE gigapress_infra TO gigapress;

-- Create schemas in domain database
\c gigapress_domain;
CREATE SCHEMA IF NOT EXISTS domain_schema;
CREATE SCHEMA IF NOT EXISTS audit_schema;

-- Set search path
ALTER DATABASE gigapress_domain SET search_path TO domain_schema, public;
