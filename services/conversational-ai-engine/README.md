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
