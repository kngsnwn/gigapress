#!/bin/bash

echo "🏗️ Building Dynamic Update Engine (without tests)..."

# Check if gradle wrapper exists
if [ -f "./gradlew" ]; then
    echo "Using gradle wrapper..."
    GRADLE_CMD="./gradlew"
else
    echo "Gradle wrapper not found, using system gradle..."
    GRADLE_CMD="gradle"
fi

# Clean and build without tests
$GRADLE_CMD clean build -x test

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo ""
    echo "⚠️ Note: Tests were skipped. To run tests, ensure:"
    echo "  1. Neo4j is running on localhost:7687"
    echo "  2. Kafka is running on localhost:9092"
    echo "  3. Redis is running on localhost:6379"
    echo ""
    echo "Then run: ./run-tests.sh"
else
    echo "❌ Build failed!"
    exit 1
fi
