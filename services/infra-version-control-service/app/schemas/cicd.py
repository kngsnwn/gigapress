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
