#!/bin/bash

# Step 6: Integration Code and Final Setup
# This script creates integration services and final configurations

SERVICE_DIR="services/infra-version-control-service"

echo "🔌 Step 6: Creating integration code and final setup..."

# Create integration service
cat > ${SERVICE_DIR}/app/services/integration_service.py << 'EOF'
"""Integration service for communicating with other GigaPress services"""
import httpx
from typing import Dict, Any, Optional
from app.core.config import settings
from app.core.logging import setup_logging

logger = setup_logging()

class IntegrationService:
    def __init__(self):
        self.client = httpx.AsyncClient(timeout=30.0)
        
    async def get_project_info(self, project_id: str) -> Optional[Dict[str, Any]]:
        """Get project information from MCP Server"""
        try:
            response = await self.client.get(
                f"{settings.MCP_SERVER_URL}/api/v1/projects/{project_id}"
            )
            if response.status_code == 200:
                return response.json()
            return None
        except Exception as e:
            logger.error(f"Failed to get project info: {e}")
            return None
            
    async def get_domain_schema(self, project_id: str) -> Optional[Dict[str, Any]]:
        """Get domain schema from Domain/Schema Service"""
        try:
            response = await self.client.get(
                f"{settings.DOMAIN_SCHEMA_SERVICE_URL}/api/v1/schema/{project_id}"
            )
            if response.status_code == 200:
                return response.json()
            return None
        except Exception as e:
            logger.error(f"Failed to get domain schema: {e}")
            return None
            
    async def get_backend_config(self, project_id: str) -> Optional[Dict[str, Any]]:
        """Get backend configuration from Backend Service"""
        try:
            response = await self.client.get(
                f"{settings.BACKEND_SERVICE_URL}/api/v1/config/{project_id}"
            )
            if response.status_code == 200:
                return response.json()
            return None
        except Exception as e:
            logger.error(f"Failed to get backend config: {e}")
            return None
            
    async def get_frontend_config(self, project_id: str) -> Optional[Dict[str, Any]]:
        """Get frontend configuration from Design/Frontend Service"""
        try:
            response = await self.client.get(
                f"{settings.DESIGN_FRONTEND_SERVICE_URL}/api/v1/config/{project_id}"
            )
            if response.status_code == 200:
                return response.json()
            return None
        except Exception as e:
            logger.error(f"Failed to get frontend config: {e}")
            return None
            
    async def notify_infra_ready(self, project_id: str, infra_type: str) -> bool:
        """Notify MCP Server that infrastructure is ready"""
        try:
            response = await self.client.post(
                f"{settings.MCP_SERVER_URL}/api/v1/notifications",
                json={
                    "project_id": project_id,
                    "event": "infra_ready",
                    "type": infra_type,
                    "service": settings.SERVICE_NAME
                }
            )
            return response.status_code == 200
        except Exception as e:
            logger.error(f"Failed to notify infra ready: {e}")
            return False
            
    async def close(self):
        """Close HTTP client"""
        await self.client.aclose()

integration_service = IntegrationService()
EOF

# Create orchestration endpoint
cat > ${SERVICE_DIR}/app/api/endpoints/orchestration.py << 'EOF'
"""Orchestration endpoint for complete infrastructure setup"""
from fastapi import APIRouter, HTTPException, BackgroundTasks
from typing import Dict, Any
from app.schemas.base import BaseResponse
from app.schemas.docker import DockerImageConfig, DockerComposeService, DockerComposeConfig
from app.schemas.kubernetes import K8sDeploymentConfig
from app.schemas.monitoring import PrometheusConfig
from app.schemas.terraform import TerraformProvider
from app.schemas.git import GitRepository, GitCommit
from app.services.integration_service import integration_service
from app.services.docker_service import docker_service
from app.services.kubernetes_service import kubernetes_service
from app.services.git_service import git_service
from app.services.cicd_service import cicd_service
from app.services.terraform_service import terraform_service
from app.services.monitoring_service import monitoring_service
from app.models.project import InfrastructureConfig, GenerationStatus
from app.core.logging import setup_logging
from datetime import datetime

router = APIRouter()
logger = setup_logging()

@router.post("/generate-complete-infra", response_model=BaseResponse)
async def generate_complete_infrastructure(
    project_id: str,
    background_tasks: BackgroundTasks
):
    """Generate complete infrastructure for a project"""
    try:
        # Get project information from other services
        project_info = await integration_service.get_project_info(project_id)
        if not project_info:
            raise HTTPException(status_code=404, detail="Project not found")
            
        # Create generation status
        status = GenerationStatus(
            project_id=project_id,
            status="in_progress",
            current_step="Initializing",
            total_steps=8,
            progress_percentage=0.0,
            started_at=datetime.now()
        )
        
        # Start background generation
        background_tasks.add_task(
            generate_infrastructure_async,
            project_id,
            project_info,
            status
        )
        
        return BaseResponse(
            success=True,
            message="Infrastructure generation started",
            data={
                "project_id": project_id,
                "status": "in_progress",
                "message": "Check /status endpoint for progress"
            }
        )
        
    except Exception as e:
        logger.error(f"Failed to start infrastructure generation: {e}")
        raise HTTPException(status_code=500, detail=str(e))

async def generate_infrastructure_async(
    project_id: str,
    project_info: Dict[str, Any],
    status: GenerationStatus
):
    """Generate infrastructure asynchronously"""
    try:
        infra_config = InfrastructureConfig(project_id=project_id)
        
        # Step 1: Initialize Git repository
        status.current_step = "Initializing Git repository"
        status.progress_percentage = 12.5
        logger.info(f"Step 1: {status.current_step}")
        
        git_repo = GitRepository(
            project_id=project_id,
            repo_name=project_info.get("name", project_id)
        )
        git_service.init_repository(git_repo)
        git_service.create_gitignore(project_id, git_repo.repo_name, "node")
        git_service.create_readme(project_id, git_repo.repo_name, {
            "project_name": project_info.get("name"),
            "description": project_info.get("description", ""),
            "license": "MIT"
        })
        
        # Step 2: Generate Docker configurations
        status.current_step = "Generating Docker configurations"
        status.progress_percentage = 25.0
        logger.info(f"Step 2: {status.current_step}")
        
        # Get service configurations
        backend_config = await integration_service.get_backend_config(project_id)
        frontend_config = await integration_service.get_frontend_config(project_id)
        
        # Generate Dockerfiles for each service
        if backend_config:
            dockerfile = docker_service.generate_dockerfile(DockerImageConfig(
                base_image="node:18-alpine",
                workdir="/app",
                exposed_ports=[backend_config.get("port", 3000)],
                environment=backend_config.get("env_vars", {})
            ))
            infra_config.docker["backend"] = dockerfile
            
        if frontend_config:
            dockerfile = docker_service.generate_dockerfile(DockerImageConfig(
                base_image="node:18-alpine",
                workdir="/app",
                exposed_ports=[frontend_config.get("port", 3001)],
                environment=frontend_config.get("env_vars", {})
            ))
            infra_config.docker["frontend"] = dockerfile
            
        # Step 3: Generate Kubernetes manifests
        status.current_step = "Generating Kubernetes manifests"
        status.progress_percentage = 37.5
        logger.info(f"Step 3: {status.current_step}")
        
        # Generate K8s manifests
        k8s_manifests = []
        
        if backend_config:
            deployment = kubernetes_service.generate_deployment(K8sDeploymentConfig(
                name=f"{project_id}-backend",
                namespace=project_id,
                image=f"{project_id}-backend:latest",
                ports=[backend_config.get("port", 3000)]
            ))
            k8s_manifests.append({"name": "backend-deployment.yaml", "content": deployment})
            
        if frontend_config:
            deployment = kubernetes_service.generate_deployment(K8sDeploymentConfig(
                name=f"{project_id}-frontend",
                namespace=project_id,
                image=f"{project_id}-frontend:latest",
                ports=[frontend_config.get("port", 3001)]
            ))
            k8s_manifests.append({"name": "frontend-deployment.yaml", "content": deployment})
            
        infra_config.kubernetes["manifests"] = k8s_manifests
        
        # Step 4: Generate CI/CD pipelines
        status.current_step = "Generating CI/CD pipelines"
        status.progress_percentage = 50.0
        logger.info(f"Step 4: {status.current_step}")
        
        # Generate GitHub Actions workflow
        workflow = cicd_service.generate_github_actions(GitHubActionsConfig(
            name=f"{project_id} CI/CD",
            triggers={
                "push": {"branches": ["main"]},
                "pull_request": {"branches": ["main"]}
            },
            jobs=cicd_service.generate_build_workflow("backend", "express")
        ))
        infra_config.cicd["github_actions"] = workflow
        
        # Step 5: Generate Terraform configuration
        status.current_step = "Generating Terraform configuration"
        status.progress_percentage = 62.5
        logger.info(f"Step 5: {status.current_step}")
        
        providers = [TerraformProvider(name="aws", version="~> 5.0")]
        resources = terraform_service.generate_aws_resources({"vpc": {"cidr": "10.0.0.0/16"}})
        
        main_tf = terraform_service.generate_main_tf(providers, resources)
        infra_config.terraform = {"main.tf": main_tf}
        
        # Step 6: Generate monitoring configuration
        status.current_step = "Generating monitoring configuration"
        status.progress_percentage = 75.0
        logger.info(f"Step 6: {status.current_step}")
        
        prometheus_config = monitoring_service.generate_prometheus_config(
            PrometheusConfig(
                scrape_configs=monitoring_service.generate_default_scrape_configs(
                    [f"{project_id}-backend", f"{project_id}-frontend"]
                )
            )
        )
        infra_config.monitoring["prometheus"] = prometheus_config
        
        # Step 7: Commit all configurations
        status.current_step = "Committing configurations"
        status.progress_percentage = 87.5
        logger.info(f"Step 7: {status.current_step}")
        
        git_service.commit(project_id, git_repo.repo_name, GitCommit(
            message="Add infrastructure configurations",
            files=["."]
        ))
        
        # Step 8: Notify completion
        status.current_step = "Finalizing"
        status.progress_percentage = 100.0
        status.status = "completed"
        status.completed_at = datetime.now()
        logger.info(f"Step 8: {status.current_step}")
        
        await integration_service.notify_infra_ready(project_id, "complete")
        
        logger.info(f"Infrastructure generation completed for project {project_id}")
        
    except Exception as e:
        logger.error(f"Infrastructure generation failed: {e}")
        status.status = "failed"
        status.errors.append(str(e))
        status.completed_at = datetime.now()

@router.get("/status/{project_id}", response_model=BaseResponse)
async def get_generation_status(project_id: str):
    """Get infrastructure generation status"""
    # This would retrieve actual status from storage
    return BaseResponse(
        success=True,
        message="Status retrieved",
        data={
            "project_id": project_id,
            "status": "completed",
            "progress": 100.0
        }
    )
EOF

# Update main.py to include orchestration endpoint
cat >> ${SERVICE_DIR}/main.py << 'EOF'

# Import orchestration endpoint
from app.api.endpoints import orchestration

# Add orchestration router
app.include_router(orchestration.router, prefix="/api/v1/orchestration", tags=["orchestration"])
EOF

# Create Dockerfile for the service
cat > ${SERVICE_DIR}/Dockerfile << 'EOF'
FROM python:3.10-slim

WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y \
    git \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy application
COPY . .

# Create necessary directories
RUN mkdir -p logs repositories

# Expose port
EXPOSE 8086

# Run the application
CMD ["python", "main.py"]
EOF

# Create docker-compose entry
cat > ${SERVICE_DIR}/docker-compose.yml << 'EOF'
version: '3.8'

services:
  infra-version-control-service:
    build: .
    container_name: gigapress-infra-version-control
    ports:
      - "8086:8086"
    environment:
      - SERVICE_NAME=infra-version-control-service
      - SERVICE_PORT=8086
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - REDIS_PASSWORD=redis123
      - KAFKA_BOOTSTRAP_SERVERS=kafka:29092
    volumes:
      - ./logs:/app/logs
      - ./repositories:/app/repositories
    depends_on:
      - redis
      - kafka
    networks:
      - gigapress-network

networks:
  gigapress-network:
    external: true
EOF

# Create startup script
cat > ${SERVICE_DIR}/start.sh << 'EOF'
#!/bin/bash
echo "Starting Infra/Version Control Service..."
python main.py
EOF

chmod +x ${SERVICE_DIR}/start.sh

# Create test file
cat > ${SERVICE_DIR}/tests/test_main.py << 'EOF'
"""Basic tests for Infra/Version Control Service"""
import pytest
from fastapi.testclient import TestClient
from main import app

client = TestClient(app)

def test_health_check():
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "healthy"

def test_docker_dockerfile_generation():
    response = client.post("/api/v1/docker/dockerfile", json={
        "project_id": "test-project",
        "service_name": "test-service",
        "service_type": "backend",
        "framework": "express",
        "ports": [3000]
    })
    assert response.status_code == 200
    assert "dockerfile" in response.json()["data"]

def test_kubernetes_manifests_generation():
    response = client.post("/api/v1/kubernetes/manifests", json={
        "project_id": "test-project",
        "environment": "dev",
        "services": [{
            "name": "test-service",
            "image": "test-image:latest",
            "ports": [8080]
        }]
    })
    assert response.status_code == 200
    assert "manifests" in response.json()["data"]

def test_cicd_pipeline_generation():
    response = client.post("/api/v1/cicd/pipeline", json={
        "project_id": "test-project",
        "pipeline_type": "github-actions",
        "build_steps": [{
            "type": "backend",
            "framework": "express"
        }]
    })
    assert response.status_code == 200
    assert "pipeline" in response.json()["data"]

def test_git_init():
    response = client.post("/api/v1/git/init", json={
        "project_id": "test-project",
        "repo_name": "test-repo",
        "include_readme": True,
        "gitignore_template": "node",
        "license": "MIT",
        "initial_commit_message": "Initial commit"
    })
    assert response.status_code == 200
    assert response.json()["success"] == True

def test_terraform_generation():
    response = client.post("/api/v1/terraform/generate", json={
        "project_id": "test-project",
        "cloud_provider": "aws",
        "infrastructure_type": "kubernetes",
        "regions": ["us-east-1"]
    })
    assert response.status_code == 200
    assert "files" in response.json()["data"]

def test_monitoring_setup():
    response = client.post("/api/v1/monitoring/setup", json={
        "project_id": "test-project",
        "monitoring_stack": ["prometheus", "grafana"],
        "metrics_endpoints": ["/metrics"],
        "log_aggregation": True,
        "tracing": False,
        "alerting_channels": ["email"]
    })
    assert response.status_code == 200
    assert "files" in response.json()["data"]
EOF

# Create README for the service
cat > ${SERVICE_DIR}/README.md << 'EOF'
# Infra/Version Control Service

Infrastructure and Version Control Service for GigaPress - handles Docker, Kubernetes, CI/CD, Git operations.

## Features

- **Docker Configuration**: Generate Dockerfiles and docker-compose.yml
- **Kubernetes Manifests**: Create K8s deployments, services, ingress
- **CI/CD Pipelines**: GitHub Actions, Jenkins, GitLab CI
- **Git Operations**: Repository management, branching, commits
- **Terraform/IaC**: Infrastructure as Code generation
- **Monitoring Setup**: Prometheus, Grafana configurations

## API Endpoints

### Docker
- `POST /api/v1/docker/dockerfile` - Generate Dockerfile
- `POST /api/v1/docker/docker-compose` - Generate docker-compose.yml
- `POST /api/v1/docker/dockerignore` - Generate .dockerignore

### Kubernetes
- `POST /api/v1/kubernetes/manifests` - Generate K8s manifests
- `POST /api/v1/kubernetes/configmap` - Generate ConfigMap
- `POST /api/v1/kubernetes/secret` - Generate Secret

### CI/CD
- `POST /api/v1/cicd/pipeline` - Generate CI/CD pipeline
- `GET /api/v1/cicd/templates/{type}` - Get pipeline templates

### Git
- `POST /api/v1/git/init` - Initialize repository
- `POST /api/v1/git/commit` - Create commit
- `POST /api/v1/git/branch` - Create branch
- `GET /api/v1/git/branches/{project_id}/{repo_name}` - List branches

### Terraform
- `POST /api/v1/terraform/generate` - Generate Terraform config
- `GET /api/v1/terraform/modules/{provider}` - Get available modules

### Monitoring
- `POST /api/v1/monitoring/setup` - Generate monitoring setup
- `GET /api/v1/monitoring/metrics/endpoints` - Get metrics endpoints

### Orchestration
- `POST /api/v1/orchestration/generate-complete-infra` - Generate complete infrastructure
- `GET /api/v1/orchestration/status/{project_id}` - Get generation status

## Running the Service

```bash
# Install dependencies
pip install -r requirements.txt

# Run locally
python main.py

# Run with Docker
docker build -t infra-version-control-service .
docker run -p 8086:8086 infra-version-control-service

# Run with Docker Compose
docker-compose up -d
```

## Environment Variables

- `SERVICE_PORT`: Service port (default: 8086)
- `REDIS_HOST`: Redis host
- `REDIS_PORT`: Redis port
- `REDIS_PASSWORD`: Redis password
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka servers
- `MCP_SERVER_URL`: MCP Server URL
- `DOMAIN_SCHEMA_SERVICE_URL`: Domain/Schema Service URL
- `BACKEND_SERVICE_URL`: Backend Service URL
- `DESIGN_FRONTEND_SERVICE_URL`: Design/Frontend Service URL

## Testing

```bash
# Run tests
pytest tests/

# Run with coverage
pytest --cov=app tests/
```

## Project Structure

```
infra-version-control-service/
├── app/
│   ├── api/
│   │   └── endpoints/      # API endpoints
│   ├── core/              # Core configuration
│   ├── models/            # Data models
│   ├── schemas/           # Pydantic schemas
│   ├── services/          # Business logic
│   └── templates/         # Jinja2 templates
├── tests/                 # Test files
├── logs/                  # Log files
├── repositories/          # Git repositories
├── requirements.txt       # Python dependencies
├── Dockerfile            # Docker configuration
├── docker-compose.yml    # Docker Compose config
└── README.md            # This file
```

## API Documentation

When the service is running, you can access:
- Swagger UI: http://localhost:8086/docs
- ReDoc: http://localhost:8086/redoc

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is part of the GigaPress system.
EOF

# Create .gitignore for the service
cat > ${SERVICE_DIR}/.gitignore << 'EOF'
# Python
__pycache__/
*.py[cod]
*$py.class
*.so
.Python
build/
develop-eggs/
dist/
downloads/
eggs/
.eggs/
lib/
lib64/
parts/
sdist/
var/
wheels/
*.egg-info/
.installed.cfg
*.egg

# Virtual Environment
venv/
ENV/
env/

# IDE
.idea/
.vscode/
*.swp
*.swo

# Logs
logs/
*.log

# Environment
.env
.env.local

# Git repositories
repositories/

# Testing
.coverage
.pytest_cache/
htmlcov/

# OS
.DS_Store
Thumbs.db
EOF

echo ""
echo "✅ Step 6 Complete: Integration code and final setup created"
echo ""
echo "🎉 ALL STEPS COMPLETE! Infra/Version Control Service is ready!"
echo ""
echo "📁 Service created at: ${SERVICE_DIR}"
echo ""
echo "📋 Total files created:"
find ${SERVICE_DIR} -type f | wc -l
echo ""
echo "🚀 To run the service:"
echo "  cd ${SERVICE_DIR}"
echo "  pip install -r requirements.txt"
echo "  python main.py"
echo ""
echo "🐳 To run with Docker:"
echo "  cd ${SERVICE_DIR}"
echo "  docker build -t infra-version-control-service ."
echo "  docker run -p 8086:8086 infra-version-control-service"
echo ""
echo "📚 API Documentation will be available at:"
echo "  - Swagger UI: http://localhost:8086/docs"
echo "  - ReDoc: http://localhost:8086/redoc"
echo ""
echo "✨ Service endpoints:"
echo "  - Health: http://localhost:8086/health"
echo "  - Docker: http://localhost:8086/api/v1/docker/*"
echo "  - Kubernetes: http://localhost:8086/api/v1/kubernetes/*"
echo "  - CI/CD: http://localhost:8086/api/v1/cicd/*"
echo "  - Git: http://localhost:8086/api/v1/git/*"
echo "  - Terraform: http://localhost:8086/api/v1/terraform/*"
echo "  - Monitoring: http://localhost:8086/api/v1/monitoring/*"
echo "  - Orchestration: http://localhost:8086/api/v1/orchestration/*"