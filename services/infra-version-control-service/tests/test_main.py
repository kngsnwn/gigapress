"""Basic tests for Infra/Version Control Service"""
import pytest
from fastapi.testclient import TestClient
from main import app

client = TestClient(app)

def test_health_check():
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "healthy"

def test_docker_dockerfile_generation():
    response = client.post("/api/v1/docker/dockerfile", json={
        "project_id": "test-project",
        "service_name": "test-service",
        "service_type": "backend",
        "framework": "express",
        "ports": [3000]
    })
    assert response.status_code == 200
    assert "dockerfile" in response.json()["data"]

def test_kubernetes_manifests_generation():
    response = client.post("/api/v1/kubernetes/manifests", json={
        "project_id": "test-project",
        "environment": "dev",
        "services": [{
            "name": "test-service",
            "image": "test-image:latest",
            "ports": [8080]
        }]
    })
    assert response.status_code == 200
    assert "manifests" in response.json()["data"]

def test_cicd_pipeline_generation():
    response = client.post("/api/v1/cicd/pipeline", json={
        "project_id": "test-project",
        "pipeline_type": "github-actions",
        "build_steps": [{
            "type": "backend",
            "framework": "express"
        }]
    })
    assert response.status_code == 200
    assert "pipeline" in response.json()["data"]

def test_git_init():
    response = client.post("/api/v1/git/init", json={
        "project_id": "test-project",
        "repo_name": "test-repo",
        "include_readme": True,
        "gitignore_template": "node",
        "license": "MIT",
        "initial_commit_message": "Initial commit"
    })
    assert response.status_code == 200
    assert response.json()["success"] == True

def test_terraform_generation():
    response = client.post("/api/v1/terraform/generate", json={
        "project_id": "test-project",
        "cloud_provider": "aws",
        "infrastructure_type": "kubernetes",
        "regions": ["us-east-1"]
    })
    assert response.status_code == 200
    assert "files" in response.json()["data"]

def test_monitoring_setup():
    response = client.post("/api/v1/monitoring/setup", json={
        "project_id": "test-project",
        "monitoring_stack": ["prometheus", "grafana"],
        "metrics_endpoints": ["/metrics"],
        "log_aggregation": True,
        "tracing": False,
        "alerting_channels": ["email"]
    })
    assert response.status_code == 200
    assert "files" in response.json()["data"]
