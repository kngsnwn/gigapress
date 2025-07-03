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
