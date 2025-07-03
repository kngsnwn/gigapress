#!/bin/bash

echo "🔍 Checking GigaPress Infrastructure Status..."
echo "============================================"

# Function to check service
check_service() {
    local service_name=$1
    local port=$2
    local display_name=$3
    
    if nc -z localhost $port 2>/dev/null; then
        echo "✅ $display_name is running on port $port"
    else
        echo "❌ $display_name is not accessible on port $port"
    fi
}

# Check Docker services
echo -e "\n📦 Docker Services:"
docker-compose ps

# Check port accessibility
echo -e "\n🔌 Service Connectivity:"
check_service "neo4j" 7474 "Neo4j HTTP"
check_service "neo4j" 7687 "Neo4j Bolt"
check_service "kafka" 9092 "Kafka"
check_service "redis" 6379 "Redis"
check_service "kafka-ui" 8090 "Kafka UI"
check_service "redis-commander" 8091 "Redis Commander"

# Check Neo4j
echo -e "\n🔷 Neo4j Status:"
curl -s -u neo4j:password123 http://localhost:7474/db/neo4j/cluster/available || echo "Neo4j not ready"

# Check Kafka topics
echo -e "\n📨 Kafka Topics:"
docker exec gigapress-kafka kafka-topics --bootstrap-server localhost:9092 --list 2>/dev/null || echo "No topics yet"

# Check Redis
echo -e "\n🔴 Redis Status:"
docker exec gigapress-redis redis-cli -a redis123 ping 2>/dev/null || echo "Redis not responding"

echo -e "\n============================================"
echo "✨ Infrastructure check complete!"
