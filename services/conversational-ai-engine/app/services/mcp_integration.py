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
