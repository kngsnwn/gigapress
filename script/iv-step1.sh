#!/bin/bash

# Step 1: Project Structure and Basic Configuration
# This script creates the basic structure and configuration files

SERVICE_NAME="infra-version-control-service"
SERVICE_DIR="services/${SERVICE_NAME}"
PORT=8086

echo "🚀 Step 1: Creating project structure and basic configuration..."

# Create directory structure
mkdir -p ${SERVICE_DIR}/{app/{api/{endpoints,dependencies},core,models,schemas,services,templates/{docker,kubernetes,cicd,git,terraform,monitoring},utils},tests,scripts,logs,repositories}

# Create requirements.txt
cat > ${SERVICE_DIR}/requirements.txt << 'EOF'
fastapi==0.109.0
uvicorn==0.27.0
pydantic==2.5.3
python-multipart==0.0.6
aiofiles==23.2.1
httpx==0.26.0
aiokafka==0.10.0
redis==5.0.1
GitPython==3.1.41
docker==7.0.0
kubernetes==29.0.0
jinja2==3.1.3
pyyaml==6.0.1
prometheus-client==0.19.0
python-dotenv==1.0.0
pytest==7.4.4
pytest-asyncio==0.23.3
EOF

# Create .env file
cat > ${SERVICE_DIR}/.env << 'EOF'
SERVICE_NAME=infra-version-control-service
SERVICE_PORT=8086
ENVIRONMENT=development

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=redis123

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_CONSUMER_GROUP=infra-version-control-group
KAFKA_TOPICS=project-updates,infra-requests,git-events

# Service URLs
MCP_SERVER_URL=http://localhost:8082
DOMAIN_SCHEMA_SERVICE_URL=http://localhost:8083
BACKEND_SERVICE_URL=http://localhost:8084
DESIGN_FRONTEND_SERVICE_URL=http://localhost:8085

# Git Configuration
GIT_DEFAULT_BRANCH=main
GIT_AUTHOR_NAME=GigaPress Bot
GIT_AUTHOR_EMAIL=bot@gigapress.io

# Docker Configuration
DOCKER_REGISTRY=localhost:5000
DOCKER_DEFAULT_BASE_IMAGE=ubuntu:22.04

# Kubernetes Configuration
K8S_DEFAULT_NAMESPACE=gigapress
K8S_DEFAULT_REPLICAS=3
EOF

# Create application.properties for consistency with Java services
cat > ${SERVICE_DIR}/application.properties << 'EOF'
# Service Configuration
service.name=infra-version-control-service
service.port=8086
environment=development

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=redis123

# Kafka
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=infra-version-control-group

# Service Discovery
mcp.server.url=http://localhost:8082
domain.schema.service.url=http://localhost:8083
backend.service.url=http://localhost:8084
design.frontend.service.url=http://localhost:8085
EOF

# Create main.py
cat > ${SERVICE_DIR}/main.py << 'EOF'
"""
Infra/Version Control Service - Main Application
Handles Docker, Kubernetes, CI/CD, Git operations for GigaPress
"""
import os
import sys
from contextlib import asynccontextmanager

import uvicorn
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from prometheus_client import make_asgi_app

# Add app directory to Python path
sys.path.insert(0, os.path.join(os.path.dirname(__file__), "app"))

from app.api.endpoints import docker, kubernetes, cicd, git, terraform, monitoring, health
from app.core.config import settings
from app.core.logging import setup_logging
from app.services.kafka_service import kafka_service
from app.services.redis_service import redis_service

# Setup logging
logger = setup_logging()

@asynccontextmanager
async def lifespan(app: FastAPI):
    """Application lifespan manager"""
    logger.info(f"Starting {settings.SERVICE_NAME} on port {settings.SERVICE_PORT}")
    
    # Startup
    try:
        # Initialize Redis connection
        await redis_service.connect()
        logger.info("Redis connection established")
        
        # Initialize Kafka
        await kafka_service.start()
        logger.info("Kafka consumer started")
        
        yield
        
    finally:
        # Shutdown
        logger.info("Shutting down services...")
        await kafka_service.stop()
        await redis_service.disconnect()
        logger.info("All services shut down")

# Create FastAPI app
app = FastAPI(
    title=settings.SERVICE_NAME,
    description="Infrastructure and Version Control Service for GigaPress",
    version="1.0.0",
    lifespan=lifespan
)

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Add Prometheus metrics endpoint
metrics_app = make_asgi_app()
app.mount("/metrics", metrics_app)

# Include routers
app.include_router(health.router, tags=["health"])
app.include_router(docker.router, prefix="/api/v1/docker", tags=["docker"])
app.include_router(kubernetes.router, prefix="/api/v1/kubernetes", tags=["kubernetes"])
app.include_router(cicd.router, prefix="/api/v1/cicd", tags=["cicd"])
app.include_router(git.router, prefix="/api/v1/git", tags=["git"])
app.include_router(terraform.router, prefix="/api/v1/terraform", tags=["terraform"])
app.include_router(monitoring.router, prefix="/api/v1/monitoring", tags=["monitoring"])

if __name__ == "__main__":
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=settings.SERVICE_PORT,
        reload=settings.ENVIRONMENT == "development"
    )
EOF

# Create core configuration
cat > ${SERVICE_DIR}/app/core/config.py << 'EOF'
"""Configuration settings for Infra/Version Control Service"""
import os
from typing import List
from pydantic_settings import BaseSettings
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

class Settings(BaseSettings):
    # Service settings
    SERVICE_NAME: str = "infra-version-control-service"
    SERVICE_PORT: int = 8086
    ENVIRONMENT: str = "development"
    
    # Redis settings
    REDIS_HOST: str = "localhost"
    REDIS_PORT: int = 6379
    REDIS_PASSWORD: str = "redis123"
    
    # Kafka settings
    KAFKA_BOOTSTRAP_SERVERS: str = "localhost:9092"
    KAFKA_CONSUMER_GROUP: str = "infra-version-control-group"
    KAFKA_TOPICS: List[str] = ["project-updates", "infra-requests", "git-events"]
    
    # Service URLs
    MCP_SERVER_URL: str = "http://localhost:8082"
    DOMAIN_SCHEMA_SERVICE_URL: str = "http://localhost:8083"
    BACKEND_SERVICE_URL: str = "http://localhost:8084"
    DESIGN_FRONTEND_SERVICE_URL: str = "http://localhost:8085"
    
    # Git settings
    GIT_DEFAULT_BRANCH: str = "main"
    GIT_AUTHOR_NAME: str = "GigaPress Bot"
    GIT_AUTHOR_EMAIL: str = "bot@gigapress.io"
    
    # Docker settings
    DOCKER_REGISTRY: str = "localhost:5000"
    DOCKER_DEFAULT_BASE_IMAGE: str = "ubuntu:22.04"
    
    # Kubernetes settings
    K8S_DEFAULT_NAMESPACE: str = "gigapress"
    K8S_DEFAULT_REPLICAS: int = 3
    
    class Config:
        case_sensitive = True
        env_file = ".env"

settings = Settings()
EOF

# Create logging configuration
cat > ${SERVICE_DIR}/app/core/logging.py << 'EOF'
"""Logging configuration"""
import logging
import sys
from pathlib import Path

def setup_logging():
    """Setup logging configuration"""
    # Create logs directory
    Path("logs").mkdir(exist_ok=True)
    
    # Configure logging
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
        handlers=[
            logging.StreamHandler(sys.stdout),
            logging.FileHandler('logs/infra-version-control.log')
        ]
    )
    
    return logging.getLogger(__name__)
EOF

# Create __init__.py files
touch ${SERVICE_DIR}/app/__init__.py
touch ${SERVICE_DIR}/app/api/__init__.py
touch ${SERVICE_DIR}/app/api/endpoints/__init__.py
touch ${SERVICE_DIR}/app/api/dependencies/__init__.py
touch ${SERVICE_DIR}/app/core/__init__.py
touch ${SERVICE_DIR}/app/models/__init__.py
touch ${SERVICE_DIR}/app/schemas/__init__.py
touch ${SERVICE_DIR}/app/services/__init__.py
touch ${SERVICE_DIR}/app/templates/__init__.py
touch ${SERVICE_DIR}/app/utils/__init__.py

echo "✅ Step 1 Complete: Basic project structure created"
echo "📁 Directory structure:"
echo "   - ${SERVICE_DIR}/"
echo "   - Configuration files created"
echo "   - Main application file created"
echo "   - Core configuration set up"