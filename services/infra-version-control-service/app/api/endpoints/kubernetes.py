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
