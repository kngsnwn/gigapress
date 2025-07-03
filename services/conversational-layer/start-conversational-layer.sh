#!/bin/bash

echo "🚀 GigaPress Conversational Layer Setup"
echo "======================================"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Navigate to services directory
mkdir -p services
cd services

# Check if conversational-layer exists
if [ -d "conversational-layer" ]; then
  echo -e "${BLUE}Conversational Layer directory already exists${NC}"
  cd conversational-layer
else
  echo -e "${GREEN}Creating Conversational Layer...${NC}"
  mkdir conversational-layer
  cd conversational-layer
fi

# Install dependencies
echo -e "${BLUE}Installing dependencies...${NC}"
npm install

# Check if other services are running
echo -e "${BLUE}Checking service dependencies...${NC}"
services=(
  "http://localhost:8081:Dynamic Update Engine"
  "http://localhost:8082:MCP Server"
  "http://localhost:8083:Domain/Schema Service"
  "http://localhost:8084:Backend Service"
  "http://localhost:8085:Design/Frontend Service"
  "http://localhost:8086:Infra/Version Control Service"
  "http://localhost:8087:Conversational AI Engine"
)

all_running=true
for service in "${services[@]}"; do
  IFS=':' read -r -a parts <<< "$service"
  url="${parts[0]}:${parts[1]}"
  name="${parts[2]}"
  
  if curl -s -o /dev/null -w "%{http_code}" "$url/health" | grep -q "200\|404"; then
    echo -e "${GREEN}✓ $name is running${NC}"
  else
    echo -e "${BLUE}✗ $name is not running${NC}"
    all_running=false
  fi
done

if [ "$all_running" = false ]; then
  echo -e "${BLUE}Warning: Some services are not running. The application may not work properly.${NC}"
fi

# Start the application
echo -e "${GREEN}Starting Conversational Layer on port 8080...${NC}"
npm run dev

