# Infra/Version Control Service

Infrastructure and Version Control Service for GigaPress - handles Docker, Kubernetes, CI/CD, Git operations.

## Features

- **Docker Configuration**: Generate Dockerfiles and docker-compose.yml
- **Kubernetes Manifests**: Create K8s deployments, services, ingress
- **CI/CD Pipelines**: GitHub Actions, Jenkins, GitLab CI
- **Git Operations**: Repository management, branching, commits
- **Terraform/IaC**: Infrastructure as Code generation
- **Monitoring Setup**: Prometheus, Grafana configurations

## API Endpoints

### Docker
- `POST /api/v1/docker/dockerfile` - Generate Dockerfile
- `POST /api/v1/docker/docker-compose` - Generate docker-compose.yml
- `POST /api/v1/docker/dockerignore` - Generate .dockerignore

### Kubernetes
- `POST /api/v1/kubernetes/manifests` - Generate K8s manifests
- `POST /api/v1/kubernetes/configmap` - Generate ConfigMap
- `POST /api/v1/kubernetes/secret` - Generate Secret

### CI/CD
- `POST /api/v1/cicd/pipeline` - Generate CI/CD pipeline
- `GET /api/v1/cicd/templates/{type}` - Get pipeline templates

### Git
- `POST /api/v1/git/init` - Initialize repository
- `POST /api/v1/git/commit` - Create commit
- `POST /api/v1/git/branch` - Create branch
- `GET /api/v1/git/branches/{project_id}/{repo_name}` - List branches

### Terraform
- `POST /api/v1/terraform/generate` - Generate Terraform config
- `GET /api/v1/terraform/modules/{provider}` - Get available modules

### Monitoring
- `POST /api/v1/monitoring/setup` - Generate monitoring setup
- `GET /api/v1/monitoring/metrics/endpoints` - Get metrics endpoints

### Orchestration
- `POST /api/v1/orchestration/generate-complete-infra` - Generate complete infrastructure
- `GET /api/v1/orchestration/status/{project_id}` - Get generation status

## Running the Service

```bash
# Install dependencies
pip install -r requirements.txt

# Run locally
python main.py

# Run with Docker
docker build -t infra-version-control-service .
docker run -p 8086:8086 infra-version-control-service

# Run with Docker Compose
docker-compose up -d
```

## Environment Variables

- `SERVICE_PORT`: Service port (default: 8086)
- `REDIS_HOST`: Redis host
- `REDIS_PORT`: Redis port
- `REDIS_PASSWORD`: Redis password
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka servers
- `MCP_SERVER_URL`: MCP Server URL
- `DOMAIN_SCHEMA_SERVICE_URL`: Domain/Schema Service URL
- `BACKEND_SERVICE_URL`: Backend Service URL
- `DESIGN_FRONTEND_SERVICE_URL`: Design/Frontend Service URL

## Testing

```bash
# Run tests
pytest tests/

# Run with coverage
pytest --cov=app tests/
```

## Project Structure

```
infra-version-control-service/
├── app/
│   ├── api/
│   │   └── endpoints/      # API endpoints
│   ├── core/              # Core configuration
│   ├── models/            # Data models
│   ├── schemas/           # Pydantic schemas
│   ├── services/          # Business logic
│   └── templates/         # Jinja2 templates
├── tests/                 # Test files
├── logs/                  # Log files
├── repositories/          # Git repositories
├── requirements.txt       # Python dependencies
├── Dockerfile            # Docker configuration
├── docker-compose.yml    # Docker Compose config
└── README.md            # This file
```

## API Documentation

When the service is running, you can access:
- Swagger UI: http://localhost:8086/docs
- ReDoc: http://localhost:8086/redoc

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is part of the GigaPress system.
