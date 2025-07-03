#!/bin/bash

echo "🚀 Starting GigaPress Infrastructure..."

# Create necessary directories
mkdir -p init-scripts

# Create init script for PostgreSQL
cat > init-scripts/01-init-databases.sql << 'EOF'
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
EOF

# Start all services
echo "Starting all infrastructure services..."
docker-compose up -d

# Wait for services to be ready
echo "Waiting for services to be ready..."
sleep 20

# Check service status
echo ""
echo "📊 Service Status:"
echo "=================="

# PostgreSQL
if docker exec gigapress-postgres pg_isready -U gigapress > /dev/null 2>&1; then
    echo "✅ PostgreSQL: Running (Port 5432)"
else
    echo "❌ PostgreSQL: Not ready"
fi

# Neo4j
if curl -s http://localhost:7474 > /dev/null 2>&1; then
    echo "✅ Neo4j: Running (Port 7474)"
else
    echo "❌ Neo4j: Not ready"
fi

# Kafka
if docker exec gigapress-kafka kafka-broker-api-versions --bootstrap-server localhost:9092 > /dev/null 2>&1; then
    echo "✅ Kafka: Running (Port 9092)"
else
    echo "❌ Kafka: Not ready"
fi

# Redis
if docker exec gigapress-redis redis-cli -a redis123 ping > /dev/null 2>&1; then
    echo "✅ Redis: Running (Port 6379)"
else
    echo "❌ Redis: Not ready"
fi

echo ""
echo "📋 Access URLs:"
echo "==============="
echo "🗄️  PostgreSQL: localhost:5432"
echo "    - Username: gigapress"
echo "    - Password: gigapress123"
echo "    - Databases: gigapress_domain, gigapress_backend, gigapress_frontend, gigapress_infra"
echo ""
echo "🔧 PgAdmin: http://localhost:5050"
echo "    - Email: admin@gigapress.ai"
echo "    - Password: admin123"
echo ""
echo "🔷 Neo4j Browser: http://localhost:7474"
echo "    - Username: neo4j"
echo "    - Password: password123"
echo ""
echo "📊 Kafka UI: http://localhost:8090"
echo ""
echo "💾 Redis Commander: http://localhost:8091"
echo ""
echo "✅ All infrastructure services are starting..."
echo "   Run 'docker-compose logs -f' to see logs"
echo "   Run 'docker-compose ps' to check status"