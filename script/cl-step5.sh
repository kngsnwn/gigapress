#!/bin/bash

# Step 5: UI/UX 마무리 및 최종 설정 (수정본)
echo "🚀 Step 5: Conversational Layer - UI/UX 마무리 및 최종 설정"
echo "=================================================="

cd services/conversational-layer

# 필요한 디렉토리 생성
mkdir -p lib/hooks
mkdir -p components/ui
mkdir -p scripts
mkdir -p public
mkdir -p types

# 애니메이션 유틸리티
cat > lib/animations.ts << 'EOF'
export const fadeIn = {
  initial: { opacity: 0 },
  animate: { opacity: 1 },
  exit: { opacity: 0 },
};

export const slideIn = {
  initial: { x: -20, opacity: 0 },
  animate: { x: 0, opacity: 1 },
  exit: { x: 20, opacity: 0 },
};

export const slideUp = {
  initial: { y: 20, opacity: 0 },
  animate: { y: 0, opacity: 1 },
  exit: { y: -20, opacity: 0 },
};

export const scaleIn = {
  initial: { scale: 0.9, opacity: 0 },
  animate: { scale: 1, opacity: 1 },
  exit: { scale: 0.9, opacity: 0 },
};

export const staggerChildren = {
  animate: {
    transition: {
      staggerChildren: 0.1,
    },
  },
};
EOF

# 반응형 Hook
cat > lib/hooks/useMediaQuery.ts << 'EOF'
'use client'

import { useState, useEffect } from 'react';

export function useMediaQuery(query: string): boolean {
  const [matches, setMatches] = useState(false);

  useEffect(() => {
    const media = window.matchMedia(query);
    if (media.matches !== matches) {
      setMatches(media.matches);
    }

    const listener = (e: MediaQueryListEvent) => setMatches(e.matches);
    media.addEventListener('change', listener);

    return () => media.removeEventListener('change', listener);
  }, [matches, query]);

  return matches;
}

export const useIsMobile = () => useMediaQuery('(max-width: 768px)');
export const useIsTablet = () => useMediaQuery('(max-width: 1024px)');
export const useIsDesktop = () => useMediaQuery('(min-width: 1024px)');
EOF

# 모바일 반응형 ChatInterface
cat > components/chat/ChatInterfaceMobile.tsx << 'EOF'
'use client'

import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useIsMobile } from '@/lib/hooks/useMediaQuery';
import { useConversationStore } from '@/lib/store';
import MessageList from './MessageList';
import InputBox from './InputBox';
import ProgressTracker from '../project/ProgressTracker';
import ProjectStatus from '../project/ProjectStatus';
import { Menu, X, FolderOpen } from 'lucide-react';
import { cn } from '@/lib/utils';
import { slideIn } from '@/lib/animations';

export default function ChatInterfaceMobile() {
  const [showProjects, setShowProjects] = useState(false);
  const { isTyping, progressUpdates, currentProject } = useConversationStore();

  return (
    <div className="flex flex-col h-full relative">
      {/* Mobile Header */}
      <div className="flex items-center justify-between p-4 border-b border-border md:hidden">
        <button
          onClick={() => setShowProjects(!showProjects)}
          className="p-2 rounded-lg hover:bg-accent transition-colors"
        >
          {showProjects ? <X size={20} /> : <Menu size={20} />}
        </button>
        
        {currentProject && (
          <div className="flex-1 mx-4">
            <p className="text-sm font-medium truncate">{currentProject.name}</p>
          </div>
        )}
        
        <button className="p-2 rounded-lg hover:bg-accent transition-colors">
          <FolderOpen size={20} />
        </button>
      </div>

      {/* Mobile Project Drawer */}
      <AnimatePresence>
        {showProjects && (
          <>
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 0.5 }}
              exit={{ opacity: 0 }}
              className="fixed inset-0 bg-black z-40 md:hidden"
              onClick={() => setShowProjects(false)}
            />
            <motion.div
              {...slideIn}
              className="fixed left-0 top-0 bottom-0 w-80 bg-card border-r border-border z-50 md:hidden"
            >
              <div className="p-4 border-b border-border">
                <h2 className="font-semibold">Projects</h2>
              </div>
              <div className="overflow-y-auto">
                <ProjectStatus />
              </div>
            </motion.div>
          </>
        )}
      </AnimatePresence>

      {/* Progress Tracker */}
      {progressUpdates.length > 0 && (
        <motion.div
          initial={{ height: 0 }}
          animate={{ height: 'auto' }}
          exit={{ height: 0 }}
          className="border-b border-border overflow-hidden"
        >
          <ProgressTracker />
        </motion.div>
      )}

      {/* Messages Area */}
      <div className="flex-1 overflow-hidden">
        <MessageList />
      </div>

      {/* Typing Indicator */}
      <AnimatePresence>
        {isTyping && (
          <motion.div
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 10 }}
            className="px-4 py-2 text-sm text-muted-foreground"
          >
            AI is typing...
          </motion.div>
        )}
      </AnimatePresence>

      {/* Input Area */}
      <div className="border-t border-border p-4">
        <InputBox />
      </div>
    </div>
  );
}
EOF

# 애니메이션이 추가된 메인 페이지
cat > app/page-animated.tsx << 'EOF'
'use client'

import { useEffect } from 'react';
import { motion } from 'framer-motion';
import { websocketService } from '@/lib/websocket';
import { useIsMobile } from '@/lib/hooks/useMediaQuery';
import ChatInterface from '@/components/chat/ChatInterface';
import ChatInterfaceMobile from '@/components/chat/ChatInterfaceMobile';
import ProjectSidebar from '@/components/project/ProjectSidebar';
import Header from '@/components/layout/Header';
import Loading from '@/components/ui/Loading';
import { fadeIn } from '@/lib/animations';
import { useConversationStore } from '@/lib/store';

export default function Home() {
  const isMobile = useIsMobile();
  const isConnected = useConversationStore((state) => state.isConnected);

  useEffect(() => {
    // Connect to WebSocket on mount
    const wsUrl = process.env.NEXT_PUBLIC_WS_URL || 'http://localhost:8087';
    websocketService.connect(wsUrl);

    // Cleanup on unmount
    return () => {
      websocketService.disconnect();
    };
  }, []);

  // Show loading screen while connecting
  if (!isConnected && !isMobile) {
    return (
      <div className="flex h-screen items-center justify-center">
        <Loading size="lg" text="Connecting to GigaPress..." />
      </div>
    );
  }

  return (
    <motion.div
      {...fadeIn}
      className="flex h-screen bg-background"
    >
      {/* Desktop Layout */}
      {!isMobile && (
        <>
          {/* Sidebar */}
          <ProjectSidebar />
          
          {/* Main Content */}
          <div className="flex-1 flex flex-col">
            <Header />
            <main className="flex-1 overflow-hidden">
              <ChatInterface />
            </main>
          </div>
        </>
      )}

      {/* Mobile Layout */}
      {isMobile && (
        <div className="flex flex-col h-full">
          <Header />
          <main className="flex-1 overflow-hidden">
            <ChatInterfaceMobile />
          </main>
        </div>
      )}
    </motion.div>
  );
}
EOF

# Update page.tsx
mv app/page-animated.tsx app/page.tsx

# Splash Screen 컴포넌트
cat > components/ui/SplashScreen.tsx << 'EOF'
'use client'

import { motion } from 'framer-motion';
import { Sparkles } from 'lucide-react';

export default function SplashScreen() {
  return (
    <motion.div
      initial={{ opacity: 1 }}
      animate={{ opacity: 0 }}
      transition={{ delay: 2, duration: 0.5 }}
      onAnimationComplete={() => {
        document.getElementById('splash')?.remove();
      }}
      id="splash"
      className="fixed inset-0 z-50 flex items-center justify-center bg-background"
    >
      <motion.div
        initial={{ scale: 0.5, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        transition={{ duration: 0.5 }}
        className="flex flex-col items-center gap-4"
      >
        <motion.div
          animate={{ rotate: 360 }}
          transition={{ duration: 2, repeat: Infinity, ease: 'linear' }}
          className="p-4 rounded-2xl bg-primary text-primary-foreground"
        >
          <Sparkles size={48} />
        </motion.div>
        <motion.h1
          initial={{ y: 20, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ delay: 0.2 }}
          className="text-3xl font-bold"
        >
          GigaPress
        </motion.h1>
        <motion.p
          initial={{ y: 20, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ delay: 0.4 }}
          className="text-muted-foreground"
        >
          AI-Powered Project Generation
        </motion.p>
      </motion.div>
    </motion.div>
  );
}
EOF

# PWA manifest
cat > public/manifest.json << 'EOF'
{
  "name": "GigaPress - AI Project Generator",
  "short_name": "GigaPress",
  "description": "Generate and modify software projects using natural language",
  "start_url": "/",
  "display": "standalone",
  "theme_color": "#000000",
  "background_color": "#ffffff",
  "icons": [
    {
      "src": "/icon-192.png",
      "sizes": "192x192",
      "type": "image/png"
    },
    {
      "src": "/icon-512.png",
      "sizes": "512x512",
      "type": "image/png"
    }
  ]
}
EOF

# 환경 변수 타입
cat > types/env.d.ts << 'EOF'
declare namespace NodeJS {
  interface ProcessEnv {
    NEXT_PUBLIC_API_URL: string;
    NEXT_PUBLIC_WS_URL: string;
  }
}
EOF

# Docker 최적화
cat > .dockerignore << 'EOF'
Dockerfile
.dockerignore
node_modules
npm-debug.log
README.md
.next
.git
.gitignore
.env*.local
coverage
.coverage
.nyc_output
.DS_Store
*.log
EOF

# Production 빌드 스크립트
cat > scripts/build.sh << 'EOF'
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
EOF

# 개발 실행 스크립트
cat > scripts/dev.sh << 'EOF'
#!/bin/bash

echo "Starting Conversational Layer in development mode..."

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
  echo "Installing dependencies..."
  npm install
fi

# Start development server
npm run dev
EOF

# 전체 설치 및 실행 스크립트
cat > start-conversational-layer.sh << 'EOF'
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

EOF

# Make scripts executable
chmod +x scripts/build.sh
chmod +x scripts/dev.sh
chmod +x start-conversational-layer.sh

# README 파일
cat > README.md << 'EOF'
# GigaPress Conversational Layer

The frontend interface for GigaPress - an AI-powered project generation system.

## Features

- 🎨 Modern UI with dark/light mode support
- 💬 Real-time chat interface with WebSocket
- 📊 Project status tracking and visualization
- 📱 Fully responsive design
- ⚡ Built with Next.js 14 and TypeScript
- 🎯 Tailwind CSS for styling

## Prerequisites

- Node.js 18+
- All backend services running (ports 8081-8087)
- Docker (for containerized deployment)

## Quick Start

```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Or use the start script
./start-conversational-layer.sh
```

## Environment Variables

Create a `.env.local` file:

```env
NEXT_PUBLIC_API_URL=http://localhost:8087
NEXT_PUBLIC_WS_URL=http://localhost:8087
```

## Available Scripts

- `npm run dev` - Start development server on port 8080
- `npm run build` - Build for production
- `npm run start` - Start production server
- `npm run lint` - Run ESLint
- `npm run type-check` - Run TypeScript type checking

## Project Structure

```
conversational-layer/
├── app/                # Next.js app directory
├── components/         # React components
│   ├── chat/          # Chat-related components
│   ├── project/       # Project management components
│   ├── layout/        # Layout components
│   └── ui/            # UI components
├── lib/               # Utilities and services
│   ├── hooks/         # Custom React hooks
│   ├── store.ts       # Zustand state management
│   └── websocket.ts   # WebSocket service
├── types/             # TypeScript type definitions
└── public/            # Static assets
```

## Docker Deployment

```bash
# Build Docker image
docker build -t gigapress-conversational-layer .

# Run container
docker run -p 8080:8080 gigapress-conversational-layer
```

## Architecture

The Conversational Layer connects to:
- **Conversational AI Engine** (port 8087) via WebSocket for real-time communication
- Displays project generation progress
- Manages chat history and project state
- Provides intuitive UI for natural language interaction

## Contributing

1. Follow the TypeScript and Next.js best practices
2. Use Tailwind CSS for styling
3. Ensure responsive design works on all devices
4. Add proper error handling
5. Write meaningful commit messages

## License

Copyright © 2025 GigaPress. All rights reserved.
EOF

echo ""
echo "✅ Step 5 완료!"
echo "   - 애니메이션 시스템 구현"
echo "   - 반응형 디자인 최적화"
echo "   - 모바일 UI 구현"
echo "   - PWA 지원"
echo "   - 빌드 및 실행 스크립트"
echo ""
echo "🎉 Conversational Layer 구현 완료!"
echo ""
echo "실행 방법:"
echo "1. cd services/conversational-layer"
echo "2. npm install"
echo "3. npm run dev"
echo ""
echo "또는 제공된 스크립트 사용:"
echo "./start-conversational-layer.sh"