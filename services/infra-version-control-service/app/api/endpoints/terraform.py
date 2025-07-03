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
