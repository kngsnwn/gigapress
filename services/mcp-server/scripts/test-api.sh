#!/bin/bash

# API Test Script for MCP Server

BASE_URL="http://localhost:8082"

echo "🧪 Testing MCP Server APIs..."
echo "================================"

# Test 1: Health Check
echo "1. Testing Health Check..."
curl -s "$BASE_URL/api/tools/health" | jq '.'
echo ""

# Test 2: Generate Project
echo "2. Testing Project Generation..."
PROJECT_RESPONSE=$(curl -s -X POST "$BASE_URL/api/tools/generate" \
  -H "Content-Type: application/json" \
  -d '{
    "project_name": "Test Shopping Mall",
    "project_description": "E-commerce platform with modern features",
    "project_type": "WEB_APPLICATION",
    "technology_stack": {
      "frontend": "react",
      "backend": "node",
      "database": "postgresql"
    },
    "features": ["Authentication", "Product Catalog", "Shopping Cart", "Payment"]
  }')

echo "$PROJECT_RESPONSE" | jq '.'
PROJECT_ID=$(echo "$PROJECT_RESPONSE" | jq -r '.data.project_id')
echo "Generated Project ID: $PROJECT_ID"
echo ""

# Test 3: Analyze Change Impact
echo "3. Testing Change Analysis..."
curl -s -X POST "$BASE_URL/api/tools/analyze" \
  -H "Content-Type: application/json" \
  -d "{
    \"project_id\": \"$PROJECT_ID\",
    \"change_description\": \"Add user review feature\",
    \"change_type\": \"FEATURE_ADD\",
    \"target_components\": [\"backend\", \"frontend\"],
    \"analysis_depth\": \"NORMAL\"
  }" | jq '.'
echo ""

# Test 4: Update Components
echo "4. Testing Component Update..."
curl -s -X PUT "$BASE_URL/api/tools/update" \
  -H "Content-Type: application/json" \
  -d "{
    \"project_id\": \"$PROJECT_ID\",
    \"updates\": [
      {
        \"component_id\": \"auth-service\",
        \"update_type\": \"MODIFY\",
        \"update_content\": {
          \"feature\": \"two-factor-auth\"
        }
      }
    ],
    \"update_strategy\": \"INCREMENTAL\"
  }" | jq '.'
echo ""

# Test 5: Validate Consistency
echo "5. Testing Validation..."
curl -s -X POST "$BASE_URL/api/tools/validate" \
  -H "Content-Type: application/json" \
  -d "{
    \"project_id\": \"$PROJECT_ID\",
    \"validation_types\": [
      \"DEPENDENCY_CONSISTENCY\",
      \"CODE_QUALITY\",
      \"API_CONTRACT\"
    ],
    \"include_warnings\": true
  }" | jq '.'
echo ""

echo "✅ API tests completed!"
echo ""
echo "📚 For interactive API testing, visit:"
echo "   Swagger UI: $BASE_URL/swagger-ui.html"
