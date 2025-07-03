#!/bin/bash

echo "🧪 Checking infrastructure before running tests..."

# Check if Neo4j is running
nc -z localhost 7687 2>/dev/null
if [ $? -ne 0 ]; then
    echo "❌ Neo4j is not running on port 7687"
    echo "Please start the infrastructure first: docker-compose up -d"
    exit 1
fi

# Check if Kafka is running
nc -z localhost 9092 2>/dev/null
if [ $? -ne 0 ]; then
    echo "❌ Kafka is not running on port 9092"
    echo "Please start the infrastructure first: docker-compose up -d"
    exit 1
fi

echo "✅ Infrastructure is running"
echo "🧪 Running tests..."

# Run tests with Neo4j enabled
if [ -f "./gradlew" ]; then
    ./gradlew test -Dtest.neo4j.enabled=true
else
    gradle test -Dtest.neo4j.enabled=true
fi
