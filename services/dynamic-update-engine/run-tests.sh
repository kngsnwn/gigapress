#!/bin/bash

echo "🧪 Running tests..."

# Check if gradle wrapper exists
if [ -f "./gradlew" ]; then
    echo "Using gradle wrapper..."
    GRADLE_CMD="./gradlew"
else
    echo "Gradle wrapper not found, using system gradle..."
    GRADLE_CMD="gradle"
fi

# Run tests
$GRADLE_CMD test

if [ $? -eq 0 ]; then
    echo "✅ All tests passed!"
else
    echo "❌ Some tests failed!"
    exit 1
fi
