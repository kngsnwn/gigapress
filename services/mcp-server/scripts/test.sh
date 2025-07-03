#!/bin/bash

echo "🧪 Running MCP Server tests..."

# Navigate to project root
cd "$(dirname "$0")/.."

# Run tests
./gradlew test

if [ $? -eq 0 ]; then
    echo "✅ All tests passed!"
    echo "📊 Test report: build/reports/tests/test/index.html"
else
    echo "❌ Tests failed!"
    exit 1
fi
