#!/bin/bash

echo "🏗️ Building Dynamic Update Engine..."

# Check if gradle wrapper exists
if [ -f "./gradlew" ]; then
    echo "Using gradle wrapper..."
    GRADLE_CMD="./gradlew"
else
    echo "Gradle wrapper not found, using system gradle..."
    GRADLE_CMD="gradle"
fi

# Clean and build
$GRADLE_CMD clean build

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo "🚀 Starting application..."
    $GRADLE_CMD bootRun
else
    echo "❌ Build failed!"
    exit 1
fi
