"""Terraform/IaC schemas"""
from typing import Dict, List, Optional, Any
from pydantic import BaseModel

class TerraformProvider(BaseModel):
    name: str
    version: Optional[str] = None
    configuration: Dict[str, Any] = {}

class TerraformResource(BaseModel):
    type: str
    name: str
    properties: Dict[str, Any]

class TerraformVariable(BaseModel):
    name: str
    type: str = "string"
    default: Optional[Any] = None
    description: Optional[str] = None

class TerraformOutput(BaseModel):
    name: str
    value: str
    description: Optional[str] = None
    sensitive: bool = False

class IaCRequest(BaseModel):
    project_id: str
    cloud_provider: str  # aws, gcp, azure
    infrastructure_type: str  # kubernetes, serverless, vm-based
    regions: List[str] = []
    resources: List[Dict[str, Any]] = []
    networking: Dict[str, Any] = {}
    security: Dict[str, Any] = {}
