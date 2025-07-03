#!/bin/bash

# Step 4: API Endpoints Implementation
# This script creates all API endpoints

SERVICE_DIR="services/infra-version-control-service"

echo "🌐 Step 4: Creating API endpoints..."

# Create health endpoint
cat > ${SERVICE_DIR}/app/api/endpoints/health.py << 'EOF'
"""Health check endpoints"""
from fastapi import APIRouter, Depends
from app.schemas.base import ServiceHealth
from app.core.config import settings
import time

router = APIRouter()

start_time = time.time()

@router.get("/health", response_model=ServiceHealth)
async def health_check():
    """Service health check"""
    return ServiceHealth(
        service=settings.SERVICE_NAME,
        status="healthy",
        version="1.0.0",
        uptime=time.time() - start_time,
        dependencies={
            "redis": "connected",
            "kafka": "connected"
        }
    )

@router.get("/ready")
async def readiness_check():
    """Kubernetes readiness probe"""
    return {"ready": True}

@router.get("/live")
async def liveness_check():
    """Kubernetes liveness probe"""
    return {"alive": True}
EOF

# Create Docker endpoints
cat > ${SERVICE_DIR}/app/api/endpoints/docker.py << 'EOF'
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
EOF

# Create Kubernetes endpoints
cat > ${SERVICE_DIR}/app/api/endpoints/kubernetes.py << 'EOF'
"""Kubernetes-related API endpoints"""
from fastapi import APIRouter, HTTPException
from typing import List, Dict, Any
from app.schemas.base import BaseResponse
from app.schemas.kubernetes import K8sManifestRequest, K8sDeploymentConfig, K8sServiceConfig, K8sIngressConfig
from app.services.kubernetes_service import kubernetes_service
from app.services.redis_service import redis_service

router = APIRouter()

@router.post("/manifests", response_model=BaseResponse)
async def generate_k8s_manifests(request: K8sManifestRequest):
    """Generate complete Kubernetes manifests for a project"""
    try:
        manifests = []
        
        # Generate namespace
        namespace_manifest = kubernetes_service.generate_namespace(
            f"{request.project_id}-{request.environment}"
        )
        manifests.append({
            "name": "namespace.yaml",
            "content": namespace_manifest
        })
        
        # Generate manifests for each service
        for service in request.services:
            # Deployment
            deployment_config = K8sDeploymentConfig(
                name=service["name"],
                namespace=f"{request.project_id}-{request.environment}",
                replicas=service.get("replicas", 1),
                image=service["image"],
                ports=service.get("ports", []),
                environment=service.get("environment", {}),
                resources=service.get("resources", {})
            )
            deployment_manifest = kubernetes_service.generate_deployment(deployment_config)
            manifests.append({
                "name": f"{service['name']}-deployment.yaml",
                "content": deployment_manifest
            })
            
            # Service
            service_config = K8sServiceConfig(
                name=service["name"],
                namespace=f"{request.project_id}-{request.environment}",
                type=service.get("service_type", "ClusterIP"),
                ports=[{
                    "port": port,
                    "targetPort": port,
                    "protocol": "TCP"
                } for port in service.get("ports", [])],
                selector={"app": service["name"]}
            )
            service_manifest = kubernetes_service.generate_service(service_config)
            manifests.append({
                "name": f"{service['name']}-service.yaml",
                "content": service_manifest
            })
            
        # Generate Ingress if enabled
        if request.enable_ingress:
            ingress_config = K8sIngressConfig(
                name=f"{request.project_id}-ingress",
                namespace=f"{request.project_id}-{request.environment}",
                host=f"{request.project_id}.example.com",
                paths=[{
                    "path": "/",
                    "pathType": "Prefix",
                    "backend": {
                        "service": {
                            "name": request.services[0]["name"],
                            "port": {"number": request.services[0]["ports"][0]}
                        }
                    }
                }],
                annotations={
                    "kubernetes.io/ingress.class": "nginx",
                    "cert-manager.io/cluster-issuer": "letsencrypt-prod"
                }
            )
            ingress_manifest = kubernetes_service.generate_ingress(ingress_config)
            manifests.append({
                "name": "ingress.yaml",
                "content": ingress_manifest
            })
            
        # Generate HPA if enabled
        if request.enable_hpa:
            for service in request.services:
                hpa_manifest = kubernetes_service.generate_hpa(
                    name=f"{service['name']}-hpa",
                    namespace=f"{request.project_id}-{request.environment}",
                    deployment=service["name"],
                    min_replicas=1,
                    max_replicas=10,
                    target_cpu=80
                )
                manifests.append({
                    "name": f"{service['name']}-hpa.yaml",
                    "content": hpa_manifest
                })
                
        # Generate kustomization.yaml
        kustomization = kubernetes_service.generate_kustomization(
            [manifest["name"] for manifest in manifests]
        )
        manifests.append({
            "name": "kustomization.yaml",
            "content": kustomization
        })
        
        # Cache the result
        cache_key = f"k8s-manifests:{request.project_id}:{request.environment}"
        await redis_service.set(cache_key, manifests)
        
        return BaseResponse(
            success=True,
            message="Kubernetes manifests generated successfully",
            data={
                "manifests": manifests,
                "total_files": len(manifests)
            }
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/configmap", response_model=BaseResponse)
async def generate_configmap(project_id: str, name: str, data: Dict[str, str]):
    """Generate ConfigMap manifest"""
    try:
        configmap_manifest = kubernetes_service.generate_configmap(
            name=name,
            namespace=project_id,
            data=data
        )
        
        return BaseResponse(
            success=True,
            message="ConfigMap generated successfully",
            data={
                "configmap": configmap_manifest,
                "name": name
            }
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/secret", response_model=BaseResponse)
async def generate_secret(project_id: str, name: str, data: Dict[str, str]):
    """Generate Secret manifest"""
    try:
        secret_manifest = kubernetes_service.generate_secret(
            name=name,
            namespace=project_id,
            data=data
        )
        
        return BaseResponse(
            success=True,
            message="Secret generated successfully",
            data={
                "secret": secret_manifest,
                "name": name
            }
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
EOF

# Create CI/CD endpoints
cat > ${SERVICE_DIR}/app/api/endpoints/cicd.py << 'EOF'
"""CI/CD pipeline API endpoints"""
from fastapi import APIRouter, HTTPException
from app.schemas.base import BaseResponse
from app.schemas.cicd import CICDRequest, GitHubActionsConfig, JenkinsConfig, GitLabCIConfig
from app.services.cicd_service import cicd_service
from app.services.redis_service import redis_service

router = APIRouter()

@router.post("/pipeline", response_model=BaseResponse)
async def generate_cicd_pipeline(request: CICDRequest):
    """Generate CI/CD pipeline configuration"""
    try:
        pipeline_content = ""
        filename = ""
        
        if request.pipeline_type == "github-actions":
            # Generate GitHub Actions workflow
            workflow_jobs = cicd_service.generate_build_workflow(
                request.build_steps[0].get("type", "backend"),
                request.build_steps[0].get("framework", "express")
            )
            
            config = GitHubActionsConfig(
                name=f"{request.project_id} CI/CD",
                triggers={
                    "push": {"branches": ["main", "develop"]},
                    "pull_request": {"branches": ["main"]}
                },
                jobs=workflow_jobs
            )
            
            pipeline_content = cicd_service.generate_github_actions(config)
            filename = ".github/workflows/main.yml"
            
        elif request.pipeline_type == "jenkins":
            # Generate Jenkinsfile
            stages = []
            for step in request.build_steps:
                stages.append({
                    "name": step.get("name", "Build"),
                    "steps": step.get("commands", [])
                })
                
            config = JenkinsConfig(
                pipeline_name=request.project_id,
                stages=stages,
                environment={"PROJECT_ID": request.project_id}
            )
            
            pipeline_content = cicd_service.generate_jenkins_pipeline(config)
            filename = "Jenkinsfile"
            
        elif request.pipeline_type == "gitlab-ci":
            # Generate GitLab CI
            jobs = {}
            for i, step in enumerate(request.build_steps):
                jobs[f"build-{i}"] = {
                    "stage": "build",
                    "script": step.get("commands", [])
                }
                
            config = GitLabCIConfig(
                stages=["build", "test", "deploy"],
                variables={"PROJECT_ID": request.project_id},
                jobs=jobs
            )
            
            pipeline_content = cicd_service.generate_gitlab_ci(config)
            filename = ".gitlab-ci.yml"
            
        # Cache the result
        cache_key = f"cicd:{request.project_id}:{request.pipeline_type}"
        await redis_service.set(cache_key, pipeline_content)
        
        return BaseResponse(
            success=True,
            message=f"{request.pipeline_type} pipeline generated successfully",
            data={
                "pipeline": pipeline_content,
                "filename": filename,
                "type": request.pipeline_type
            }
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/templates/{pipeline_type}", response_model=BaseResponse)
async def get_pipeline_templates(pipeline_type: str):
    """Get available pipeline templates"""
    try:
        templates = {
            "github-actions": [
                "node-build-deploy",
                "java-gradle-build",
                "python-test-deploy",
                "docker-build-push"
            ],
            "jenkins": [
                "declarative-pipeline",
                "scripted-pipeline",
                "multibranch-pipeline"
            ],
            "gitlab-ci": [
                "docker-build",
                "kubernetes-deploy",
                "terraform-apply"
            ]
        }
        
        return BaseResponse(
            success=True,
            message="Templates retrieved successfully",
            data={
                "templates": templates.get(pipeline_type, []),
                "type": pipeline_type
            }
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
EOF

# Create Git endpoints
cat > ${SERVICE_DIR}/app/api/endpoints/git.py << 'EOF'
"""Git version control API endpoints"""
from fastapi import APIRouter, HTTPException
from app.schemas.base import BaseResponse
from app.schemas.git import GitInitRequest, GitCommit, GitBranch, GitRepository
from app.services.git_service import git_service
from app.services.kafka_service import kafka_service

router = APIRouter()

@router.post("/init", response_model=BaseResponse)
async def initialize_repository(request: GitInitRequest):
    """Initialize a new Git repository"""
    try:
        # Create repository
        repo = GitRepository(
            project_id=request.project_id,
            repo_name=request.repo_name,
            default_branch="main"
        )
        
        success = git_service.init_repository(repo)
        if not success:
            raise HTTPException(status_code=500, detail="Failed to initialize repository")
            
        # Create .gitignore
        if request.gitignore_template:
            git_service.create_gitignore(
                request.project_id,
                request.repo_name,
                request.gitignore_template
            )
            
        # Create README.md
        if request.include_readme:
            git_service.create_readme(
                request.project_id,
                request.repo_name,
                {
                    "project_name": request.repo_name,
                    "description": f"Repository for {request.repo_name}",
                    "license": request.license
                }
            )
            
        # Initial commit
        commit = GitCommit(
            message=request.initial_commit_message,
            files=["."]
        )
        git_service.commit(request.project_id, request.repo_name, commit)
        
        # Publish event
        await kafka_service.publish_event("git-events", {
            "event": "repository_initialized",
            "project_id": request.project_id,
            "repo_name": request.repo_name
        })
        
        return BaseResponse(
            success=True,
            message="Repository initialized successfully",
            data={
                "project_id": request.project_id,
                "repo_name": request.repo_name,
                "default_branch": "main"
            }
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/commit", response_model=BaseResponse)
async def create_commit(project_id: str, repo_name: str, commit: GitCommit):
    """Create a new commit"""
    try:
        success = git_service.commit(project_id, repo_name, commit)
        if not success:
            raise HTTPException(status_code=500, detail="Failed to create commit")
            
        # Publish event
        await kafka_service.publish_event("git-events", {
            "event": "commit_created",
            "project_id": project_id,
            "repo_name": repo_name,
            "message": commit.message
        })
        
        return BaseResponse(
            success=True,
            message="Commit created successfully",
            data={
                "message": commit.message,
                "files": commit.files
            }
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/branch", response_model=BaseResponse)
async def create_branch(project_id: str, repo_name: str, branch: GitBranch):
    """Create a new branch"""
    try:
        success = git_service.create_branch(project_id, repo_name, branch)
        if not success:
            raise HTTPException(status_code=500, detail="Failed to create branch")
            
        return BaseResponse(
            success=True,
            message="Branch created successfully",
            data={
                "branch_name": branch.name,
                "from_branch": branch.from_branch
            }
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/branches/{project_id}/{repo_name}", response_model=BaseResponse)
async def list_branches(project_id: str, repo_name: str):
    """List all branches in a repository"""
    try:
        branches = git_service.get_branches(project_id, repo_name)
        
        return BaseResponse(
            success=True,
            message="Branches retrieved successfully",
            data={
                "branches": branches,
                "total": len(branches)
            }
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/workflow", response_model=BaseResponse)
async def generate_git_workflow(project_id: str, workflow_type: str = "gitflow"):
    """Generate Git workflow configuration"""
    try:
        workflows = {
            "gitflow": {
                "branches": {
                    "main": "Production-ready code",
                    "develop": "Integration branch",
                    "feature/*": "New features",
                    "release/*": "Release preparation",
                    "hotfix/*": "Emergency fixes"
                },
                "rules": [
                    "Feature branches merge into develop",
                    "Release branches merge into main and develop",
                    "Hotfix branches merge into main and develop"
                ]
            },
            "github-flow": {
                "branches": {
                    "main": "Production-ready code",
                    "feature/*": "All changes"
                },
                "rules": [
                    "All changes through feature branches",
                    "Pull requests for code review",
                    "Deploy from main"
                ]
            }
        }
        
        return BaseResponse(
            success=True,
            message="Git workflow configuration generated",
            data={
                "workflow": workflows.get(workflow_type, workflows["gitflow"]),
                "type": workflow_type
            }
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
EOF

# Create Terraform endpoints
cat > ${SERVICE_DIR}/app/api/endpoints/terraform.py << 'EOF'
"""Terraform/IaC API endpoints"""
from fastapi import APIRouter, HTTPException
from typing import List, Dict, Any
from app.schemas.base import BaseResponse
from app.schemas.terraform import IaCRequest, TerraformProvider, TerraformResource
from app.services.terraform_service import terraform_service
from app.services.redis_service import redis_service

router = APIRouter()

@router.post("/generate", response_model=BaseResponse)
async def generate_terraform_config(request: IaCRequest):
    """Generate Terraform configuration files"""
    try:
        # Set up providers
        providers = []
        if request.cloud_provider == "aws":
            providers.append(TerraformProvider(
                name="aws",
                version="~> 5.0",
                configuration={"region": request.regions[0] if request.regions else "us-east-1"}
            ))
        elif request.cloud_provider == "gcp":
            providers.append(TerraformProvider(
                name="google",
                version="~> 5.0",
                configuration={"project": request.project_id, "region": request.regions[0] if request.regions else "us-central1"}
            ))
        elif request.cloud_provider == "azure":
            providers.append(TerraformProvider(
                name="azurerm",
                version="~> 3.0",
                configuration={"features": {}}
            ))
            
        # Generate resources based on infrastructure type
        resources = []
        if request.infrastructure_type == "kubernetes":
            if request.cloud_provider == "aws":
                resources = terraform_service.generate_aws_resources(request.dict())
            elif request.cloud_provider == "gcp":
                resources = terraform_service.generate_gcp_resources(request.dict())
            elif request.cloud_provider == "azure":
                resources = terraform_service.generate_azure_resources(request.dict())
                
        # Generate Terraform files
        main_tf = terraform_service.generate_main_tf(providers, resources)
        
        variables_tf = terraform_service.generate_variables_tf([
            {
                "name": "project_id",
                "type": "string",
                "default": request.project_id,
                "description": "Project identifier"
            },
            {
                "name": "environment",
                "type": "string",
                "default": "dev",
                "description": "Environment name"
            }
        ])
        
        outputs_tf = terraform_service.generate_outputs_tf([
            {
                "name": "cluster_endpoint",
                "value": "${module.kubernetes.cluster_endpoint}",
                "description": "Kubernetes cluster endpoint"
            }
        ])
        
        files = [
            {"name": "main.tf", "content": main_tf},
            {"name": "variables.tf", "content": variables_tf},
            {"name": "outputs.tf", "content": outputs_tf}
        ]
        
        # Cache the result
        cache_key = f"terraform:{request.project_id}:{request.cloud_provider}"
        await redis_service.set(cache_key, files)
        
        return BaseResponse(
            success=True,
            message="Terraform configuration generated successfully",
            data={
                "files": files,
                "cloud_provider": request.cloud_provider,
                "infrastructure_type": request.infrastructure_type
            }
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/modules/{cloud_provider}", response_model=BaseResponse)
async def get_terraform_modules(cloud_provider: str):
    """Get available Terraform modules for cloud provider"""
    try:
        modules = {
            "aws": [
                "vpc", "eks", "rds", "s3", "lambda", "api-gateway",
                "cloudfront", "elasticache", "ecs", "fargate"
            ],
            "gcp": [
                "vpc", "gke", "cloud-sql", "gcs", "cloud-functions",
                "cloud-run", "pub-sub", "firestore"
            ],
            "azure": [
                "vnet", "aks", "sql-database", "storage", "functions",
                "app-service", "cosmos-db", "service-bus"
            ]
        }
        
        return BaseResponse(
            success=True,
            message="Terraform modules retrieved successfully",
            data={
                "modules": modules.get(cloud_provider, []),
                "provider": cloud_provider
            }
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
EOF

# Create Monitoring endpoints
cat > ${SERVICE_DIR}/app/api/endpoints/monitoring.py << 'EOF'
"""Monitoring configuration API endpoints"""
from fastapi import APIRouter, HTTPException
from typing import List, Dict, Any
from app.schemas.base import BaseResponse
from app.schemas.monitoring import MonitoringRequest, PrometheusConfig, GrafanaDashboard, AlertRule
from app.services.monitoring_service import monitoring_service
from app.services.redis_service import redis_service

router = APIRouter()

@router.post("/setup", response_model=BaseResponse)
async def setup_monitoring(request: MonitoringRequest):
    """Generate complete monitoring setup"""
    try:
        monitoring_files = []
        
        # Generate Prometheus configuration
        if "prometheus" in request.monitoring_stack:
            scrape_configs = monitoring_service.generate_default_scrape_configs(
                request.metrics_endpoints
            )
            
            prometheus_config = PrometheusConfig(
                scrape_configs=scrape_configs
            )
            
            prometheus_yaml = monitoring_service.generate_prometheus_config(prometheus_config)
            monitoring_files.append({
                "name": "prometheus.yml",
                "content": prometheus_yaml
            })
            
            # Generate alert rules
            alert_rules = [
                AlertRule(
                    name="HighCPUUsage",
                    expression='rate(process_cpu_seconds_total[5m]) > 0.8',
                    duration="5m",
                    labels={"severity": "warning"},
                    annotations={
                        "summary": "High CPU usage detected",
                        "description": "CPU usage is above 80% for more than 5 minutes"
                    }
                ),
                AlertRule(
                    name="HighMemoryUsage",
                    expression='(1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) > 0.9',
                    duration="5m",
                    labels={"severity": "critical"},
                    annotations={
                        "summary": "High memory usage detected",
                        "description": "Memory usage is above 90% for more than 5 minutes"
                    }
                )
            ]
            
            alert_rules_yaml = monitoring_service.generate_alert_rules(alert_rules)
            monitoring_files.append({
                "name": "alerts.yml",
                "content": alert_rules_yaml
            })
            
        # Generate Grafana dashboards
        if "grafana" in request.monitoring_stack:
            dashboard = GrafanaDashboard(
                title=f"{request.project_id} Overview",
                panels=[
                    {
                        "title": "CPU Usage",
                        "type": "graph",
                        "targets": [{"expr": "rate(process_cpu_seconds_total[5m])"}]
                    },
                    {
                        "title": "Memory Usage",
                        "type": "graph",
                        "targets": [{"expr": "process_resident_memory_bytes"}]
                    },
                    {
                        "title": "HTTP Request Rate",
                        "type": "graph",
                        "targets": [{"expr": "rate(http_requests_total[5m])"}]
                    }
                ]
            )
            
            dashboard_json = monitoring_service.generate_grafana_dashboard(dashboard)
            monitoring_files.append({
                "name": "dashboard.json",
                "content": dashboard_json
            })
            
        # Generate logging configuration
        if request.log_aggregation:
            logging_config = monitoring_service.generate_logging_config("fluentd")
            monitoring_files.append({
                "name": "fluentd.conf",
                "content": logging_config
            })
            
        # Cache the result
        cache_key = f"monitoring:{request.project_id}"
        await redis_service.set(cache_key, monitoring_files)
        
        return BaseResponse(
            success=True,
            message="Monitoring configuration generated successfully",
            data={
                "files": monitoring_files,
                "stack": request.monitoring_stack,
                "features": {
                    "metrics": True,
                    "logging": request.log_aggregation,
                    "tracing": request.tracing,
                    "alerting": len(request.alerting_channels) > 0
                }
            }
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/metrics/endpoints", response_model=BaseResponse)
async def get_metrics_endpoints():
    """Get recommended metrics endpoints for services"""
    try:
        endpoints = {
            "spring-boot": "/actuator/prometheus",
            "express": "/metrics",
            "django": "/metrics",
            "go": "/metrics",
            "default": "/metrics"
        }
        
        return BaseResponse(
            success=True,
            message="Metrics endpoints retrieved successfully",
            data={"endpoints": endpoints}
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
EOF

echo "✅ Step 4 Complete: API endpoints created"
echo "🌐 Created endpoints:"
echo "   - Health check endpoints"
echo "   - Docker API endpoints"
echo "   - Kubernetes API endpoints"
echo "   - CI/CD API endpoints"
echo "   - Git API endpoints"
echo "   - Terraform API endpoints"
echo "   - Monitoring API endpoints"