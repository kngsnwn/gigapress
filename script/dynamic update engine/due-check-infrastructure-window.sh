#!/bin/bash

echo "🔍 Checking GigaPress Infrastructure Status..."
echo "============================================"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to check service using curl or docker
check_service_windows() {
    local service_name=$1
    local port=$2
    local display_name=$3
    local container_name=$4
    
    # Try to check if container is running
    if docker ps --format "table {{.Names}}" | grep -q "$container_name"; then
        echo -e "${GREEN}✅ $display_name is running (Container: $container_name)${NC}"
    else
        echo -e "${RED}❌ $display_name container is not running${NC}"
    fi
}

# Check Docker services
echo -e "\n📦 Docker Services:"
docker-compose ps

# Check container status
echo -e "\n🔌 Service Status:"
check_service_windows "neo4j" 7474 "Neo4j" "gigapress-neo4j"
check_service_windows "kafka" 9092 "Kafka" "gigapress-kafka"
check_service_windows "redis" 6379 "Redis" "gigapress-redis"
check_service_windows "kafka-ui" 8090 "Kafka UI" "gigapress-kafka-ui"
check_service_windows "redis-commander" 8091 "Redis Commander" "gigapress-redis-commander"

# Check Neo4j with proper endpoint
echo -e "\n🔷 Neo4j Status:"
curl -s -u neo4j:password123 http://localhost:7474/db/neo4j/ 2>/dev/null || echo "Checking Neo4j..."
# Alternative check using docker exec
docker exec gigapress-neo4j cypher-shell -u neo4j -p password123 "RETURN 'Neo4j is running' as status" 2>/dev/null || echo "Neo4j might still be starting up"

# Check Kafka topics
echo -e "\n📨 Kafka Topics:"
docker exec gigapress-kafka kafka-topics --bootstrap-server localhost:9092 --list 2>/dev/null || echo "No topics yet"

# Check Redis
echo -e "\n🔴 Redis Status:"
docker exec gigapress-redis redis-cli -a redis123 ping 2>/dev/null || echo "Redis not responding"

# Check container health status
echo -e "\n🏥 Container Health Status:"
docker ps --format "table {{.Names}}\t{{.Status}}" | grep gigapress

# Display access URLs
echo -e "\n🌐 Access URLs:"
echo "  Neo4j Browser:     http://localhost:7474     (neo4j/password123)"
echo "  Kafka UI:          http://localhost:8090"
echo "  Redis Commander:   http://localhost:8091"

# Test actual connectivity with timeout
echo -e "\n🔗 Testing Web UI Connectivity:"
echo -n "  Neo4j Browser: "
curl -s -o /dev/null -w "%{http_code}" -m 2 http://localhost:7474 2>/dev/null && echo -e "${GREEN}✅ Accessible${NC}" || echo -e "${RED}❌ Not accessible${NC}"

echo -n "  Kafka UI: "
curl -s -o /dev/null -w "%{http_code}" -m 2 http://localhost:8090 2>/dev/null && echo -e "${GREEN}✅ Accessible${NC}" || echo -e "${RED}❌ Not accessible${NC}"

echo -n "  Redis Commander: "
curl -s -o /dev/null -w "%{http_code}" -m 2 http://localhost:8091 2>/dev/null && echo -e "${GREEN}✅ Accessible${NC}" || echo -e "${RED}❌ Not accessible${NC}"

echo -e "\n============================================"
echo "✨ Infrastructure check complete!"

# Windows-specific note
if [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "win32" ]]; then
    echo -e "\n📌 Note: Running on Windows. If services are not accessible:"
    echo "   1. Check Windows Firewall settings"
    echo "   2. Ensure Docker Desktop is running with WSL2 backend"
    echo "   3. Try accessing services directly in browser"
fi