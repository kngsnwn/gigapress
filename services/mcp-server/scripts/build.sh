#!/bin/bash

echo "🔨 Building MCP Server..."

# Navigate to project root
cd "$(dirname "$0")/.."

# Clean and build
./gradlew clean build

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo "📦 JAR location: build/libs/"
else
    echo "❌ Build failed!"
    exit 1
fi
