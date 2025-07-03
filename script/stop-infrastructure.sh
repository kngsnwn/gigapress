#!/bin/bash

echo "🛑 Stopping GigaPress Infrastructure..."
docker-compose down

echo "✅ Infrastructure stopped!"
echo ""
echo "To remove volumes as well, run:"
echo "  docker-compose down -v"
