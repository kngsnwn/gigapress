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
