"""Base schemas for common response formats"""
from typing import Any, Dict, List, Optional
from datetime import datetime
from pydantic import BaseModel

class BaseResponse(BaseModel):
    success: bool
    message: str
    data: Optional[Any] = None
    error: Optional[str] = None
    timestamp: datetime = datetime.now()

class ProjectInfo(BaseModel):
    project_id: str
    project_name: str
    project_type: str
    version: str
    description: Optional[str] = None
    
class ServiceHealth(BaseModel):
    service: str
    status: str
    version: str
    uptime: float
    dependencies: Dict[str, str]
