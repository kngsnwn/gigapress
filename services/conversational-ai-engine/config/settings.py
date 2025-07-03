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
