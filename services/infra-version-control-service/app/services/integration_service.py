"""Integration service for communicating with other GigaPress services"""
import httpx
from typing import Dict, Any, Optional
from app.core.config import settings
from app.core.logging import setup_logging

logger = setup_logging()

class IntegrationService:
    def __init__(self):
        self.client = httpx.AsyncClient(timeout=30.0)
        
    async def get_project_info(self, project_id: str) -> Optional[Dict[str, Any]]:
        """Get project information from MCP Server"""
        try:
            response = await self.client.get(
                f"{settings.MCP_SERVER_URL}/api/v1/projects/{project_id}"
            )
            if response.status_code == 200:
                return response.json()
            return None
        except Exception as e:
            logger.error(f"Failed to get project info: {e}")
            return None
            
    async def get_domain_schema(self, project_id: str) -> Optional[Dict[str, Any]]:
        """Get domain schema from Domain/Schema Service"""
        try:
            response = await self.client.get(
                f"{settings.DOMAIN_SCHEMA_SERVICE_URL}/api/v1/schema/{project_id}"
            )
            if response.status_code == 200:
                return response.json()
            return None
        except Exception as e:
            logger.error(f"Failed to get domain schema: {e}")
            return None
            
    async def get_backend_config(self, project_id: str) -> Optional[Dict[str, Any]]:
        """Get backend configuration from Backend Service"""
        try:
            response = await self.client.get(
                f"{settings.BACKEND_SERVICE_URL}/api/v1/config/{project_id}"
            )
            if response.status_code == 200:
                return response.json()
            return None
        except Exception as e:
            logger.error(f"Failed to get backend config: {e}")
            return None
            
    async def get_frontend_config(self, project_id: str) -> Optional[Dict[str, Any]]:
        """Get frontend configuration from Design/Frontend Service"""
        try:
            response = await self.client.get(
                f"{settings.DESIGN_FRONTEND_SERVICE_URL}/api/v1/config/{project_id}"
            )
            if response.status_code == 200:
                return response.json()
            return None
        except Exception as e:
            logger.error(f"Failed to get frontend config: {e}")
            return None
            
    async def notify_infra_ready(self, project_id: str, infra_type: str) -> bool:
        """Notify MCP Server that infrastructure is ready"""
        try:
            response = await self.client.post(
                f"{settings.MCP_SERVER_URL}/api/v1/notifications",
                json={
                    "project_id": project_id,
                    "event": "infra_ready",
                    "type": infra_type,
                    "service": settings.SERVICE_NAME
                }
            )
            return response.status_code == 200
        except Exception as e:
            logger.error(f"Failed to notify infra ready: {e}")
            return False
            
    async def close(self):
        """Close HTTP client"""
        await self.client.aclose()

integration_service = IntegrationService()
