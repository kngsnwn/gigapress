#!/bin/bash

# Step 2: FastAPI Server Basic Implementation
echo "🚀 Setting up Conversational AI Engine - Step 2: FastAPI Server"

cd conversational-ai-engine

# Create main application file
echo "📝 Creating main.py..."
cat > app/main.py << 'EOF'
from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from contextlib import asynccontextmanager
import logging
import sys
from typing import Dict, Any

from config.settings import settings
from app.core.logging import setup_logging
from app.api.router import api_router
from app.core.exceptions import AppException
from app.utils.health import get_health_status


# Setup logging
logger = setup_logging()


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Application lifespan manager"""
    logger.info(f"Starting {settings.app_name} v{settings.app_version}")
    logger.info(f"Environment: {settings.environment}")
    logger.info(f"Debug mode: {settings.debug}")
    
    # Startup tasks
    try:
        # Initialize connections, services, etc.
        logger.info("Initializing application services...")
        yield
    finally:
        # Shutdown tasks
        logger.info("Shutting down application...")


# Create FastAPI app
app = FastAPI(
    title=settings.app_name,
    version=settings.app_version,
    description="AI-powered conversational engine for GigaPress project generation",
    lifespan=lifespan,
    debug=settings.debug,
    docs_url="/docs" if settings.debug else None,
    redoc_url="/redoc" if settings.debug else None,
)

# Configure CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins,
    allow_credentials=settings.cors_allow_credentials,
    allow_methods=settings.cors_allow_methods,
    allow_headers=settings.cors_allow_headers,
)


# Global exception handler
@app.exception_handler(AppException)
async def app_exception_handler(request: Request, exc: AppException):
    logger.error(f"Application error: {exc.message}", extra={"status_code": exc.status_code})
    return JSONResponse(
        status_code=exc.status_code,
        content={"error": exc.message, "details": exc.details}
    )


@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    logger.exception("Unhandled exception occurred")
    return JSONResponse(
        status_code=500,
        content={"error": "Internal server error", "message": str(exc) if settings.debug else None}
    )


# Root endpoint
@app.get("/", tags=["Root"])
async def root() -> Dict[str, Any]:
    """Root endpoint"""
    return {
        "service": settings.app_name,
        "version": settings.app_version,
        "status": "running",
        "environment": settings.environment
    }


# Health check endpoint
@app.get("/health", tags=["Health"])
async def health_check() -> Dict[str, Any]:
    """Health check endpoint"""
    return await get_health_status()


# Include API routes
app.include_router(api_router, prefix="/api/v1")


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "app.main:app",
        host=settings.app_host,
        port=settings.app_port,
        reload=settings.debug,
        log_level=settings.log_level.lower()
    )
EOF

# Create logging configuration
echo "📝 Creating logging configuration..."
cat > app/core/logging.py << 'EOF'
import logging
import sys
import json
from pythonjsonlogger import jsonlogger
from config.settings import settings


class CustomJsonFormatter(jsonlogger.JsonFormatter):
    """Custom JSON formatter for structured logging"""
    
    def add_fields(self, log_record, record, message_dict):
        super(CustomJsonFormatter, self).add_fields(log_record, record, message_dict)
        log_record['service'] = settings.app_name
        log_record['environment'] = settings.environment
        log_record['level'] = record.levelname
        log_record['logger'] = record.name


def setup_logging():
    """Configure application logging"""
    # Get root logger
    logger = logging.getLogger()
    logger.setLevel(getattr(logging, settings.log_level.upper()))
    
    # Remove existing handlers
    for handler in logger.handlers[:]:
        logger.removeHandler(handler)
    
    # Console handler
    console_handler = logging.StreamHandler(sys.stdout)
    
    if settings.log_format == "json":
        formatter = CustomJsonFormatter(
            '%(timestamp)s %(level)s %(name)s %(message)s',
            timestamp=True
        )
    else:
        formatter = logging.Formatter(
            '%(asctime)s - %(name)s - %(levelname)s - %(message)s'
        )
    
    console_handler.setFormatter(formatter)
    logger.addHandler(console_handler)
    
    # File handler (optional)
    if settings.environment == "production":
        file_handler = logging.FileHandler('logs/app.log')
        file_handler.setFormatter(formatter)
        logger.addHandler(file_handler)
    
    # Suppress noisy loggers
    logging.getLogger("uvicorn.access").setLevel(logging.WARNING)
    logging.getLogger("httpx").setLevel(logging.WARNING)
    
    return logger
EOF

# Create exception handlers
echo "📝 Creating exception handlers..."
cat > app/core/exceptions.py << 'EOF'
from typing import Optional, Dict, Any


class AppException(Exception):
    """Base application exception"""
    
    def __init__(
        self,
        message: str,
        status_code: int = 500,
        details: Optional[Dict[str, Any]] = None
    ):
        self.message = message
        self.status_code = status_code
        self.details = details or {}
        super().__init__(message)


class ValidationException(AppException):
    """Validation exception"""
    
    def __init__(self, message: str, details: Optional[Dict[str, Any]] = None):
        super().__init__(message, status_code=400, details=details)


class NotFoundException(AppException):
    """Resource not found exception"""
    
    def __init__(self, message: str = "Resource not found", details: Optional[Dict[str, Any]] = None):
        super().__init__(message, status_code=404, details=details)


class AuthenticationException(AppException):
    """Authentication exception"""
    
    def __init__(self, message: str = "Authentication failed", details: Optional[Dict[str, Any]] = None):
        super().__init__(message, status_code=401, details=details)


class AuthorizationException(AppException):
    """Authorization exception"""
    
    def __init__(self, message: str = "Unauthorized", details: Optional[Dict[str, Any]] = None):
        super().__init__(message, status_code=403, details=details)


class ExternalServiceException(AppException):
    """External service exception"""
    
    def __init__(self, service: str, message: str, details: Optional[Dict[str, Any]] = None):
        super().__init__(
            f"{service} error: {message}",
            status_code=502,
            details=details
        )


class RateLimitException(AppException):
    """Rate limit exception"""
    
    def __init__(self, message: str = "Rate limit exceeded", details: Optional[Dict[str, Any]] = None):
        super().__init__(message, status_code=429, details=details)
EOF

# Create API router
echo "📝 Creating API router..."
cat > app/api/router.py << 'EOF'
from fastapi import APIRouter
from app.api.endpoints import health, conversation

api_router = APIRouter()

# Include endpoint routers
api_router.include_router(health.router, prefix="/health", tags=["Health"])
api_router.include_router(conversation.router, prefix="/conversation", tags=["Conversation"])
EOF

# Create health utility
echo "📝 Creating health check utility..."
cat > app/utils/health.py << 'EOF'
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
EOF

# Create health endpoint
echo "📝 Creating health endpoint..."
mkdir -p app/api/endpoints
cat > app/api/endpoints/health.py << 'EOF'
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
EOF

# Create basic conversation endpoint
echo "📝 Creating basic conversation endpoint..."
cat > app/api/endpoints/conversation.py << 'EOF'
from fastapi import APIRouter, HTTPException
from typing import Dict, Any
from pydantic import BaseModel, Field
from datetime import datetime
import uuid

router = APIRouter()


class ConversationRequest(BaseModel):
    """Conversation request model"""
    message: str = Field(..., description="User message")
    session_id: str = Field(default_factory=lambda: str(uuid.uuid4()), description="Session ID")
    context: Dict[str, Any] = Field(default_factory=dict, description="Additional context")


class ConversationResponse(BaseModel):
    """Conversation response model"""
    response: str = Field(..., description="AI response")
    session_id: str = Field(..., description="Session ID")
    timestamp: datetime = Field(default_factory=datetime.utcnow)
    metadata: Dict[str, Any] = Field(default_factory=dict)


@router.post("/chat", response_model=ConversationResponse)
async def chat(request: ConversationRequest) -> ConversationResponse:
    """Process a conversation message"""
    # Placeholder implementation
    return ConversationResponse(
        response=f"Echo: {request.message} (This is a placeholder response)",
        session_id=request.session_id,
        metadata={
            "model": "placeholder",
            "tokens": len(request.message.split()),
            "processing_time": 0.0
        }
    )


@router.get("/sessions/{session_id}")
async def get_session(session_id: str) -> Dict[str, Any]:
    """Get session information"""
    # Placeholder implementation
    return {
        "session_id": session_id,
        "created_at": datetime.utcnow().isoformat(),
        "messages": [],
        "context": {}
    }


@router.delete("/sessions/{session_id}")
async def clear_session(session_id: str) -> Dict[str, str]:
    """Clear a conversation session"""
    # Placeholder implementation
    return {
        "message": f"Session {session_id} cleared",
        "status": "success"
    }
EOF

# Create middleware
echo "📝 Creating middleware..."
cat > app/core/middleware.py << 'EOF'
from fastapi import Request
from starlette.middleware.base import BaseHTTPMiddleware
import time
import logging
import uuid

logger = logging.getLogger(__name__)


class RequestLoggingMiddleware(BaseHTTPMiddleware):
    """Log all requests"""
    
    async def dispatch(self, request: Request, call_next):
        request_id = str(uuid.uuid4())
        start_time = time.time()
        
        # Add request ID to state
        request.state.request_id = request_id
        
        # Log request
        logger.info(
            f"Request started",
            extra={
                "request_id": request_id,
                "method": request.method,
                "path": request.url.path,
                "client": request.client.host if request.client else None
            }
        )
        
        # Process request
        response = await call_next(request)
        
        # Calculate duration
        duration = time.time() - start_time
        
        # Log response
        logger.info(
            f"Request completed",
            extra={
                "request_id": request_id,
                "status_code": response.status_code,
                "duration": f"{duration:.3f}s"
            }
        )
        
        # Add headers
        response.headers["X-Request-ID"] = request_id
        response.headers["X-Process-Time"] = str(duration)
        
        return response


class ErrorHandlingMiddleware(BaseHTTPMiddleware):
    """Handle errors gracefully"""
    
    async def dispatch(self, request: Request, call_next):
        try:
            response = await call_next(request)
            return response
        except Exception as e:
            logger.exception("Unhandled error in middleware")
            raise
EOF

# Update requirements with additional packages
echo "📝 Updating requirements.txt..."
cat >> requirements.txt << 'EOF'

# Additional for Step 2
aioredis==2.0.1
starlette==0.27.0
uuid==1.30
EOF

# Create tests for basic functionality
echo "🧪 Creating basic tests..."
cat > tests/test_main.py << 'EOF'
import pytest
from fastapi.testclient import TestClient
from app.main import app


client = TestClient(app)


def test_root_endpoint():
    """Test root endpoint"""
    response = client.get("/")
    assert response.status_code == 200
    data = response.json()
    assert "service" in data
    assert "version" in data
    assert "status" in data


def test_health_endpoint():
    """Test health endpoint"""
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert "status" in data


def test_api_docs():
    """Test API documentation endpoint"""
    response = client.get("/docs")
    assert response.status_code == 200


def test_conversation_chat():
    """Test basic chat endpoint"""
    response = client.post(
        "/api/v1/conversation/chat",
        json={
            "message": "Hello, AI!",
            "session_id": "test-session"
        }
    )
    assert response.status_code == 200
    data = response.json()
    assert "response" in data
    assert "session_id" in data
    assert data["session_id"] == "test-session"
EOF

echo "✅ Step 2 completed! FastAPI server basic implementation done."
echo "📊 Created:"
echo "   - Main FastAPI application"
echo "   - Logging configuration"
echo "   - Exception handlers"
echo "   - API router structure"
echo "   - Health check endpoints"
echo "   - Basic conversation endpoints"
echo "   - Middleware components"
echo "   - Basic tests"

echo ""
echo "🧪 To test the server:"
echo "   cd conversational-ai-engine"
echo "   python -m venv venv"
echo "   source venv/bin/activate"
echo "   pip install -r requirements.txt"
echo "   uvicorn app.main:app --reload --port 8087"

echo ""
echo "Next step: Run setup_step3_langchain_integration.sh"