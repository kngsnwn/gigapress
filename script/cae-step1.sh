#!/bin/bash

# Step 1: Project Structure Setup for Conversational AI Engine
echo "🚀 Setting up Conversational AI Engine - Step 1: Project Structure"

# Create project directory
mkdir -p conversational-ai-engine
cd conversational-ai-engine

# Create directory structure
echo "📁 Creating directory structure..."
mkdir -p {app/{api/endpoints,core,services,models,schemas,utils},tests,config,logs}

# Create requirements.txt
echo "📝 Creating requirements.txt..."
cat > requirements.txt << 'EOF'
# Core Framework
fastapi==0.104.1
uvicorn[standard]==0.24.0
python-dotenv==1.0.0
pydantic==2.5.0
pydantic-settings==2.1.0

# LangChain and AI
langchain==0.1.0
langchain-openai==0.0.2
langchain-community==0.0.10
openai==1.6.1
tiktoken==0.5.2

# Database and Caching
redis==5.0.1
motor==3.3.2
pymongo==4.6.1

# Kafka
aiokafka==0.10.0
confluent-kafka==2.3.0

# HTTP Client
httpx==0.25.2
aiohttp==3.9.1

# WebSocket
python-socketio==5.10.0
websockets==12.0

# Utilities
python-json-logger==2.0.7
prometheus-client==0.19.0
python-multipart==0.0.6

# Development
pytest==7.4.3
pytest-asyncio==0.21.1
black==23.11.0
flake8==6.1.0
mypy==1.7.1

# API Documentation
fastapi-pagination==0.12.13
EOF

# Create .env file
echo "🔐 Creating .env file..."
cat > .env << 'EOF'
# Server Configuration
APP_NAME=conversational-ai-engine
APP_VERSION=1.0.0
APP_PORT=8087
APP_HOST=0.0.0.0
ENVIRONMENT=development
DEBUG=true

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=redis123
REDIS_DB=0

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_CONSUMER_GROUP=conversational-ai-group
KAFKA_TOPICS=project-updates,conversation-events

# MCP Server Configuration
MCP_SERVER_URL=http://localhost:8082
MCP_SERVER_TIMEOUT=30

# LangChain Configuration
OPENAI_API_KEY=your-openai-api-key-here
LANGCHAIN_TRACING_V2=true
LANGCHAIN_API_KEY=your-langchain-api-key-here
LANGCHAIN_PROJECT=gigapress-conversational-ai

# Model Configuration
DEFAULT_MODEL=gpt-4-turbo-preview
TEMPERATURE=0.7
MAX_TOKENS=2000

# Logging Configuration
LOG_LEVEL=INFO
LOG_FORMAT=json

# CORS Configuration
CORS_ORIGINS=["http://localhost:8080", "http://localhost:3000"]
CORS_ALLOW_CREDENTIALS=true
CORS_ALLOW_METHODS=["*"]
CORS_ALLOW_HEADERS=["*"]
EOF

# Create .gitignore
echo "📄 Creating .gitignore..."
cat > .gitignore << 'EOF'
# Python
__pycache__/
*.py[cod]
*$py.class
*.so
.Python
env/
venv/
ENV/
.venv
pip-log.txt
pip-delete-this-directory.txt
.pytest_cache/
*.cover
.coverage
.coverage.*
htmlcov/
.tox/
.nox/
*.egg-info/
dist/
build/

# IDE
.idea/
.vscode/
*.swp
*.swo
*~

# Environment
.env
.env.local
.env.*.local

# Logs
logs/
*.log

# OS
.DS_Store
Thumbs.db

# Project specific
data/
tmp/
EOF

# Create Dockerfile
echo "🐳 Creating Dockerfile..."
cat > Dockerfile << 'EOF'
FROM python:3.11-slim

WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y \
    gcc \
    g++ \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy application
COPY . .

# Create non-root user
RUN useradd -m -u 1000 appuser && chown -R appuser:appuser /app
USER appuser

# Expose port
EXPOSE 8087

# Run application
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8087", "--reload"]
EOF

# Create docker-compose.yml for this service
echo "🐳 Creating docker-compose.yml..."
cat > docker-compose.yml << 'EOF'
version: '3.8'

services:
  conversational-ai-engine:
    build: .
    container_name: gigapress-conversational-ai-engine
    ports:
      - "8087:8087"
    environment:
      - REDIS_HOST=host.docker.internal
      - KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092
      - MCP_SERVER_URL=http://host.docker.internal:8082
    volumes:
      - ./logs:/app/logs
      - ./.env:/app/.env
    networks:
      - gigapress-network
    depends_on:
      - redis
      - kafka
    restart: unless-stopped

networks:
  gigapress-network:
    external: true

# Note: Redis and Kafka are already running from the main docker-compose.yml
EOF

# Create README.md
echo "📚 Creating README.md..."
cat > README.md << 'EOF'
# Conversational AI Engine

## Overview
The Conversational AI Engine is the natural language processing component of the GigaPress system. It handles user conversations, understands intent, manages context, and coordinates with other services to generate and modify projects.

## Features
- Natural Language Understanding (NLU)
- Conversation Context Management
- Intent Recognition and Classification
- Integration with LangChain for advanced AI capabilities
- Real-time communication with MCP Server
- Event-driven architecture with Kafka
- WebSocket support for real-time updates

## Technology Stack
- **Framework**: FastAPI (Python)
- **AI/NLP**: LangChain, OpenAI
- **Caching**: Redis
- **Message Queue**: Kafka
- **WebSocket**: Socket.IO

## Getting Started

### Prerequisites
- Python 3.11+
- Redis
- Kafka
- Running MCP Server (port 8082)

### Installation
```bash
# Create virtual environment
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt

# Set up environment variables
cp .env.example .env
# Edit .env with your configuration
```

### Running the Service
```bash
# Development mode
uvicorn app.main:app --reload --port 8087

# Production mode
uvicorn app.main:app --host 0.0.0.0 --port 8087 --workers 4
```

### Using Docker
```bash
# Build and run
docker-compose up -d

# View logs
docker-compose logs -f
```

## API Documentation
Once running, visit:
- Swagger UI: http://localhost:8087/docs
- ReDoc: http://localhost:8087/redoc

## Architecture
The service follows a layered architecture:
- **API Layer**: FastAPI endpoints
- **Service Layer**: Business logic and LangChain integration
- **Integration Layer**: MCP Server and Kafka communication
- **Data Layer**: Redis for caching and session management
EOF

# Create pytest.ini
echo "🧪 Creating pytest.ini..."
cat > pytest.ini << 'EOF'
[tool:pytest]
testpaths = tests
python_files = test_*.py
python_classes = Test*
python_functions = test_*
addopts = -v --tb=short --strict-markers --cov=app --cov-report=term-missing
markers =
    unit: Unit tests
    integration: Integration tests
    e2e: End-to-end tests
EOF

# Create __init__.py files
echo "🐍 Creating __init__.py files..."
touch app/__init__.py
touch app/api/__init__.py
touch app/api/endpoints/__init__.py
touch app/core/__init__.py
touch app/services/__init__.py
touch app/models/__init__.py
touch app/schemas/__init__.py
touch app/utils/__init__.py
touch tests/__init__.py
touch config/__init__.py

# Create config files
echo "⚙️ Creating configuration files..."
cat > config/settings.py << 'EOF'
from pydantic_settings import BaseSettings
from typing import List
import os


class Settings(BaseSettings):
    # Application
    app_name: str = "conversational-ai-engine"
    app_version: str = "1.0.0"
    app_port: int = 8087
    app_host: str = "0.0.0.0"
    environment: str = "development"
    debug: bool = True
    
    # Redis
    redis_host: str = "localhost"
    redis_port: int = 6379
    redis_password: str = "redis123"
    redis_db: int = 0
    
    # Kafka
    kafka_bootstrap_servers: str = "localhost:9092"
    kafka_consumer_group: str = "conversational-ai-group"
    kafka_topics: List[str] = ["project-updates", "conversation-events"]
    
    # MCP Server
    mcp_server_url: str = "http://localhost:8082"
    mcp_server_timeout: int = 30
    
    # LangChain
    openai_api_key: str = ""
    langchain_tracing_v2: bool = True
    langchain_api_key: str = ""
    langchain_project: str = "gigapress-conversational-ai"
    
    # Model Configuration
    default_model: str = "gpt-4-turbo-preview"
    temperature: float = 0.7
    max_tokens: int = 2000
    
    # Logging
    log_level: str = "INFO"
    log_format: str = "json"
    
    # CORS
    cors_origins: List[str] = ["http://localhost:8080", "http://localhost:3000"]
    cors_allow_credentials: bool = True
    cors_allow_methods: List[str] = ["*"]
    cors_allow_headers: List[str] = ["*"]
    
    class Config:
        env_file = ".env"
        case_sensitive = False


# Create singleton instance
settings = Settings()
EOF

echo "✅ Step 1 completed! Project structure created successfully."
echo "📊 Created:"
echo "   - Directory structure"
echo "   - requirements.txt with all dependencies"
echo "   - .env file with configurations"
echo "   - Docker setup files"
echo "   - Configuration module"
echo "   - README.md documentation"

echo ""
echo "Next step: Run setup_step2_fastapi_server.sh"