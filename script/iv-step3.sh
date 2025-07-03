#!/bin/bash

# Step 3: Core Services Implementation
# This script creates all core services

SERVICE_DIR="services/infra-version-control-service"

echo "🔧 Step 3: Creating core services..."

# Create Redis service
cat > ${SERVICE_DIR}/app/services/redis_service.py << 'EOF'
"""Redis service for caching"""
import json
from typing import Any, Optional
import redis.asyncio as redis
from app.core.config import settings
from app.core.logging import setup_logging

logger = setup_logging()

class RedisService:
    def __init__(self):
        self.redis_client: Optional[redis.Redis] = None
        
    async def connect(self):
        """Connect to Redis"""
        try:
            self.redis_client = await redis.from_url(
                f"redis://:{settings.REDIS_PASSWORD}@{settings.REDIS_HOST}:{settings.REDIS_PORT}",
                encoding="utf-8",
                decode_responses=True
            )
            await self.redis_client.ping()
            logger.info("Connected to Redis")
        except Exception as e:
            logger.error(f"Failed to connect to Redis: {e}")
            raise
            
    async def disconnect(self):
        """Disconnect from Redis"""
        if self.redis_client:
            await self.redis_client.close()
            logger.info("Disconnected from Redis")
            
    async def get(self, key: str) -> Optional[Any]:
        """Get value from Redis"""
        if not self.redis_client:
            return None
        try:
            value = await self.redis_client.get(key)
            return json.loads(value) if value else None
        except Exception as e:
            logger.error(f"Redis get error: {e}")
            return None
            
    async def set(self, key: str, value: Any, expire: int = 3600) -> bool:
        """Set value in Redis with expiration"""
        if not self.redis_client:
            return False
        try:
            await self.redis_client.set(
                key, 
                json.dumps(value), 
                ex=expire
            )
            return True
        except Exception as e:
            logger.error(f"Redis set error: {e}")
            return False
            
    async def delete(self, key: str) -> bool:
        """Delete key from Redis"""
        if not self.redis_client:
            return False
        try:
            await self.redis_client.delete(key)
            return True
        except Exception as e:
            logger.error(f"Redis delete error: {e}")
            return False

redis_service = RedisService()
EOF

# Create Kafka service
cat > ${SERVICE_DIR}/app/services/kafka_service.py << 'EOF'
"""Kafka service for event processing"""
import json
from typing import Dict, Any, Optional
from aiokafka import AIOKafkaConsumer, AIOKafkaProducer
from app.core.config import settings
from app.core.logging import setup_logging

logger = setup_logging()

class KafkaService:
    def __init__(self):
        self.consumer: Optional[AIOKafkaConsumer] = None
        self.producer: Optional[AIOKafkaProducer] = None
        
    async def start(self):
        """Start Kafka consumer and producer"""
        try:
            # Initialize producer
            self.producer = AIOKafkaProducer(
                bootstrap_servers=settings.KAFKA_BOOTSTRAP_SERVERS,
                value_serializer=lambda v: json.dumps(v).encode()
            )
            await self.producer.start()
            
            # Initialize consumer
            self.consumer = AIOKafkaConsumer(
                *settings.KAFKA_TOPICS,
                bootstrap_servers=settings.KAFKA_BOOTSTRAP_SERVERS,
                group_id=settings.KAFKA_CONSUMER_GROUP,
                value_deserializer=lambda v: json.loads(v.decode())
            )
            await self.consumer.start()
            logger.info("Kafka service started")
            
            # Start consuming messages
            await self._consume_messages()
            
        except Exception as e:
            logger.error(f"Failed to start Kafka service: {e}")
            raise
            
    async def stop(self):
        """Stop Kafka consumer and producer"""
        if self.consumer:
            await self.consumer.stop()
        if self.producer:
            await self.producer.stop()
        logger.info("Kafka service stopped")
        
    async def _consume_messages(self):
        """Consume messages from Kafka topics"""
        async for message in self.consumer:
            try:
                await self._process_message(
                    message.topic,
                    message.value
                )
            except Exception as e:
                logger.error(f"Error processing message: {e}")
                
    async def _process_message(self, topic: str, message: Dict[str, Any]):
        """Process incoming Kafka message"""
        logger.info(f"Received message from {topic}: {message}")
        
        if topic == "project-updates":
            await self._handle_project_update(message)
        elif topic == "infra-requests":
            await self._handle_infra_request(message)
        elif topic == "git-events":
            await self._handle_git_event(message)
            
    async def _handle_project_update(self, message: Dict[str, Any]):
        """Handle project update events"""
        # Implement project update logic
        pass
        
    async def _handle_infra_request(self, message: Dict[str, Any]):
        """Handle infrastructure request events"""
        # Implement infra request logic
        pass
        
    async def _handle_git_event(self, message: Dict[str, Any]):
        """Handle git events"""
        # Implement git event logic
        pass
        
    async def publish_event(self, topic: str, event: Dict[str, Any]):
        """Publish event to Kafka topic"""
        if self.producer:
            await self.producer.send(topic, value=event)
            logger.info(f"Published event to {topic}: {event}")

kafka_service = KafkaService()
EOF

# Create template service
cat > ${SERVICE_DIR}/app/services/template_service.py << 'EOF'
"""Template service for generating configuration files"""
from typing import Dict, Any
from pathlib import Path
from jinja2 import Environment, FileSystemLoader, Template
from app.core.logging import setup_logging

logger = setup_logging()

class TemplateService:
    def __init__(self):
        template_dir = Path(__file__).parent.parent / "templates"
        self.env = Environment(
            loader=FileSystemLoader(str(template_dir)),
            trim_blocks=True,
            lstrip_blocks=True
        )
        
    def render_template(self, template_path: str, context: Dict[str, Any]) -> str:
        """Render a template with given context"""
        try:
            template = self.env.get_template(template_path)
            return template.render(**context)
        except Exception as e:
            logger.error(f"Template rendering error: {e}")
            raise
            
    def render_string(self, template_string: str, context: Dict[str, Any]) -> str:
        """Render a template string with given context"""
        try:
            template = Template(template_string)
            return template.render(**context)
        except Exception as e:
            logger.error(f"String template rendering error: {e}")
            raise

template_service = TemplateService()
EOF

# Create Docker service
cat > ${SERVICE_DIR}/app/services/docker_service.py << 'EOF'
"""Docker service for generating Docker configurations"""
from typing import Dict, Any, List
import docker
import yaml
from app.services.template_service import template_service
from app.schemas.docker import DockerImageConfig, DockerComposeConfig
from app.core.logging import setup_logging

logger = setup_logging()

class DockerService:
    def __init__(self):
        try:
            self.client = docker.from_env()
        except Exception as e:
            logger.warning(f"Docker client initialization failed: {e}")
            self.client = None
            
    def generate_dockerfile(self, config: DockerImageConfig) -> str:
        """Generate Dockerfile content"""
        context = {
            "base_image": config.base_image,
            "workdir": config.workdir,
            "ports": config.exposed_ports,
            "env_vars": config.environment,
            "commands": config.commands,
            "entrypoint": config.entrypoint,
            "labels": config.labels
        }
        
        return template_service.render_template(
            "docker/Dockerfile.j2",
            context
        )
        
    def generate_docker_compose(self, config: DockerComposeConfig) -> str:
        """Generate docker-compose.yml content"""
        compose_dict = {
            "version": config.version,
            "services": {}
        }
        
        for service_name, service_config in config.services.items():
            service_dict = {}
            
            if service_config.image:
                service_dict["image"] = service_config.image
            if service_config.build:
                service_dict["build"] = service_config.build
            if service_config.ports:
                service_dict["ports"] = service_config.ports
            if service_config.environment:
                service_dict["environment"] = service_config.environment
            if service_config.volumes:
                service_dict["volumes"] = service_config.volumes
            if service_config.depends_on:
                service_dict["depends_on"] = service_config.depends_on
            if service_config.networks:
                service_dict["networks"] = service_config.networks
            if service_config.healthcheck:
                service_dict["healthcheck"] = service_config.healthcheck
                
            compose_dict["services"][service_name] = service_dict
            
        if config.volumes:
            compose_dict["volumes"] = config.volumes
        if config.networks:
            compose_dict["networks"] = config.networks
            
        return yaml.dump(compose_dict, default_flow_style=False)
        
    def generate_dockerignore(self, framework: str) -> str:
        """Generate .dockerignore content based on framework"""
        return template_service.render_template(
            f"docker/dockerignore/{framework}.dockerignore.j2",
            {}
        )
        
    async def build_image(self, dockerfile: str, tag: str) -> bool:
        """Build Docker image (if Docker daemon is available)"""
        if not self.client:
            logger.warning("Docker client not available")
            return False
            
        try:
            # This would build the image if Docker is available
            logger.info(f"Would build Docker image with tag: {tag}")
            return True
        except Exception as e:
            logger.error(f"Docker build error: {e}")
            return False

docker_service = DockerService()
EOF

# Create Kubernetes service
cat > ${SERVICE_DIR}/app/services/kubernetes_service.py << 'EOF'
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
EOF

# Create Git service
cat > ${SERVICE_DIR}/app/services/git_service.py << 'EOF'
"""Git service for version control operations"""
import os
import shutil
from pathlib import Path
from typing import List, Dict, Any, Optional
from git import Repo, GitCommandError
from app.core.config import settings
from app.core.logging import setup_logging
from app.schemas.git import GitRepository, GitCommit, GitBranch
from app.services.template_service import template_service

logger = setup_logging()

class GitService:
    def __init__(self):
        self.repos_dir = Path("repositories")
        self.repos_dir.mkdir(exist_ok=True)
        
    def init_repository(self, repo: GitRepository) -> bool:
        """Initialize a new Git repository"""
        try:
            repo_path = self.repos_dir / repo.project_id / repo.repo_name
            repo_path.mkdir(parents=True, exist_ok=True)
            
            # Initialize repository
            git_repo = Repo.init(repo_path)
            
            # Set default branch
            git_repo.git.checkout("-b", repo.default_branch)
            
            # Configure git
            with git_repo.config_writer() as config:
                config.set_value("user", "name", settings.GIT_AUTHOR_NAME)
                config.set_value("user", "email", settings.GIT_AUTHOR_EMAIL)
                
            logger.info(f"Initialized repository: {repo_path}")
            return True
            
        except Exception as e:
            logger.error(f"Failed to initialize repository: {e}")
            return False
            
    def create_gitignore(self, project_id: str, repo_name: str, template: str) -> bool:
        """Create .gitignore file from template"""
        try:
            repo_path = self.repos_dir / project_id / repo_name
            gitignore_content = template_service.render_template(
                f"git/gitignore/{template}.gitignore.j2",
                {}
            )
            
            gitignore_path = repo_path / ".gitignore"
            gitignore_path.write_text(gitignore_content)
            
            return True
        except Exception as e:
            logger.error(f"Failed to create .gitignore: {e}")
            return False
            
    def create_readme(self, project_id: str, repo_name: str, content: Dict[str, Any]) -> bool:
        """Create README.md file"""
        try:
            repo_path = self.repos_dir / project_id / repo_name
            readme_content = template_service.render_template(
                "git/README.md.j2",
                content
            )
            
            readme_path = repo_path / "README.md"
            readme_path.write_text(readme_content)
            
            return True
        except Exception as e:
            logger.error(f"Failed to create README: {e}")
            return False
            
    def commit(self, project_id: str, repo_name: str, commit: GitCommit) -> bool:
        """Make a commit"""
        try:
            repo_path = self.repos_dir / project_id / repo_name
            repo = Repo(repo_path)
            
            # Add files
            if commit.files:
                repo.index.add(commit.files)
            else:
                repo.git.add(A=True)  # Add all files
                
            # Commit
            repo.index.commit(
                commit.message,
                author=f"{commit.author_name or settings.GIT_AUTHOR_NAME} <{commit.author_email or settings.GIT_AUTHOR_EMAIL}>"
            )
            
            logger.info(f"Created commit: {commit.message}")
            return True
            
        except Exception as e:
            logger.error(f"Failed to commit: {e}")
            return False
            
    def create_branch(self, project_id: str, repo_name: str, branch: GitBranch) -> bool:
        """Create a new branch"""
        try:
            repo_path = self.repos_dir / project_id / repo_name
            repo = Repo(repo_path)
            
            # Create and checkout new branch
            repo.git.checkout("-b", branch.name, branch.from_branch)
            
            logger.info(f"Created branch: {branch.name}")
            return True
            
        except Exception as e:
            logger.error(f"Failed to create branch: {e}")
            return False
            
    def get_branches(self, project_id: str, repo_name: str) -> List[str]:
        """Get list of branches"""
        try:
            repo_path = self.repos_dir / project_id / repo_name
            repo = Repo(repo_path)
            
            return [ref.name for ref in repo.references if ref.name.startswith("refs/heads/")]
            
        except Exception as e:
            logger.error(f"Failed to get branches: {e}")
            return []
            
    def generate_github_actions(self, workflow_config: Dict[str, Any]) -> str:
        """Generate GitHub Actions workflow"""
        return template_service.render_template(
            "cicd/github-actions.yml.j2",
            workflow_config
        )
        
    def generate_gitlab_ci(self, pipeline_config: Dict[str, Any]) -> str:
        """Generate GitLab CI pipeline"""
        return template_service.render_template(
            "cicd/gitlab-ci.yml.j2",
            pipeline_config
        )

git_service = GitService()
EOF

# Create CI/CD service
cat > ${SERVICE_DIR}/app/services/cicd_service.py << 'EOF'
"""CI/CD service for pipeline generation"""
from typing import Dict, Any, List
import yaml
from app.services.template_service import template_service
from app.schemas.cicd import GitHubActionsConfig, JenkinsConfig, GitLabCIConfig
from app.core.logging import setup_logging

logger = setup_logging()

class CICDService:
    def generate_github_actions(self, config: GitHubActionsConfig) -> str:
        """Generate GitHub Actions workflow"""
        workflow = {
            "name": config.name,
            "on": config.triggers,
            "env": config.env or {},
            "jobs": config.jobs
        }
        
        return yaml.dump(workflow, default_flow_style=False, sort_keys=False)
        
    def generate_jenkins_pipeline(self, config: JenkinsConfig) -> str:
        """Generate Jenkinsfile"""
        context = {
            "pipeline_name": config.pipeline_name,
            "agent": config.agent,
            "stages": config.stages,
            "environment": config.environment,
            "options": config.options
        }
        
        return template_service.render_template(
            "cicd/Jenkinsfile.j2",
            context
        )
        
    def generate_gitlab_ci(self, config: GitLabCIConfig) -> str:
        """Generate .gitlab-ci.yml"""
        ci_config = {
            "stages": config.stages,
            "variables": config.variables
        }
        ci_config.update(config.jobs)
        
        return yaml.dump(ci_config, default_flow_style=False, sort_keys=False)
        
    def generate_build_workflow(self, project_type: str, framework: str) -> Dict[str, Any]:
        """Generate build workflow based on project type and framework"""
        workflows = {
            "frontend": {
                "react": self._react_workflow(),
                "vue": self._vue_workflow(),
                "angular": self._angular_workflow()
            },
            "backend": {
                "express": self._node_workflow(),
                "spring-boot": self._java_workflow(),
                "django": self._python_workflow()
            }
        }
        
        return workflows.get(project_type, {}).get(framework, self._default_workflow())
        
    def _react_workflow(self) -> Dict[str, Any]:
        """React build workflow"""
        return {
            "build": {
                "runs-on": "ubuntu-latest",
                "steps": [
                    {"uses": "actions/checkout@v3"},
                    {"uses": "actions/setup-node@v3", "with": {"node-version": "18"}},
                    {"run": "npm ci"},
                    {"run": "npm run build"},
                    {"run": "npm test"}
                ]
            }
        }
        
    def _vue_workflow(self) -> Dict[str, Any]:
        """Vue build workflow"""
        return {
            "build": {
                "runs-on": "ubuntu-latest",
                "steps": [
                    {"uses": "actions/checkout@v3"},
                    {"uses": "actions/setup-node@v3", "with": {"node-version": "18"}},
                    {"run": "npm ci"},
                    {"run": "npm run build"},
                    {"run": "npm run test:unit"}
                ]
            }
        }
        
    def _angular_workflow(self) -> Dict[str, Any]:
        """Angular build workflow"""
        return {
            "build": {
                "runs-on": "ubuntu-latest",
                "steps": [
                    {"uses": "actions/checkout@v3"},
                    {"uses": "actions/setup-node@v3", "with": {"node-version": "18"}},
                    {"run": "npm ci"},
                    {"run": "npm run build"},
                    {"run": "npm run test -- --watch=false"}
                ]
            }
        }
        
    def _node_workflow(self) -> Dict[str, Any]:
        """Node.js build workflow"""
        return {
            "build": {
                "runs-on": "ubuntu-latest",
                "steps": [
                    {"uses": "actions/checkout@v3"},
                    {"uses": "actions/setup-node@v3", "with": {"node-version": "18"}},
                    {"run": "npm ci"},
                    {"run": "npm test"},
                    {"run": "npm run lint"}
                ]
            }
        }
        
    def _java_workflow(self) -> Dict[str, Any]:
        """Java/Spring Boot build workflow"""
        return {
            "build": {
                "runs-on": "ubuntu-latest",
                "steps": [
                    {"uses": "actions/checkout@v3"},
                    {"uses": "actions/setup-java@v3", "with": {"java-version": "17", "distribution": "temurin"}},
                    {"run": "./gradlew build"},
                    {"run": "./gradlew test"}
                ]
            }
        }
        
    def _python_workflow(self) -> Dict[str, Any]:
        """Python build workflow"""
        return {
            "build": {
                "runs-on": "ubuntu-latest",
                "steps": [
                    {"uses": "actions/checkout@v3"},
                    {"uses": "actions/setup-python@v4", "with": {"python-version": "3.10"}},
                    {"run": "pip install -r requirements.txt"},
                    {"run": "python -m pytest"},
                    {"run": "python -m flake8"}
                ]
            }
        }
        
    def _default_workflow(self) -> Dict[str, Any]:
        """Default build workflow"""
        return {
            "build": {
                "runs-on": "ubuntu-latest",
                "steps": [
                    {"uses": "actions/checkout@v3"},
                    {"run": "echo 'Add build steps here'"}
                ]
            }
        }

cicd_service = CICDService()
EOF

# Create Terraform service
cat > ${SERVICE_DIR}/app/services/terraform_service.py << 'EOF'
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
EOF

# Create monitoring service
cat > ${SERVICE_DIR}/app/services/monitoring_service.py << 'EOF'
"""Monitoring service for observability setup"""
from typing import Dict, Any, List
import yaml
from app.services.template_service import template_service
from app.schemas.monitoring import PrometheusConfig, GrafanaDashboard, AlertRule
from app.core.logging import setup_logging

logger = setup_logging()

class MonitoringService:
    def generate_prometheus_config(self, config: PrometheusConfig) -> str:
        """Generate prometheus.yml configuration"""
        prometheus_config = {
            "global": config.global_config,
            "scrape_configs": config.scrape_configs
        }
        
        if config.alerting:
            prometheus_config["alerting"] = config.alerting
            
        if config.rule_files:
            prometheus_config["rule_files"] = config.rule_files
            
        return yaml.dump(prometheus_config, default_flow_style=False)
        
    def generate_grafana_dashboard(self, dashboard: GrafanaDashboard) -> Dict[str, Any]:
        """Generate Grafana dashboard JSON"""
        return {
            "dashboard": {
                "title": dashboard.title,
                "panels": dashboard.panels,
                "templating": {"list": dashboard.variables},
                "time": dashboard.time,
                "timepicker": {},
                "timezone": "browser",
                "version": 1
            },
            "overwrite": True
        }
        
    def generate_alert_rules(self, rules: List[AlertRule]) -> str:
        """Generate Prometheus alert rules"""
        groups = [{
            "name": "gigapress_alerts",
            "interval": "30s",
            "rules": [
                {
                    "alert": rule.name,
                    "expr": rule.expression,
                    "for": rule.duration,
                    "labels": rule.labels,
                    "annotations": rule.annotations
                }
                for rule in rules
            ]
        }]
        
        return yaml.dump({"groups": groups}, default_flow_style=False)
        
    def generate_default_scrape_configs(self, services: List[str]) -> List[Dict[str, Any]]:
        """Generate default scrape configurations for services"""
        configs = []
        
        for service in services:
            configs.append({
                "job_name": service,
                "static_configs": [{
                    "targets": [f"{service}:9090"]
                }],
                "metrics_path": "/metrics"
            })
            
        return configs
        
    def generate_logging_config(self, log_aggregator: str = "fluentd") -> str:
        """Generate logging configuration"""
        if log_aggregator == "fluentd":
            return template_service.render_template(
                "monitoring/fluentd.conf.j2",
                {}
            )
        elif log_aggregator == "fluent-bit":
            return template_service.render_template(
                "monitoring/fluent-bit.conf.j2",
                {}
            )
        else:
            return ""

monitoring_service = MonitoringService()
EOF

echo "✅ Step 3 Complete: Core services created"
echo "🔧 Created services:"
echo "   - Redis service (caching)"
echo "   - Kafka service (event processing)"
echo "   - Template service (template engine)"
echo "   - Docker service"
echo "   - Kubernetes service"
echo "   - Git service"
echo "   - CI/CD service"
echo "   - Terraform service"
echo "   - Monitoring service"