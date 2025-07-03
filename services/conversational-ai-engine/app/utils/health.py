from typing import Dict, Any
import aioredis
import httpx
from datetime import datetime
from config.settings import settings
import logging

logger = logging.getLogger(__name__)


async def check_redis_health() -> Dict[str, Any]:
    """Check Redis connection health"""
    try:
        redis = await aioredis.from_url(
            f"redis://{settings.redis_host}:{settings.redis_port}",
            password=settings.redis_password,
            decode_responses=True
        )
        await redis.ping()
        await redis.close()
        return {"status": "healthy", "message": "Redis connection successful"}
    except Exception as e:
        logger.error(f"Redis health check failed: {str(e)}")
        return {"status": "unhealthy", "message": str(e)}


async def check_mcp_server_health() -> Dict[str, Any]:
    """Check MCP Server connection health"""
    try:
        async with httpx.AsyncClient() as client:
            response = await client.get(
                f"{settings.mcp_server_url}/health",
                timeout=5.0
            )
            if response.status_code == 200:
                return {"status": "healthy", "message": "MCP Server reachable"}
            else:
                return {
                    "status": "unhealthy",
                    "message": f"MCP Server returned {response.status_code}"
                }
    except Exception as e:
        logger.error(f"MCP Server health check failed: {str(e)}")
        return {"status": "unhealthy", "message": str(e)}


async def get_health_status() -> Dict[str, Any]:
    """Get comprehensive health status"""
    redis_health = await check_redis_health()
    mcp_health = await check_mcp_server_health()
    
    overall_status = "healthy"
    if redis_health["status"] == "unhealthy" or mcp_health["status"] == "unhealthy":
        overall_status = "degraded"
    
    return {
        "status": overall_status,
        "timestamp": datetime.utcnow().isoformat(),
        "service": settings.app_name,
        "version": settings.app_version,
        "environment": settings.environment,
        "checks": {
            "redis": redis_health,
            "mcp_server": mcp_health
        }
    }
