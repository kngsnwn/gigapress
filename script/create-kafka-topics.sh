#!/bin/bash

echo "📨 Creating Kafka topics for GigaPress..."

# Wait for Kafka to be ready
echo "⏳ Waiting for Kafka to be ready..."
while ! docker exec gigapress-kafka kafka-broker-api-versions --bootstrap-server localhost:9092 >/dev/null 2>&1; do
    sleep 1
done
echo "✅ Kafka is ready!"

# Function to create topic
create_topic() {
    local topic_name=$1
    local partitions=$2
    local replication=$3
    
    docker exec gigapress-kafka kafka-topics \
        --create \
        --bootstrap-server localhost:9092 \
        --topic $topic_name \
        --partitions $partitions \
        --replication-factor $replication \
        --if-not-exists
    
    if [ $? -eq 0 ]; then
        echo "✅ Topic '$topic_name' created successfully"
    else
        echo "❌ Failed to create topic '$topic_name'"
    fi
}

# Create topics
create_topic "project.updates" 3 1
create_topic "component.changes" 3 1
create_topic "dependency.events" 3 1
create_topic "validation.results" 3 1
create_topic "generation.requests" 3 1
create_topic "generation.responses" 3 1

echo -e "\n📋 Listing all topics:"
docker exec gigapress-kafka kafka-topics --bootstrap-server localhost:9092 --list

echo -e "\n✨ Kafka topics initialization complete!"
