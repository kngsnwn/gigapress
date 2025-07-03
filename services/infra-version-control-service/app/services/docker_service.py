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
