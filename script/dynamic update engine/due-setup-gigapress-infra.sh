#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 GigaPress Infrastructure Setup Script${NC}"
echo "========================================"

# Create project directory structure
echo -e "\n${YELLOW}📁 Creating project directory structure...${NC}"
mkdir -p gigapress/services/{dynamic-update-engine,mcp-server,domain-schema-service,backend-service,design-frontend-service,infra-version-control-service,conversational-ai-engine,conversational-layer}

cd gigapress

# Create docker-compose.yml
echo -e "\n${YELLOW}📝 Creating docker-compose.yml...${NC}"
cat > docker-compose.yml << 'EOF'
version: '3.8'

services:
  # Neo4j - Graph Database for dependency tracking
  neo4j:
    image: neo4j:5-community
    container_name: gigapress-neo4j
    ports:
      - "7474:7474"  # HTTP
      - "7687:7687"  # Bolt
    environment:
      - NEO4J_AUTH=neo4j/password123
      - NEO4J_PLUGINS=["apoc"]
    volumes:
      - neo4j_data:/data
      - neo4j_logs:/logs
      - neo4j_import:/var/lib/neo4j/import
      - neo4j_plugins:/plugins
    healthcheck:
      test: ["CMD", "neo4j", "status"]
      interval: 10s
      timeout: 10s
      retries: 5
    networks:
      - gigapress-network

  # Zookeeper for Kafka
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    container_name: gigapress-zookeeper
    ports:
      - "2181:2181"
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    volumes:
      - zookeeper_data:/var/lib/zookeeper/data
      - zookeeper_logs:/var/lib/zookeeper/log
    networks:
      - gigapress-network

  # Kafka - Event streaming platform
  kafka:
    image: confluentinc/cp-kafka:7.5.0
    container_name: gigapress-kafka
    ports:
      - "9092:9092"
      - "9101:9101"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: 'zookeeper:2181'
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
      KAFKA_JMX_PORT: 9101
      KAFKA_JMX_HOSTNAME: localhost
    volumes:
      - kafka_data:/var/lib/kafka/data
    depends_on:
      - zookeeper
    healthcheck:
      test: ["CMD", "kafka-broker-api-versions", "--bootstrap-server", "localhost:9092"]
      interval: 10s
      timeout: 10s
      retries: 5
    networks:
      - gigapress-network

  # Kafka UI for monitoring
  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    container_name: gigapress-kafka-ui
    ports:
      - "8090:8080"
    environment:
      KAFKA_CLUSTERS_0_NAME: gigapress
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:29092
      KAFKA_CLUSTERS_0_ZOOKEEPER: zookeeper:2181
    depends_on:
      - kafka
      - zookeeper
    networks:
      - gigapress-network

  # Redis - Caching layer
  redis:
    image: redis:7-alpine
    container_name: gigapress-redis
    ports:
      - "6379:6379"
    command: redis-server --requirepass redis123 --appendonly yes
    volumes:
      - redis_data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "--raw", "incr", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - gigapress-network

  # Redis Commander - Redis UI
  redis-commander:
    image: rediscommander/redis-commander:latest
    container_name: gigapress-redis-commander
    ports:
      - "8091:8081"
    environment:
      - REDIS_HOSTS=local:redis:6379:0:redis123
    depends_on:
      - redis
    networks:
      - gigapress-network

# Volumes for data persistence
volumes:
  neo4j_data:
  neo4j_logs:
  neo4j_import:
  neo4j_plugins:
  zookeeper_data:
  zookeeper_logs:
  kafka_data:
  redis_data:

# Network configuration
networks:
  gigapress-network:
    driver: bridge
EOF

echo -e "${GREEN}✅ docker-compose.yml created successfully!${NC}"

# Create infrastructure check script
echo -e "\n${YELLOW}📝 Creating infrastructure check script...${NC}"
cat > check-infrastructure.sh << 'EOF'
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
EOF

chmod +x check-infrastructure.sh
echo -e "${GREEN}✅ check-infrastructure.sh created successfully!${NC}"

# Create Kafka topics initialization script
echo -e "\n${YELLOW}📝 Creating Kafka topics initialization script...${NC}"
cat > create-kafka-topics.sh << 'EOF'
#!/bin/bash

echo "📨 Creating Kafka topics for GigaPress..."

# Wait for Kafka to be ready
echo "⏳ Waiting for Kafka to be ready..."
while ! docker exec gigapress-kafka kafka-broker-api-versions --bootstrap-server localhost:9092 >/dev/null 2>&1; do
    sleep 1
done
echo "✅ Kafka is ready!"

# Function to create topic
create_topic() {
    local topic_name=$1
    local partitions=$2
    local replication=$3
    
    docker exec gigapress-kafka kafka-topics \
        --create \
        --bootstrap-server localhost:9092 \
        --topic $topic_name \
        --partitions $partitions \
        --replication-factor $replication \
        --if-not-exists
    
    if [ $? -eq 0 ]; then
        echo "✅ Topic '$topic_name' created successfully"
    else
        echo "❌ Failed to create topic '$topic_name'"
    fi
}

# Create topics
create_topic "project.updates" 3 1
create_topic "component.changes" 3 1
create_topic "dependency.events" 3 1
create_topic "validation.results" 3 1
create_topic "generation.requests" 3 1
create_topic "generation.responses" 3 1

echo -e "\n📋 Listing all topics:"
docker exec gigapress-kafka kafka-topics --bootstrap-server localhost:9092 --list

echo -e "\n✨ Kafka topics initialization complete!"
EOF

chmod +x create-kafka-topics.sh
echo -e "${GREEN}✅ create-kafka-topics.sh created successfully!${NC}"

# Create start script
echo -e "\n${YELLOW}📝 Creating start script...${NC}"
cat > start-infrastructure.sh << 'EOF'
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
EOF

chmod +x start-infrastructure.sh
echo -e "${GREEN}✅ start-infrastructure.sh created successfully!${NC}"

# Create stop script
echo -e "\n${YELLOW}📝 Creating stop script...${NC}"
cat > stop-infrastructure.sh << 'EOF'
#!/bin/bash

echo "🛑 Stopping GigaPress Infrastructure..."
docker-compose down

echo "✅ Infrastructure stopped!"
echo ""
echo "To remove volumes as well, run:"
echo "  docker-compose down -v"
EOF

chmod +x stop-infrastructure.sh
echo -e "${GREEN}✅ stop-infrastructure.sh created successfully!${NC}"

# Create README
echo -e "\n${YELLOW}📝 Creating README.md...${NC}"
cat > README.md << 'EOF'
# GigaPress Local Infrastructure

## Quick Start

1. Start all infrastructure services:
   ```bash
   ./start-infrastructure.sh
   ```

2. Check infrastructure status:
   ```bash
   ./check-infrastructure.sh
   ```

3. Stop infrastructure:
   ```bash
   ./stop-infrastructure.sh
   ```

## Services

| Service | Port | Web UI | Credentials |
|---------|------|--------|-------------|
| Neo4j | 7474, 7687 | http://localhost:7474 | neo4j/password123 |
| Kafka | 9092 | http://localhost:8090 | - |
| Redis | 6379 | http://localhost:8091 | password: redis123 |

## Scripts

- `start-infrastructure.sh` - Start all services and initialize
- `stop-infrastructure.sh` - Stop all services
- `check-infrastructure.sh` - Check service status
- `create-kafka-topics.sh` - Create Kafka topics

## Project Structure

```
gigapress/
├── docker-compose.yml
├── start-infrastructure.sh
├── stop-infrastructure.sh
├── check-infrastructure.sh
├── create-kafka-topics.sh
├── README.md
└── services/
    ├── dynamic-update-engine/
    ├── mcp-server/
    ├── domain-schema-service/
    ├── backend-service/
    ├── design-frontend-service/
    ├── infra-version-control-service/
    ├── conversational-ai-engine/
    └── conversational-layer/
```
EOF

echo -e "${GREEN}✅ README.md created successfully!${NC}"

# Final summary
echo -e "\n${BLUE}========================================${NC}"
echo -e "${GREEN}✨ GigaPress infrastructure setup complete!${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo "📁 Created files:"
echo "  - docker-compose.yml"
echo "  - start-infrastructure.sh"
echo "  - stop-infrastructure.sh"
echo "  - check-infrastructure.sh"
echo "  - create-kafka-topics.sh"
echo "  - README.md"
echo ""
echo "🚀 To start the infrastructure, run:"
echo -e "  ${YELLOW}cd gigapress${NC}"
echo -e "  ${YELLOW}./start-infrastructure.sh${NC}"
echo ""
echo "📋 For more information, see README.md"