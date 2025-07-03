from fastapi import APIRouter, HTTPException, Query
from typing import Dict, Any, List, Optional
from datetime import datetime
import logging

from app.schemas.project import (
    ProjectCreateRequest,
    ProjectModifyRequest,
    ProjectResponse,
    ProjectListResponse,
    ValidationRequest,
    ValidationResponse
)
from app.services.mcp_integration import mcp_integration_service
from app.services.context_manager import context_manager
from app.services.session_manager import session_manager

router = APIRouter()
logger = logging.getLogger(__name__)


@router.post("/create", response_model=ProjectResponse)
async def create_project(request: ProjectCreateRequest) -> ProjectResponse:
    """Create a new project"""
    try:
        # Create project through MCP
        result = await mcp_integration_service.create_project(
            session_id=request.session_id,
            requirements=request.requirements.dict()
        )
        
        return ProjectResponse(
            project_id=result["project_id"],
            status="created",
            details=result,
            created_at=datetime.utcnow()
        )
        
    except Exception as e:
        logger.error(f"Project creation failed: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/{project_id}/modify", response_model=ProjectResponse)
async def modify_project(
    project_id: str,
    request: ProjectModifyRequest
) -> ProjectResponse:
    """Modify an existing project"""
    try:
        result = await mcp_integration_service.modify_project(
            session_id=request.session_id,
            project_id=project_id,
            modification_request=request.modification
        )
        
        return ProjectResponse(
            project_id=project_id,
            status="modified" if result["status"] == "success" else result["status"],
            details=result,
            created_at=datetime.utcnow()
        )
        
    except Exception as e:
        logger.error(f"Project modification failed: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/{project_id}", response_model=ProjectResponse)
async def get_project(project_id: str) -> ProjectResponse:
    """Get project details"""
    try:
        project_info = await mcp_integration_service.get_project_info(project_id)
        
        return ProjectResponse(
            project_id=project_id,
            status=project_info.get("status", "unknown"),
            details=project_info,
            created_at=datetime.fromisoformat(project_info.get("created_at", datetime.utcnow().isoformat()))
        )
        
    except Exception as e:
        logger.error(f"Failed to get project info: {str(e)}")
        raise HTTPException(status_code=404, detail="Project not found")

@router.post("/{project_id}/validate", response_model=ValidationResponse)
async def validate_project(
    project_id: str,
    request: ValidationRequest
) -> ValidationResponse:
    """Validate project consistency"""
    try:
        result = await mcp_integration_service.validate_project(
            project_id,
            request.validation_type
        )
        return ValidationResponse(
            project_id=project_id,
            validation_status="success",
            details=result
        )
    except Exception as e:
        logger.error(f"Project validation failed: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

