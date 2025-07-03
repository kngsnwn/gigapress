#!/bin/bash

echo "🚀 Starting GigaPress Infrastructure..."

# Start Docker Compose
docker-compose up -d

# Wait for services to be ready
echo "⏳ Waiting for services to start..."
sleep 10

# Run infrastructure check
./check-infrastructure.sh

# Create Kafka topics
echo -e "\n📨 Initializing Kafka topics..."
./create-kafka-topics.sh

echo -e "\n🌐 Web UIs available at:"
echo "  - Neo4j Browser: http://localhost:7474 (neo4j/password123)"
echo "  - Kafka UI: http://localhost:8090"
echo "  - Redis Commander: http://localhost:8091"

echo -e "\n✨ GigaPress infrastructure is ready!"
