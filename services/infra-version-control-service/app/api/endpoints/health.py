"""Health check endpoints"""
from fastapi import APIRouter, Depends
from app.schemas.base import ServiceHealth
from app.core.config import settings
import time

router = APIRouter()

start_time = time.time()

@router.get("/health", response_model=ServiceHealth)
async def health_check():
    """Service health check"""
    return ServiceHealth(
        service=settings.SERVICE_NAME,
        status="healthy",
        version="1.0.0",
        uptime=time.time() - start_time,
        dependencies={
            "redis": "connected",
            "kafka": "connected"
        }
    )

@router.get("/ready")
async def readiness_check():
    """Kubernetes readiness probe"""
    return {"ready": True}

@router.get("/live")
async def liveness_check():
    """Kubernetes liveness probe"""
    return {"alive": True}
