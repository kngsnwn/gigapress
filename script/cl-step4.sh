#!/bin/bash

# Step 4: WebSocket 통신 구현 및 레이아웃 컴포넌트 (수정본)
echo "🚀 Step 4: Conversational Layer - WebSocket 통신 및 레이아웃"
echo "=================================================="

cd services/conversational-layer

# 필요한 디렉토리 생성
mkdir -p lib/hooks
mkdir -p components/ui

# Header 컴포넌트
cat > components/layout/Header.tsx << 'EOF'
'use client'

import { useTheme } from 'next-themes';
import { useConversationStore } from '@/lib/store';
import { 
  Sun, 
  Moon, 
  Settings, 
  HelpCircle, 
  Wifi, 
  WifiOff,
  Sparkles
} from 'lucide-react';
import { cn } from '@/lib/utils';

export default function Header() {
  const { theme, setTheme } = useTheme();
  const isConnected = useConversationStore((state) => state.isConnected);

  return (
    <header className="flex items-center justify-between px-6 py-4 border-b border-border bg-card">
      {/* Logo and Title */}
      <div className="flex items-center gap-3">
        <div className="p-2 rounded-lg bg-primary text-primary-foreground">
          <Sparkles size={24} />
        </div>
        <div>
          <h1 className="text-xl font-bold">GigaPress</h1>
          <p className="text-xs text-muted-foreground">AI-Powered Project Generation</p>
        </div>
      </div>

      {/* Actions */}
      <div className="flex items-center gap-2">
        {/* Connection Status */}
        <div
          className={cn(
            'flex items-center gap-2 px-3 py-1.5 rounded-full text-sm',
            isConnected
              ? 'bg-green-500/10 text-green-600 dark:text-green-400'
              : 'bg-destructive/10 text-destructive'
          )}
        >
          {isConnected ? <Wifi size={16} /> : <WifiOff size={16} />}
          <span className="font-medium">
            {isConnected ? 'Connected' : 'Disconnected'}
          </span>
        </div>

        {/* Theme Toggle */}
        <button
          onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}
          className="p-2 rounded-lg hover:bg-accent transition-colors"
          title="Toggle theme"
        >
          {theme === 'dark' ? <Sun size={20} /> : <Moon size={20} />}
        </button>

        {/* Help */}
        <button
          className="p-2 rounded-lg hover:bg-accent transition-colors"
          title="Help"
        >
          <HelpCircle size={20} />
        </button>

        {/* Settings */}
        <button
          className="p-2 rounded-lg hover:bg-accent transition-colors"
          title="Settings"
        >
          <Settings size={20} />
        </button>
      </div>
    </header>
  );
}
EOF

# WebSocket Hook
cat > lib/hooks/useWebSocket.ts << 'EOF'
'use client'

import { useEffect, useRef } from 'react';
import { websocketService } from '@/lib/websocket';
import { useConversationStore } from '@/lib/store';

export function useWebSocket(url?: string) {
  const wsUrl = url || process.env.NEXT_PUBLIC_WS_URL || 'http://localhost:8087';
  const reconnectTimeoutRef = useRef<NodeJS.Timeout>();
  const isConnected = useConversationStore((state) => state.isConnected);

  useEffect(() => {
    const connect = () => {
      websocketService.connect(wsUrl);
    };

    const handleReconnect = () => {
      if (!isConnected) {
        reconnectTimeoutRef.current = setTimeout(() => {
          console.log('Attempting to reconnect...');
          connect();
        }, 5000);
      }
    };

    // Initial connection
    connect();

    // Set up reconnection logic
    const interval = setInterval(() => {
      if (!isConnected) {
        handleReconnect();
      }
    }, 10000);

    return () => {
      clearInterval(interval);
      if (reconnectTimeoutRef.current) {
        clearTimeout(reconnectTimeoutRef.current);
      }
      websocketService.disconnect();
    };
  }, [wsUrl, isConnected]);

  return {
    isConnected,
    sendMessage: websocketService.sendMessage.bind(websocketService),
    sendProjectAction: websocketService.sendProjectAction.bind(websocketService),
  };
}
EOF

# 향상된 WebSocket 서비스 (오류 처리 개선)
cat > lib/websocket-enhanced.ts << 'EOF'
import { io, Socket } from 'socket.io-client';
import { WebSocketMessage, Message, Project, ProgressUpdate } from '@/types';
import { useConversationStore } from './store';
import toast from 'react-hot-toast';

interface WebSocketOptions {
  reconnectAttempts?: number;
  reconnectDelay?: number;
  timeout?: number;
}

class EnhancedWebSocketService {
  private socket: Socket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 1000;
  private messageQueue: WebSocketMessage[] = [];
  private isProcessingQueue = false;

  connect(url: string = 'http://localhost:8087', options?: WebSocketOptions) {
    if (this.socket?.connected) {
      console.log('Already connected');
      return;
    }

    const { reconnectAttempts = 5, reconnectDelay = 1000, timeout = 10000 } = options || {};
    
    this.maxReconnectAttempts = reconnectAttempts;
    this.reconnectDelay = reconnectDelay;

    this.socket = io(url, {
      transports: ['websocket', 'polling'],
      autoConnect: true,
      reconnection: true,
      reconnectionAttempts: this.maxReconnectAttempts,
      reconnectionDelay: this.reconnectDelay,
      timeout,
    });

    this.setupEventHandlers();
  }

  private setupEventHandlers() {
    if (!this.socket) return;

    const store = useConversationStore.getState();

    this.socket.on('connect', () => {
      console.log('WebSocket connected');
      store.setIsConnected(true);
      this.reconnectAttempts = 0;
      toast.success('Connected to server');
      
      // Process queued messages
      this.processMessageQueue();
    });

    this.socket.on('disconnect', (reason) => {
      console.log('WebSocket disconnected:', reason);
      store.setIsConnected(false);
      
      if (reason === 'io server disconnect') {
        // Server disconnected, don't auto-reconnect
        toast.error('Server disconnected');
      } else {
        // Client disconnected, try to reconnect
        toast.error('Connection lost, attempting to reconnect...');
      }
    });

    this.socket.on('connect_error', (error) => {
      console.error('WebSocket connection error:', error);
      this.reconnectAttempts++;
      
      if (this.reconnectAttempts >= this.maxReconnectAttempts) {
        toast.error('Failed to connect to server. Please check your connection.');
        store.setIsConnected(false);
      }
    });

    // Handle incoming messages
    this.socket.on('message', (data: WebSocketMessage) => {
      this.handleMessage(data);
    });

    // Handle typing indicator
    this.socket.on('typing', (isTyping: boolean) => {
      store.setIsTyping(isTyping);
    });

    // Handle errors
    this.socket.on('error', (error: any) => {
      console.error('WebSocket error:', error);
      toast.error(error.message || 'An error occurred');
    });
  }

  private handleMessage(data: WebSocketMessage) {
    const store = useConversationStore.getState();

    try {
      switch (data.type) {
        case 'message':
          const message: Message = {
            ...data.payload,
            timestamp: new Date(data.payload.timestamp),
          };
          store.addMessage(message);
          break;

        case 'progress':
          const progress: ProgressUpdate = {
            ...data.payload,
            timestamp: new Date(data.payload.timestamp),
          };
          store.addProgressUpdate(progress);
          break;

        case 'project_update':
          const project: Project = {
            ...data.payload,
            lastModified: new Date(data.payload.lastModified),
          };
          if (store.projects.find(p => p.id === project.id)) {
            store.updateProject(project.id, project);
          } else {
            store.addProject(project);
            store.setCurrentProject(project);
          }
          break;

        case 'error':
          const errorMessage = data.payload.message || 'An error occurred';
          toast.error(errorMessage);
          
          // Add error message to chat
          const errorMsg: Message = {
            id: `error-${Date.now()}`,
            role: 'system',
            content: `Error: ${errorMessage}`,
            timestamp: new Date(),
            status: 'error',
          };
          store.addMessage(errorMsg);
          break;

        default:
          console.warn('Unknown message type:', data.type);
      }
    } catch (error) {
      console.error('Error handling message:', error);
      toast.error('Failed to process message');
    }
  }

  private processMessageQueue() {
    if (this.isProcessingQueue || this.messageQueue.length === 0) return;
    
    this.isProcessingQueue = true;
    
    while (this.messageQueue.length > 0 && this.socket?.connected) {
      const message = this.messageQueue.shift();
      if (message) {
        this.socket.emit('message', message);
      }
    }
    
    this.isProcessingQueue = false;
  }

  sendMessage(content: string) {
    const message: WebSocketMessage = {
      type: 'message',
      payload: {
        content,
        timestamp: new Date().toISOString(),
      },
    };

    if (!this.socket?.connected) {
      toast.error('Not connected to server. Message queued.');
      this.messageQueue.push(message);
      return;
    }

    const userMessage: Message = {
      id: `msg-${Date.now()}`,
      role: 'user',
      content,
      timestamp: new Date(),
      status: 'sending',
    };

    const store = useConversationStore.getState();
    store.addMessage(userMessage);

    this.socket.emit('message', {
      type: 'user_message',
      payload: {
        content,
        projectId: store.currentProject?.id,
        messageId: userMessage.id,
      },
    });

    // Update message status
    setTimeout(() => {
      store.updateMessage(userMessage.id, { status: 'sent' });
    }, 100);
  }

  sendProjectAction(action: string, payload: any) {
    if (!this.socket?.connected) {
      toast.error('Not connected to server');
      return;
    }

    this.socket.emit('project_action', {
      action,
      payload,
      timestamp: new Date().toISOString(),
    });
  }

  getConnectionState() {
    return {
      connected: this.socket?.connected || false,
      reconnectAttempts: this.reconnectAttempts,
      maxReconnectAttempts: this.maxReconnectAttempts,
    };
  }

  disconnect() {
    if (this.socket) {
      this.socket.disconnect();
      this.socket = null;
      this.messageQueue = [];
    }
  }
}

export const enhancedWebSocketService = new EnhancedWebSocketService();
EOF

# Loading 컴포넌트
cat > components/ui/Loading.tsx << 'EOF'
'use client'

import { Loader2 } from 'lucide-react';
import { cn } from '@/lib/utils';

interface LoadingProps {
  size?: 'sm' | 'md' | 'lg';
  text?: string;
  className?: string;
}

export default function Loading({ size = 'md', text, className }: LoadingProps) {
  const sizeMap = {
    sm: 'w-4 h-4',
    md: 'w-8 h-8',
    lg: 'w-12 h-12',
  };

  return (
    <div className={cn('flex flex-col items-center justify-center gap-3', className)}>
      <Loader2 className={cn('animate-spin text-primary', sizeMap[size])} />
      {text && <p className="text-sm text-muted-foreground">{text}</p>}
    </div>
  );
}
EOF

# ErrorBoundary 컴포넌트
cat > components/ui/ErrorBoundary.tsx << 'EOF'
'use client'

import React from 'react';
import { AlertCircle } from 'lucide-react';

interface ErrorBoundaryState {
  hasError: boolean;
  error: Error | null;
}

export default class ErrorBoundary extends React.Component<
  { children: React.ReactNode },
  ErrorBoundaryState
> {
  constructor(props: { children: React.ReactNode }) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error('Error caught by boundary:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="flex flex-col items-center justify-center h-screen p-8">
          <div className="flex flex-col items-center gap-4 max-w-md text-center">
            <div className="p-3 rounded-full bg-destructive/10">
              <AlertCircle className="w-8 h-8 text-destructive" />
            </div>
            <h1 className="text-2xl font-semibold">Something went wrong</h1>
            <p className="text-muted-foreground">
              An unexpected error occurred. Please refresh the page to try again.
            </p>
            {this.state.error && (
              <details className="mt-4 p-4 rounded-lg bg-muted text-left w-full">
                <summary className="cursor-pointer font-medium">Error details</summary>
                <pre className="mt-2 text-xs overflow-auto">
                  {this.state.error.toString()}
                </pre>
              </details>
            )}
            <button
              onClick={() => window.location.reload()}
              className="mt-4 px-4 py-2 rounded-lg bg-primary text-primary-foreground hover:bg-primary/90 transition-colors"
            >
              Refresh Page
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
EOF

# Update layout with ErrorBoundary
cat > app/layout-updated.tsx << 'EOF'
import type { Metadata } from 'next'
import { Inter } from 'next/font/google'
import './globals.css'
import { ThemeProvider } from '@/components/layout/ThemeProvider'
import ErrorBoundary from '@/components/ui/ErrorBoundary'
import { Toaster } from 'react-hot-toast'

const inter = Inter({ subsets: ['latin'] })

export const metadata: Metadata = {
  title: 'GigaPress - AI-Powered Project Generation',
  description: 'Generate and modify software projects using natural language',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body className={inter.className}>
        <ErrorBoundary>
          <ThemeProvider attribute="class" defaultTheme="system" enableSystem>
            {children}
            <Toaster
              position="top-center"
              toastOptions={{
                duration: 4000,
                style: {
                  background: 'var(--background)',
                  color: 'var(--foreground)',
                  border: '1px solid var(--border)',
                },
              }}
            />
          </ThemeProvider>
        </ErrorBoundary>
      </body>
    </html>
  )
}
EOF

# Update the layout file
mv app/layout-updated.tsx app/layout.tsx

echo ""
echo "✅ Step 4 완료!"
echo "   - Header 컴포넌트"
echo "   - WebSocket Hook 구현"
echo "   - Enhanced WebSocket Service"
echo "   - Loading & ErrorBoundary 컴포넌트"
echo ""
echo "Step 5로 진행합니다..."