"""Kubernetes service for generating K8s manifests"""
from typing import Dict, Any, List
import yaml
from kubernetes import client, config
from app.services.template_service import template_service
from app.schemas.kubernetes import K8sDeploymentConfig, K8sServiceConfig, K8sIngressConfig
from app.core.logging import setup_logging

logger = setup_logging()

class KubernetesService:
    def __init__(self):
        try:
            config.load_incluster_config()
        except:
            try:
                config.load_kube_config()
            except:
                logger.warning("Kubernetes configuration not found")
                
    def generate_deployment(self, config: K8sDeploymentConfig) -> str:
        """Generate Kubernetes Deployment manifest"""
        context = {
            "name": config.name,
            "namespace": config.namespace,
            "replicas": config.replicas,
            "image": config.image,
            "ports": config.ports,
            "env_vars": config.environment,
            "resources": config.resources,
            "labels": config.labels,
            "annotations": config.annotations
        }
        
        return template_service.render_template(
            "kubernetes/deployment.yaml.j2",
            context
        )
        
    def generate_service(self, config: K8sServiceConfig) -> str:
        """Generate Kubernetes Service manifest"""
        context = {
            "name": config.name,
            "namespace": config.namespace,
            "type": config.type,
            "ports": config.ports,
            "selector": config.selector
        }
        
        return template_service.render_template(
            "kubernetes/service.yaml.j2",
            context
        )
        
    def generate_ingress(self, config: K8sIngressConfig) -> str:
        """Generate Kubernetes Ingress manifest"""
        context = {
            "name": config.name,
            "namespace": config.namespace,
            "host": config.host,
            "paths": config.paths,
            "tls": config.tls,
            "annotations": config.annotations
        }
        
        return template_service.render_template(
            "kubernetes/ingress.yaml.j2",
            context
        )
        
    def generate_configmap(self, name: str, namespace: str, data: Dict[str, str]) -> str:
        """Generate Kubernetes ConfigMap manifest"""
        context = {
            "name": name,
            "namespace": namespace,
            "data": data
        }
        
        return template_service.render_template(
            "kubernetes/configmap.yaml.j2",
            context
        )
        
    def generate_secret(self, name: str, namespace: str, data: Dict[str, str]) -> str:
        """Generate Kubernetes Secret manifest"""
        context = {
            "name": name,
            "namespace": namespace,
            "data": data
        }
        
        return template_service.render_template(
            "kubernetes/secret.yaml.j2",
            context
        )
        
    def generate_hpa(self, name: str, namespace: str, deployment: str, 
                     min_replicas: int = 1, max_replicas: int = 10,
                     target_cpu: int = 80) -> str:
        """Generate Horizontal Pod Autoscaler manifest"""
        context = {
            "name": name,
            "namespace": namespace,
            "deployment": deployment,
            "min_replicas": min_replicas,
            "max_replicas": max_replicas,
            "target_cpu": target_cpu
        }
        
        return template_service.render_template(
            "kubernetes/hpa.yaml.j2",
            context
        )
        
    def generate_pvc(self, name: str, namespace: str, size: str = "10Gi",
                     storage_class: str = "standard") -> str:
        """Generate PersistentVolumeClaim manifest"""
        context = {
            "name": name,
            "namespace": namespace,
            "size": size,
            "storage_class": storage_class
        }
        
        return template_service.render_template(
            "kubernetes/pvc.yaml.j2",
            context
        )
        
    def generate_namespace(self, name: str) -> str:
        """Generate Namespace manifest"""
        return template_service.render_template(
            "kubernetes/namespace.yaml.j2",
            {"name": name}
        )
        
    def generate_kustomization(self, resources: List[str]) -> str:
        """Generate kustomization.yaml"""
        return yaml.dump({
            "apiVersion": "kustomize.config.k8s.io/v1beta1",
            "kind": "Kustomization",
            "resources": resources
        })

kubernetes_service = KubernetesService()
