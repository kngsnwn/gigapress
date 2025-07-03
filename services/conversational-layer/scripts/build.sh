#!/bin/bash

echo "Building Conversational Layer for production..."

# Install dependencies
npm ci --only=production

# Build Next.js
npm run build

# Create standalone output
cp -r .next/standalone ./
cp -r .next/static ./.next/
cp -r public ./

echo "Build complete!"
