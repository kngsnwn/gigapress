#!/bin/bash

echo "🚀 Starting MCP Server..."

# Navigate to project root
cd "$(dirname "$0")/.."

# Check if jar exists
if [ ! -f build/libs/*.jar ]; then
    echo "⚠️  JAR file not found. Building project..."
    ./scripts/build.sh
fi

# Run with dev profile
echo "Starting server on port 8082..."
./gradlew bootRun --args='--spring.profiles.active=dev'
