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
