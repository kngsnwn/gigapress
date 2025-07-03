#!/bin/bash

echo "📦 Generating Gradle Wrapper..."

if command -v gradle &> /dev/null; then
    gradle wrapper --gradle-version 8.5 --distribution-type all
    echo "✅ Gradle wrapper generated successfully!"
else
    echo "❌ Gradle is not installed. Please install Gradle first."
    echo "Visit: https://gradle.org/install/"
fi
