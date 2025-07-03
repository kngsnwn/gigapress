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
