"""Docker-related API endpoints"""
from fastapi import APIRouter, HTTPException
from app.schemas.base import BaseResponse
from app.schemas.docker import DockerBuildRequest, DockerImageConfig, DockerComposeConfig, DockerComposeService
from app.services.docker_service import docker_service
from app.services.redis_service import redis_service
import json

router = APIRouter()

@router.post("/dockerfile", response_model=BaseResponse)
async def generate_dockerfile(request: DockerBuildRequest):
    """Generate Dockerfile for a service"""
    try:
        # Create Docker image configuration
        config = DockerImageConfig(
            base_image=_get_base_image(request.framework),
            workdir="/app",
            exposed_ports=request.ports,
            environment=request.environment_vars,
            commands=_get_build_commands(request.framework, request.service_type)
        )
        
        # Generate Dockerfile
        dockerfile_content = docker_service.generate_dockerfile(config)
        
        # Cache the result
        cache_key = f"dockerfile:{request.project_id}:{request.service_name}"
        await redis_service.set(cache_key, dockerfile_content)
        
        return BaseResponse(
            success=True,
            message="Dockerfile generated successfully",
            data={
                "dockerfile": dockerfile_content,
                "service_name": request.service_name
            }
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/docker-compose", response_model=BaseResponse)
async def generate_docker_compose(project_id: str, services: list):
    """Generate docker-compose.yml for the project"""
    try:
        compose_services = {}
        
        for service in services:
            service_config = DockerComposeService(
                image=service.get("image"),
                build=service.get("build"),
                ports=service.get("ports", []),
                environment=service.get("environment", {}),
                volumes=service.get("volumes", []),
                depends_on=service.get("depends_on", []),
                networks=["gigapress-network"]
            )
            compose_services[service["name"]] = service_config
            
        config = DockerComposeConfig(
            services=compose_services,
            networks={"gigapress-network": {"driver": "bridge"}}
        )
        
        # Generate docker-compose.yml
        compose_content = docker_service.generate_docker_compose(config)
        
        # Cache the result
        cache_key = f"docker-compose:{project_id}"
        await redis_service.set(cache_key, compose_content)
        
        return BaseResponse(
            success=True,
            message="docker-compose.yml generated successfully",
            data={
                "docker_compose": compose_content,
                "services_count": len(services)
            }
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/dockerignore", response_model=BaseResponse)
async def generate_dockerignore(project_id: str, framework: str):
    """Generate .dockerignore file"""
    try:
        dockerignore_content = docker_service.generate_dockerignore(framework)
        
        return BaseResponse(
            success=True,
            message=".dockerignore generated successfully",
            data={
                "dockerignore": dockerignore_content,
                "framework": framework
            }
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

def _get_base_image(framework: str) -> str:
    """Get base Docker image for framework"""
    base_images = {
        "react": "node:18-alpine",
        "vue": "node:18-alpine",
        "angular": "node:18-alpine",
        "express": "node:18-alpine",
        "spring-boot": "openjdk:17-jdk-slim",
        "django": "python:3.10-slim",
        "flask": "python:3.10-slim",
        "go": "golang:1.20-alpine",
        "rust": "rust:1.70-slim"
    }
    return base_images.get(framework, "ubuntu:22.04")

def _get_build_commands(framework: str, service_type: str) -> list:
    """Get build commands for framework"""
    if service_type == "frontend":
        if framework in ["react", "vue", "angular"]:
            return [
                "COPY package*.json ./",
                "RUN npm ci --only=production",
                "COPY . .",
                "RUN npm run build"
            ]
    elif service_type == "backend":
        if framework in ["express"]:
            return [
                "COPY package*.json ./",
                "RUN npm ci --only=production",
                "COPY . ."
            ]
        elif framework == "spring-boot":
            return [
                "COPY build/libs/*.jar app.jar"
            ]
        elif framework in ["django", "flask"]:
            return [
                "COPY requirements.txt .",
                "RUN pip install --no-cache-dir -r requirements.txt",
                "COPY . ."
            ]
    return ["COPY . ."]
