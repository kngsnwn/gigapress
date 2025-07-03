from fastapi import APIRouter, Response
from typing import Dict, Any
from app.utils.health import get_health_status

router = APIRouter()


@router.get("/status")
async def health_status(response: Response) -> Dict[str, Any]:
    """Get detailed health status"""
    health = await get_health_status()
    
    # Set appropriate status code
    if health["status"] == "unhealthy":
        response.status_code = 503
    elif health["status"] == "degraded":
        response.status_code = 200  # Still return 200 for degraded
    
    return health


@router.get("/ready")
async def readiness_check() -> Dict[str, bool]:
    """Kubernetes readiness probe"""
    health = await get_health_status()
    return {"ready": health["status"] != "unhealthy"}


@router.get("/live")
async def liveness_check() -> Dict[str, bool]:
    """Kubernetes liveness probe"""
    return {"alive": True}
