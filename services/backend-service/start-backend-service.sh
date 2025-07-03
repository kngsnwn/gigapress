#!/bin/bash

echo "Starting Backend Service..."

# Build the service
./gradlew clean build

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "Build successful. Starting service..."
    ./gradlew bootRun
else
    echo "Build failed. Please check the errors above."
    exit 1
fi
