"""Terraform service for Infrastructure as Code"""
from typing import Dict, Any, List
from app.services.template_service import template_service
from app.schemas.terraform import TerraformProvider, TerraformResource
from app.core.logging import setup_logging

logger = setup_logging()

class TerraformService:
    def generate_main_tf(self, providers: List[TerraformProvider], 
                        resources: List[TerraformResource]) -> str:
        """Generate main.tf file"""
        context = {
            "providers": providers,
            "resources": resources
        }
        
        return template_service.render_template(
            "terraform/main.tf.j2",
            context
        )
        
    def generate_variables_tf(self, variables: List[Dict[str, Any]]) -> str:
        """Generate variables.tf file"""
        context = {"variables": variables}
        
        return template_service.render_template(
            "terraform/variables.tf.j2",
            context
        )
        
    def generate_outputs_tf(self, outputs: List[Dict[str, Any]]) -> str:
        """Generate outputs.tf file"""
        context = {"outputs": outputs}
        
        return template_service.render_template(
            "terraform/outputs.tf.j2",
            context
        )
        
    def generate_aws_resources(self, config: Dict[str, Any]) -> List[TerraformResource]:
        """Generate AWS resources"""
        resources = []
        
        # VPC
        if config.get("vpc"):
            resources.append(TerraformResource(
                type="aws_vpc",
                name="main",
                properties={
                    "cidr_block": config["vpc"].get("cidr", "10.0.0.0/16"),
                    "enable_dns_hostnames": True,
                    "enable_dns_support": True
                }
            ))
            
        # EKS Cluster
        if config.get("eks"):
            resources.append(TerraformResource(
                type="aws_eks_cluster",
                name="main",
                properties={
                    "name": config["eks"].get("name", "gigapress-cluster"),
                    "role_arn": "${aws_iam_role.eks_cluster.arn}",
                    "vpc_config": {
                        "subnet_ids": "${aws_subnet.private[*].id}"
                    }
                }
            ))
            
        return resources
        
    def generate_gcp_resources(self, config: Dict[str, Any]) -> List[TerraformResource]:
        """Generate GCP resources"""
        resources = []
        
        # GKE Cluster
        if config.get("gke"):
            resources.append(TerraformResource(
                type="google_container_cluster",
                name="main",
                properties={
                    "name": config["gke"].get("name", "gigapress-cluster"),
                    "location": config["gke"].get("location", "us-central1"),
                    "initial_node_count": 3
                }
            ))
            
        return resources
        
    def generate_azure_resources(self, config: Dict[str, Any]) -> List[TerraformResource]:
        """Generate Azure resources"""
        resources = []
        
        # AKS Cluster
        if config.get("aks"):
            resources.append(TerraformResource(
                type="azurerm_kubernetes_cluster",
                name="main",
                properties={
                    "name": config["aks"].get("name", "gigapress-cluster"),
                    "location": config["aks"].get("location", "eastus"),
                    "resource_group_name": "${azurerm_resource_group.main.name}",
                    "dns_prefix": "gigapress"
                }
            ))
            
        return resources

terraform_service = TerraformService()
