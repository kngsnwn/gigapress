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
