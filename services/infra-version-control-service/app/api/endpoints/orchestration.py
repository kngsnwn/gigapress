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
