#!/bin/bash

# Step 5: MCP Server Integration
echo "🚀 Setting up Conversational AI Engine - Step 5: MCP Server Integration"

cd conversational-ai-engine

# Create MCP client
echo "📝 Creating MCP client..."
cat > app/services/mcp_client.py << 'EOF'
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
EOF

# Create MCP integration service
echo "📝 Creating MCP integration service..."
cat > app/services/mcp_integration.py << 'EOF'
from typing import Dict, Any, List, Optional
import logging
from datetime import datetime
import asyncio

from app.services.mcp_client import mcp_client
from app.services.context_manager import context_manager
from app.services.state_tracker import state_tracker, ProjectState
from app.core.exceptions import ExternalServiceException

logger = logging.getLogger(__name__)


class MCPIntegrationService:
    """Service for integrating with MCP Server"""
    
    def __init__(self):
        self.workflow_cache = {}
        
    async def initialize(self):
        """Initialize MCP integration"""
        await mcp_client.initialize()
        
        # Test connection
        try:
            health = await mcp_client.health_check()
            logger.info(f"MCP Server connected: {health}")
        except Exception as e:
            logger.error(f"Failed to connect to MCP Server: {str(e)}")
            raise
    
    async def create_project(
        self,
        session_id: str,
        requirements: Dict[str, Any]
    ) -> Dict[str, Any]:
        """Create a new project through MCP Server"""
        try:
            # Update project state
            await state_tracker.update_project_state(
                session_id,
                ProjectState.PLANNING
            )
            
            # Generate project structure
            project_structure = await mcp_client.generate_project_structure(
                requirements=requirements,
                project_type=requirements.get("project_type", "web_app")
            )
            
            project_id = project_structure.get("projectId")
            
            # Update context with project info
            await context_manager.update_project_state(
                session_id,
                {
                    "project_id": project_id,
                    "structure": project_structure,
                    "created_at": datetime.utcnow().isoformat()
                }
            )
            
            # Execute creation workflow
            await state_tracker.update_project_state(
                session_id,
                ProjectState.IN_PROGRESS
            )
            
            workflow_result = await self._execute_creation_workflow(
                project_id,
                requirements,
                project_structure
            )
            
            # Update state to completed
            await state_tracker.update_project_state(
                session_id,
                ProjectState.COMPLETED
            )
            
            return {
                "project_id": project_id,
                "structure": project_structure,
                "workflow_result": workflow_result,
                "status": "success"
            }
            
        except Exception as e:
            logger.error(f"Project creation failed: {str(e)}")
            await state_tracker.update_project_state(
                session_id,
                ProjectState.FAILED,
                {"error": str(e)}
            )
            raise
    
    async def modify_project(
        self,
        session_id: str,
        project_id: str,
        modification_request: str
    ) -> Dict[str, Any]:
        """Modify an existing project"""
        try:
            # Get current project state
            context = await context_manager.get_relevant_context(session_id)
            current_state = context.get("project", {}).get("current_state", {})
            
            # Update project state
            await state_tracker.update_project_state(
                session_id,
                ProjectState.MODIFYING
            )
            
            # Analyze change impact
            impact_analysis = await mcp_client.analyze_change_impact(
                project_id=project_id,
                requested_change=modification_request,
                current_state=current_state
            )
            
            # If high risk, return for confirmation
            if impact_analysis.get("riskLevel") == "high":
                return {
                    "status": "confirmation_needed",
                    "impact_analysis": impact_analysis,
                    "message": "This change has high impact. Please confirm to proceed."
                }
            
            # Execute modification
            modification_result = await self._execute_modification_workflow(
                project_id,
                impact_analysis,
                modification_request
            )
            
            # Update context with modification
            await context_manager.add_modification(
                session_id,
                {
                    "request": modification_request,
                    "impact": impact_analysis,
                    "result": modification_result
                }
            )
            
            # Update state
            await state_tracker.update_project_state(
                session_id,
                ProjectState.COMPLETED
            )
            
            return {
                "status": "success",
                "impact_analysis": impact_analysis,
                "modification_result": modification_result
            }
            
        except Exception as e:
            logger.error(f"Project modification failed: {str(e)}")
            await state_tracker.update_project_state(
                session_id,
                ProjectState.FAILED,
                {"error": str(e)}
            )
            raise
    
    async def _execute_creation_workflow(
        self,
        project_id: str,
        requirements: Dict[str, Any],
        structure: Dict[str, Any]
    ) -> Dict[str, Any]:
        """Execute project creation workflow"""
        workflow_steps = []
        
        try:
            # Step 1: Domain analysis
            domain_result = await mcp_client.analyze_domain(
                description=requirements.get("description", ""),
                context=requirements
            )
            workflow_steps.append({"step": "domain_analysis", "status": "completed"})
            
            # Step 2: Backend generation
            if requirements.get("needs_backend", True):
                backend_result = await mcp_client.generate_backend(
                    requirements=requirements,
                    technology_stack=requirements.get("technologies", {})
                )
                workflow_steps.append({"step": "backend_generation", "status": "completed"})
            
            # Step 3: Frontend generation
            if requirements.get("needs_frontend", True):
                frontend_result = await mcp_client.generate_frontend(
                    requirements=requirements,
                    design_system=requirements.get("design_system", "material")
                )
                workflow_steps.append({"step": "frontend_generation", "status": "completed"})
            
            # Step 4: Infrastructure setup
            infra_result = await mcp_client.setup_infrastructure(
                project_id=project_id,
                deployment_target=requirements.get("deployment_target", "cloud")
            )
            workflow_steps.append({"step": "infrastructure_setup", "status": "completed"})
            
            # Step 5: Validate consistency
            validation_result = await mcp_client.validate_consistency(
                project_id=project_id,
                validation_scope="full"
            )
            workflow_steps.append({"step": "validation", "status": "completed"})
            
            return {
                "workflow": "project_creation",
                "steps": workflow_steps,
                "validation": validation_result,
                "status": "completed"
            }
            
        except Exception as e:
            logger.error(f"Workflow execution failed at step: {len(workflow_steps) + 1}")
            workflow_steps.append({
                "step": "failed",
                "error": str(e),
                "status": "failed"
            })
            raise
    
    async def _execute_modification_workflow(
        self,
        project_id: str,
        impact_analysis: Dict[str, Any],
        modification_request: str
    ) -> Dict[str, Any]:
        """Execute project modification workflow"""
        affected_components = impact_analysis.get("affectedComponents", [])
        
        try:
            # Update affected components
            update_results = []
            for component in affected_components:
                result = await mcp_client.update_components(
                    project_id=project_id,
                    components=[component],
                    update_type="modify"
                )
                update_results.append(result)
            
            # Validate after updates
            validation_result = await mcp_client.validate_consistency(
                project_id=project_id,
                validation_scope="modified"
            )
            
            return {
                "workflow": "project_modification",
                "modification": modification_request,
                "updates": update_results,
                "validation": validation_result,
                "status": "completed"
            }
            
        except Exception as e:
            logger.error(f"Modification workflow failed: {str(e)}")
            raise
    
    async def get_project_info(
        self,
        project_id: str
    ) -> Dict[str, Any]:
        """Get detailed project information from MCP Server"""
        try:
            status = await mcp_client.get_project_status(project_id)
            return status
        except Exception as e:
            logger.error(f"Failed to get project info: {str(e)}")
            raise
    
    async def validate_project(
        self,
        project_id: str,
        validation_type: str = "full"
    ) -> Dict[str, Any]:
        """Validate project through MCP Server"""
        try:
            result = await mcp_client.validate_consistency(
                project_id=project_id,
                validation_scope=validation_type
            )
            return result
        except Exception as e:
            logger.error(f"Project validation failed: {str(e)}")
            raise


# Singleton instance
mcp_integration_service = MCPIntegrationService()
EOF

# Create LangChain tools for MCP
echo "📝 Creating LangChain tools for MCP..."
cat > app/services/langchain_tools.py << 'EOF'
from langchain.tools import Tool, StructuredTool
from langchain.tools.base import ToolException
from typing import Dict, Any, List, Optional
from pydantic import BaseModel, Field
import json
import logging

from app.services.mcp_client import mcp_client
from app.services.mcp_integration import mcp_integration_service

logger = logging.getLogger(__name__)


class ProjectRequirementsTool(BaseModel):
    """Input for project requirements analysis"""
    description: str = Field(..., description="Project description")
    project_type: str = Field(..., description="Type of project")
    features: List[str] = Field(..., description="Required features")


class ChangeImpactTool(BaseModel):
    """Input for change impact analysis"""
    project_id: str = Field(..., description="Project ID")
    requested_change: str = Field(..., description="Requested change description")


class ComponentUpdateTool(BaseModel):
    """Input for component updates"""
    project_id: str = Field(..., description="Project ID")
    component_name: str = Field(..., description="Component to update")
    update_type: str = Field(..., description="Type of update: add, modify, remove")
    details: Dict[str, Any] = Field(..., description="Update details")


def create_mcp_tools() -> List[Tool]:
    """Create LangChain tools for MCP Server interaction"""
    
    async def analyze_project_requirements(
        description: str,
        project_type: str,
        features: List[str]
    ) -> str:
        """Analyze project requirements and generate structure"""
        try:
            requirements = {
                "description": description,
                "project_type": project_type,
                "features": features
            }
            
            result = await mcp_client.generate_project_structure(
                requirements=requirements,
                project_type=project_type
            )
            
            return json.dumps(result, indent=2)
            
        except Exception as e:
            raise ToolException(f"Failed to analyze requirements: {str(e)}")
    
    async def analyze_change_impact(
        project_id: str,
        requested_change: str
    ) -> str:
        """Analyze the impact of a requested change"""
        try:
            # Get current state (simplified for this example)
            current_state = {}
            
            result = await mcp_client.analyze_change_impact(
                project_id=project_id,
                requested_change=requested_change,
                current_state=current_state
            )
            
            return json.dumps(result, indent=2)
            
        except Exception as e:
            raise ToolException(f"Failed to analyze change impact: {str(e)}")
    
    async def update_component(
        project_id: str,
        component_name: str,
        update_type: str,
        details: Dict[str, Any]
    ) -> str:
        """Update a project component"""
        try:
            component = {
                "name": component_name,
                "type": update_type,
                "details": details
            }
            
            result = await mcp_client.update_components(
                project_id=project_id,
                components=[component],
                update_type=update_type
            )
            
            return json.dumps(result, indent=2)
            
        except Exception as e:
            raise ToolException(f"Failed to update component: {str(e)}")
    
    async def validate_project(project_id: str) -> str:
        """Validate project consistency"""
        try:
            result = await mcp_client.validate_consistency(
                project_id=project_id,
                validation_scope="full"
            )
            
            return json.dumps(result, indent=2)
            
        except Exception as e:
            raise ToolException(f"Failed to validate project: {str(e)}")
    
    async def get_project_status(project_id: str) -> str:
        """Get current project status"""
        try:
            result = await mcp_client.get_project_status(project_id)
            return json.dumps(result, indent=2)
            
        except Exception as e:
            raise ToolException(f"Failed to get project status: {str(e)}")
    
    # Create tools
    tools = [
        StructuredTool(
            name="analyze_project_requirements",
            description="Analyze project requirements and generate structure",
            func=analyze_project_requirements,
            args_schema=ProjectRequirementsTool,
            coroutine=analyze_project_requirements
        ),
        StructuredTool(
            name="analyze_change_impact",
            description="Analyze the impact of a requested change on a project",
            func=analyze_change_impact,
            args_schema=ChangeImpactTool,
            coroutine=analyze_change_impact
        ),
        StructuredTool(
            name="update_component",
            description="Update a component in the project",
            func=update_component,
            args_schema=ComponentUpdateTool,
            coroutine=update_component
        ),
        Tool(
            name="validate_project",
            description="Validate project consistency and correctness",
            func=validate_project,
            coroutine=validate_project
        ),
        Tool(
            name="get_project_status",
            description="Get the current status of a project",
            func=get_project_status,
            coroutine=get_project_status
        )
    ]
    
    return tools
class MCPToolkit:
    """Toolkit for MCP Server tools"""
    
    def __init__(self):
        self.tools = create_mcp_tools()
        
    def get_tools(self) -> List[Tool]:
        """Get all MCP tools"""
        return self.tools
EOF