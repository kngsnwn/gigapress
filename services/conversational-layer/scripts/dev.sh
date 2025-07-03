#!/bin/bash

echo "Starting Conversational Layer in development mode..."

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
  echo "Installing dependencies..."
  npm install
fi

# Start development server
npm run dev
