"""Project infrastructure models"""
from typing import Dict, List, Optional, Any
from datetime import datetime
from pydantic import BaseModel

class InfrastructureConfig(BaseModel):
    """Complete infrastructure configuration for a project"""
    project_id: str
    created_at: datetime = datetime.now()
    updated_at: datetime = datetime.now()
    
    # Docker configuration
    docker: Dict[str, Any] = {}
    docker_compose: Optional[Dict[str, Any]] = None
    
    # Kubernetes configuration
    kubernetes: Dict[str, Any] = {}
    helm_charts: Optional[Dict[str, Any]] = None
    
    # CI/CD configuration
    cicd: Dict[str, Any] = {}
    
    # Git configuration
    git: Dict[str, Any] = {}
    
    # IaC configuration
    terraform: Optional[Dict[str, Any]] = None
    
    # Monitoring configuration
    monitoring: Dict[str, Any] = {}
    
    # Metadata
    version: str = "1.0.0"
    environment_configs: Dict[str, Dict[str, Any]] = {}
    
class GenerationStatus(BaseModel):
    """Status of infrastructure generation"""
    project_id: str
    status: str  # pending, in_progress, completed, failed
    current_step: str
    total_steps: int
    progress_percentage: float
    messages: List[str] = []
    errors: List[str] = []
    started_at: datetime
    completed_at: Optional[datetime] = None
