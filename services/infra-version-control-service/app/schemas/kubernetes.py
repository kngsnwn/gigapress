"""Kubernetes-related schemas"""
from typing import Dict, List, Optional, Any
from pydantic import BaseModel

class K8sResource(BaseModel):
    apiVersion: str
    kind: str
    metadata: Dict[str, Any]
    spec: Dict[str, Any]

class K8sDeploymentConfig(BaseModel):
    name: str
    namespace: str = "default"
    replicas: int = 1
    image: str
    ports: List[int] = []
    environment: Dict[str, str] = {}
    resources: Dict[str, Dict[str, str]] = {
        "limits": {"memory": "512Mi", "cpu": "500m"},
        "requests": {"memory": "256Mi", "cpu": "250m"}
    }
    labels: Dict[str, str] = {}
    annotations: Dict[str, str] = {}

class K8sServiceConfig(BaseModel):
    name: str
    namespace: str = "default"
    type: str = "ClusterIP"  # ClusterIP, NodePort, LoadBalancer
    ports: List[Dict[str, Any]] = []
    selector: Dict[str, str] = {}

class K8sIngressConfig(BaseModel):
    name: str
    namespace: str = "default"
    host: str
    paths: List[Dict[str, Any]] = []
    tls: Optional[Dict[str, Any]] = None
    annotations: Dict[str, str] = {}

class K8sManifestRequest(BaseModel):
    project_id: str
    environment: str  # dev, staging, prod
    services: List[Dict[str, Any]]
    enable_ingress: bool = True
    enable_hpa: bool = False
    enable_pvc: bool = False
