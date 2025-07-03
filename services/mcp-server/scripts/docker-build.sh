#!/bin/bash

echo "🐳 Building Docker image for MCP Server..."

# Navigate to project root
cd "$(dirname "$0")/.."

# Build Docker image
docker build -t gigapress/mcp-server:latest .

if [ $? -eq 0 ]; then
    echo "✅ Docker image built successfully!"
    echo "📦 Image: gigapress/mcp-server:latest"
else
    echo "❌ Docker build failed!"
    exit 1
fi
