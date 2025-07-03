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

# Import orchestration endpoint
from app.api.endpoints import orchestration

# Add orchestration router
app.include_router(orchestration.router, prefix="/api/v1/orchestration", tags=["orchestration"])
