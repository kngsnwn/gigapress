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
