#!/bin/bash

# Step 2: Data Models and Schemas
# This script creates all data models and schemas

SERVICE_DIR="services/infra-version-control-service"

echo "📋 Step 2: Creating data models and schemas..."

# Create base schemas
cat > ${SERVICE_DIR}/app/schemas/base.py << 'EOF'
"""Base schemas for common response formats"""
from typing import Any, Dict, List, Optional
from datetime import datetime
from pydantic import BaseModel

class BaseResponse(BaseModel):
    success: bool
    message: str
    data: Optional[Any] = None
    error: Optional[str] = None
    timestamp: datetime = datetime.now()

class ProjectInfo(BaseModel):
    project_id: str
    project_name: str
    project_type: str
    version: str
    description: Optional[str] = None
    
class ServiceHealth(BaseModel):
    service: str
    status: str
    version: str
    uptime: float
    dependencies: Dict[str, str]
EOF

# Create Docker schemas
cat > ${SERVICE_DIR}/app/schemas/docker.py << 'EOF'
"""Docker-related schemas"""
from typing import Dict, List, Optional, Any
from pydantic import BaseModel

class DockerImageConfig(BaseModel):
    base_image: str = "node:18-alpine"
    workdir: str = "/app"
    exposed_ports: List[int] = []
    environment: Dict[str, str] = {}
    commands: List[str] = []
    entrypoint: Optional[List[str]] = None
    labels: Dict[str, str] = {}

class DockerComposeService(BaseModel):
    image: Optional[str] = None
    build: Optional[Dict[str, Any]] = None
    ports: List[str] = []
    environment: Dict[str, str] = {}
    volumes: List[str] = []
    depends_on: List[str] = []
    networks: List[str] = []
    healthcheck: Optional[Dict[str, Any]] = None

class DockerComposeConfig(BaseModel):
    version: str = "3.8"
    services: Dict[str, DockerComposeService]
    volumes: Optional[Dict[str, Dict]] = None
    networks: Optional[Dict[str, Dict]] = None

class DockerBuildRequest(BaseModel):
    project_id: str
    service_name: str
    service_type: str  # frontend, backend, database
    framework: str  # react, express, spring-boot, etc.
    dependencies: List[str] = []
    environment_vars: Dict[str, str] = {}
    ports: List[int] = []
    build_args: Dict[str, str] = {}
EOF

# Create Kubernetes schemas
cat > ${SERVICE_DIR}/app/schemas/kubernetes.py << 'EOF'
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
EOF

# Create CI/CD schemas
cat > ${SERVICE_DIR}/app/schemas/cicd.py << 'EOF'
"""CI/CD pipeline schemas"""
from typing import Dict, List, Optional, Any
from pydantic import BaseModel

class GitHubActionsConfig(BaseModel):
    name: str
    triggers: Dict[str, Any]
    jobs: Dict[str, Any]
    env: Optional[Dict[str, str]] = None

class JenkinsConfig(BaseModel):
    pipeline_name: str
    agent: str = "any"
    stages: List[Dict[str, Any]]
    environment: Dict[str, str] = {}
    options: List[str] = []
    
class GitLabCIConfig(BaseModel):
    stages: List[str]
    variables: Dict[str, str] = {}
    jobs: Dict[str, Any] = {}
    
class CICDRequest(BaseModel):
    project_id: str
    pipeline_type: str  # github-actions, jenkins, gitlab-ci
    branch_strategy: str = "gitflow"  # gitflow, github-flow, trunk-based
    environments: List[str] = ["dev", "staging", "prod"]
    build_steps: List[Dict[str, Any]] = []
    test_commands: List[str] = []
    deploy_strategy: str = "rolling"  # rolling, blue-green, canary
    notifications: Dict[str, Any] = {}
EOF

# Create Git schemas
cat > ${SERVICE_DIR}/app/schemas/git.py << 'EOF'
"""Git-related schemas"""
from typing import List, Optional
from datetime import datetime
from pydantic import BaseModel

class GitRepository(BaseModel):
    project_id: str
    repo_name: str
    remote_url: Optional[str] = None
    default_branch: str = "main"
    description: Optional[str] = None
    
class GitCommit(BaseModel):
    message: str
    files: List[str]
    author_name: Optional[str] = None
    author_email: Optional[str] = None
    
class GitBranch(BaseModel):
    name: str
    from_branch: str = "main"
    
class GitInitRequest(BaseModel):
    project_id: str
    repo_name: str
    include_readme: bool = True
    gitignore_template: str = "node"  # node, java, python, etc.
    license: Optional[str] = "MIT"
    initial_commit_message: str = "Initial commit"
EOF

# Create Terraform schemas
cat > ${SERVICE_DIR}/app/schemas/terraform.py << 'EOF'
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
EOF

# Create monitoring schemas
cat > ${SERVICE_DIR}/app/schemas/monitoring.py << 'EOF'
"""Monitoring configuration schemas"""
from typing import Dict, List, Optional, Any
from pydantic import BaseModel

class PrometheusConfig(BaseModel):
    global_config: Dict[str, Any] = {
        "scrape_interval": "15s",
        "evaluation_interval": "15s"
    }
    scrape_configs: List[Dict[str, Any]] = []
    alerting: Optional[Dict[str, Any]] = None
    rule_files: List[str] = []

class GrafanaDashboard(BaseModel):
    title: str
    panels: List[Dict[str, Any]]
    variables: List[Dict[str, Any]] = []
    time: Dict[str, str] = {"from": "now-6h", "to": "now"}

class AlertRule(BaseModel):
    name: str
    expression: str
    duration: str = "5m"
    labels: Dict[str, str] = {}
    annotations: Dict[str, str] = {}

class MonitoringRequest(BaseModel):
    project_id: str
    monitoring_stack: List[str] = ["prometheus", "grafana"]
    metrics_endpoints: List[str] = []
    log_aggregation: bool = True
    tracing: bool = True
    alerting_channels: List[str] = ["email", "slack"]
EOF

# Create models
cat > ${SERVICE_DIR}/app/models/project.py << 'EOF'
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
EOF

echo "✅ Step 2 Complete: Data models and schemas created"
echo "📦 Created schemas for:"
echo "   - Docker configurations"
echo "   - Kubernetes manifests"
echo "   - CI/CD pipelines"
echo "   - Git operations"
echo "   - Terraform/IaC"
echo "   - Monitoring setup"