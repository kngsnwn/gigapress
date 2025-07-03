# GigaPress Backend Service

## Overview
Backend Service for GigaPress project running on port 8084.

## Features
- API endpoint generation
- Business logic implementation
- JWT-based authentication
- Service integration with other microservices
- Kafka event handling

## API Endpoints
- POST /api/generation/generate - Generate API endpoints
- GET /api/generation/health - Health check

## Running the Service
```bash
./gradlew bootRun
```

## Building
```bash
./gradlew build
```

## Dependencies
- Domain/Schema Service (port 8083)
- MCP Server (port 8082)
- Dynamic Update Engine (port 8081)
- Kafka (port 9092)
- Redis (port 6379)
