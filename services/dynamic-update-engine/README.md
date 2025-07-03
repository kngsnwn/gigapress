# Dynamic Update Engine

## Overview
The Dynamic Update Engine is responsible for managing component dependencies and propagating changes throughout the GigaPress system.

## Features
- Component dependency graph management (Neo4j)
- Event-driven update propagation (Kafka)
- Change impact analysis
- Caching layer (Redis)

## Tech Stack
- Java 17
- Spring Boot 3.2.0
- Spring Data Neo4j
- Spring Kafka
- Spring Data Redis
- Gradle 8.5

## Setup

### Prerequisites
- Java 17+
- Docker & Docker Compose (for infrastructure)
- Infrastructure services running (Neo4j, Kafka, Redis)

### Build
```bash
./gradlew build
```

### Run
```bash
./gradlew bootRun
```

## API Endpoints
- Health check: `GET http://localhost:8081/actuator/health`
- Component dependencies: `GET http://localhost:8081/api/dependencies/{componentId}`
- Update propagation: `POST http://localhost:8081/api/updates`

## Configuration
See `src/main/resources/application.properties` for configuration options.
