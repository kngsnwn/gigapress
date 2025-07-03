import httpx
from typing import Dict, Any, Optional, List
import logging
from datetime import datetime
import asyncio
from config.settings import settings
from app.core.exceptions import ExternalServiceException

logger = logging.getLogger(__name__)


class MCPClient:
    """Client for MCP Server communication"""
    
    def __init__(self):
        self.base_url = settings.mcp_server_url
        self.timeout = settings.mcp_server_timeout
        self.client: Optional[httpx.AsyncClient] = None
        
    async def initialize(self):
        """Initialize HTTP client"""
        self.client = httpx.AsyncClient(
            base_url=self.base_url,
            timeout=self.timeout,
            headers={
                "Content-Type": "application/json",
                "X-Service": "conversational-ai-engine"
            }
        )
        logger.info(f"MCP client initialized with base URL: {self.base_url}")
    
    async def close(self):
        """Close HTTP client"""
        if self.client:
            await self.client.aclose()
    
    async def health_check(self) -> Dict[str, Any]:
        """Check MCP Server health"""
        try:
            response = await self.client.get("/health")
            response.raise_for_status()
            return response.json()
        except Exception as e:
            logger.error(f"MCP health check failed: {str(e)}")
            raise ExternalServiceException("MCP Server", "Health check failed", {"error": str(e)})
    
    # Core MCP Tools
    
    async def analyze_change_impact(
        self,
        project_id: str,
        requested_change: str,
        current_state: Dict[str, Any]
    ) -> Dict[str, Any]:
        """Analyze the impact of a requested change"""
        try:
            payload = {
                "projectId": project_id,
                "requestedChange": requested_change,
                "currentState": current_state
            }
            
            response = await self.client.post(
                "/api/v1/tools/analyze-change-impact",
                json=payload
            )
            response.raise_for_status()
            
            result = response.json()
            logger.info(f"Change impact analysis completed for project {project_id}")
            return result
            
        except httpx.HTTPStatusError as e:
            logger.error(f"MCP analyze_change_impact failed: {e.response.status_code}")
            raise ExternalServiceException(
                "MCP Server",
                f"Change impact analysis failed: {e.response.text}",
                {"status_code": e.response.status_code}
            )
        except Exception as e:
            logger.error(f"MCP analyze_change_impact error: {str(e)}")
            raise
    
    async def generate_project_structure(
        self,
        requirements: Dict[str, Any],
        project_type: str
    ) -> Dict[str, Any]:
        """Generate project structure based on requirements"""
        try:
            payload = {
                "requirements": requirements,
                "projectType": project_type,
                "timestamp": datetime.utcnow().isoformat()
            }
            
            response = await self.client.post(
                "/api/v1/tools/generate-project-structure",
                json=payload
            )
            response.raise_for_status()
            
            result = response.json()
            logger.info(f"Project structure generated for type: {project_type}")
            return result
            
        except httpx.HTTPStatusError as e:
            logger.error(f"MCP generate_project_structure failed: {e.response.status_code}")
            raise ExternalServiceException(
                "MCP Server",
                f"Project structure generation failed: {e.response.text}",
                {"status_code": e.response.status_code}
            )
        except Exception as e:
            logger.error(f"MCP generate_project_structure error: {str(e)}")
            raise
    
    async def update_components(
        self,
        project_id: str,
        components: List[Dict[str, Any]],
        update_type: str
    ) -> Dict[str, Any]:
        """Update project components"""
        try:
            payload = {
                "projectId": project_id,
                "components": components,
                "updateType": update_type,
                "timestamp": datetime.utcnow().isoformat()
            }
            
            response = await self.client.post(
                "/api/v1/tools/update-components",
                json=payload
            )
            response.raise_for_status()
            
            result = response.json()
            logger.info(f"Components updated for project {project_id}")
            return result
            
        except httpx.HTTPStatusError as e:
            logger.error(f"MCP update_components failed: {e.response.status_code}")
            raise ExternalServiceException(
                "MCP Server",
                f"Component update failed: {e.response.text}",
                {"status_code": e.response.status_code}
            )
        except Exception as e:
            logger.error(f"MCP update_components error: {str(e)}")
            raise
    
    async def validate_consistency(
        self,
        project_id: str,
        validation_scope: str = "full"
    ) -> Dict[str, Any]:
        """Validate project consistency"""
        try:
            payload = {
                "projectId": project_id,
                "validationScope": validation_scope
            }
            
            response = await self.client.post(
                "/api/v1/tools/validate-consistency",
                json=payload
            )
            response.raise_for_status()
            
            result = response.json()
            logger.info(f"Consistency validation completed for project {project_id}")
            return result
            
        except httpx.HTTPStatusError as e:
            logger.error(f"MCP validate_consistency failed: {e.response.status_code}")
            raise ExternalServiceException(
                "MCP Server",
                f"Consistency validation failed: {e.response.text}",
                {"status_code": e.response.status_code}
            )
        except Exception as e:
            logger.error(f"MCP validate_consistency error: {str(e)}")
            raise
    
    # Service-specific endpoints
    
    async def analyze_domain(
        self,
        description: str,
        context: Dict[str, Any]
    ) -> Dict[str, Any]:
        """Analyze domain requirements"""
        try:
            response = await self.client.post(
                "/api/v1/services/domain/analyze",
                json={
                    "description": description,
                    "context": context
                }
            )
            response.raise_for_status()
            return response.json()
        except Exception as e:
            logger.error(f"Domain analysis failed: {str(e)}")
            raise
    
    async def generate_backend(
        self,
        requirements: Dict[str, Any],
        technology_stack: Dict[str, str]
    ) -> Dict[str, Any]:
        """Generate backend code"""
        try:
            response = await self.client.post(
                "/api/v1/services/backend/generate",
                json={
                    "requirements": requirements,
                    "technologyStack": technology_stack
                }
            )
            response.raise_for_status()
            return response.json()
        except Exception as e:
            logger.error(f"Backend generation failed: {str(e)}")
            raise
    
    async def generate_frontend(
        self,
        requirements: Dict[str, Any],
        design_system: str
    ) -> Dict[str, Any]:
        """Generate frontend code"""
        try:
            response = await self.client.post(
                "/api/v1/services/frontend/generate",
                json={
                    "requirements": requirements,
                    "designSystem": design_system
                }
            )
            response.raise_for_status()
            return response.json()
        except Exception as e:
            logger.error(f"Frontend generation failed: {str(e)}")
            raise
    
    async def setup_infrastructure(
        self,
        project_id: str,
        deployment_target: str
    ) -> Dict[str, Any]:
        """Setup infrastructure configuration"""
        try:
            response = await self.client.post(
                "/api/v1/services/infrastructure/setup",
                json={
                    "projectId": project_id,
                    "deploymentTarget": deployment_target
                }
            )
            response.raise_for_status()
            return response.json()
        except Exception as e:
            logger.error(f"Infrastructure setup failed: {str(e)}")
            raise
    
    # Batch operations
    
    async def execute_workflow(
        self,
        workflow_name: str,
        parameters: Dict[str, Any]
    ) -> Dict[str, Any]:
        """Execute a complete workflow"""
        try:
            response = await self.client.post(
                f"/api/v1/workflows/{workflow_name}/execute",
                json=parameters
            )
            response.raise_for_status()
            return response.json()
        except Exception as e:
            logger.error(f"Workflow execution failed: {str(e)}")
            raise
    
    async def get_project_status(self, project_id: str) -> Dict[str, Any]:
        """Get project status from MCP Server"""
        try:
            response = await self.client.get(f"/api/v1/projects/{project_id}/status")
            response.raise_for_status()
            return response.json()
        except Exception as e:
            logger.error(f"Failed to get project status: {str(e)}")
            raise


# Singleton instance
mcp_client = MCPClient()
