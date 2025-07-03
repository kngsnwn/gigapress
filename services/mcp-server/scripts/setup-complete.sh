#!/bin/bash

echo "🚀 Complete MCP Server Setup"
echo "============================"

# Navigate to project root
cd "$(dirname "$0")/.."

# Step 1: Build the project
echo "Step 1: Building project..."
./scripts/build.sh
if [ $? -ne 0 ]; then exit 1; fi

# Step 2: Run tests
echo -e "\nStep 2: Running tests..."
./scripts/test.sh
if [ $? -ne 0 ]; then exit 1; fi

# Step 3: Build Docker image
echo -e "\nStep 3: Building Docker image..."
./scripts/docker-build.sh
if [ $? -ne 0 ]; then exit 1; fi

echo -e "\n✅ MCP Server setup completed successfully!"
echo ""
echo "📋 Next steps:"
echo "1. Run locally: ./scripts/run.sh"
echo "2. Run with Docker: docker-compose up"
echo "3. Test APIs: ./scripts/test-api.sh"
echo "4. View API docs: http://localhost:8082/swagger-ui.html"
