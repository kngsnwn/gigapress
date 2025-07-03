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
