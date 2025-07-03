#!/bin/bash

# Step 2: 기본 구조 구성
echo "🚀 Step 2: Conversational Layer - 기본 구조 구성"
echo "=================================================="

cd services/conversational-layer

# Zustand store 생성
cat > lib/store.ts << 'EOF'
import { create } from 'zustand';
import { Message, Project, ProgressUpdate } from '@/types';

interface ConversationStore {
  // Messages
  messages: Message[];
  addMessage: (message: Message) => void;
  updateMessage: (id: string, updates: Partial<Message>) => void;
  clearMessages: () => void;
  
  // Projects
  currentProject: Project | null;
  projects: Project[];
  setCurrentProject: (project: Project | null) => void;
  updateProject: (id: string, updates: Partial<Project>) => void;
  addProject: (project: Project) => void;
  
  // Progress
  progressUpdates: ProgressUpdate[];
  addProgressUpdate: (update: ProgressUpdate) => void;
  clearProgress: () => void;
  
  // UI State
  isConnected: boolean;
  isTyping: boolean;
  setIsConnected: (connected: boolean) => void;
  setIsTyping: (typing: boolean) => void;
}

export const useConversationStore = create<ConversationStore>((set) => ({
  // Messages
  messages: [],
  addMessage: (message) =>
    set((state) => ({ messages: [...state.messages, message] })),
  updateMessage: (id, updates) =>
    set((state) => ({
      messages: state.messages.map((msg) =>
        msg.id === id ? { ...msg, ...updates } : msg
      ),
    })),
  clearMessages: () => set({ messages: [] }),
  
  // Projects
  currentProject: null,
  projects: [],
  setCurrentProject: (project) => set({ currentProject: project }),
  updateProject: (id, updates) =>
    set((state) => ({
      projects: state.projects.map((proj) =>
        proj.id === id ? { ...proj, ...updates } : proj
      ),
      currentProject:
        state.currentProject?.id === id
          ? { ...state.currentProject, ...updates }
          : state.currentProject,
    })),
  addProject: (project) =>
    set((state) => ({ projects: [...state.projects, project] })),
  
  // Progress
  progressUpdates: [],
  addProgressUpdate: (update) =>
    set((state) => ({
      progressUpdates: [...state.progressUpdates, update],
    })),
  clearProgress: () => set({ progressUpdates: [] }),
  
  // UI State
  isConnected: false,
  isTyping: false,
  setIsConnected: (connected) => set({ isConnected: connected }),
  setIsTyping: (typing) => set({ isTyping: typing }),
}));
EOF

# WebSocket 서비스 생성
cat > lib/websocket.ts << 'EOF'
import { io, Socket } from 'socket.io-client';
import { WebSocketMessage, Message, Project, ProgressUpdate } from '@/types';
import { useConversationStore } from './store';
import toast from 'react-hot-toast';

class WebSocketService {
  private socket: Socket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 1000;

  connect(url: string = 'http://localhost:8087') {
    if (this.socket?.connected) {
      return;
    }

    this.socket = io(url, {
      transports: ['websocket'],
      autoConnect: true,
      reconnection: true,
      reconnectionAttempts: this.maxReconnectAttempts,
      reconnectionDelay: this.reconnectDelay,
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
    });

    this.socket.on('disconnect', () => {
      console.log('WebSocket disconnected');
      store.setIsConnected(false);
      toast.error('Disconnected from server');
    });

    this.socket.on('connect_error', (error) => {
      console.error('WebSocket connection error:', error);
      this.reconnectAttempts++;
      
      if (this.reconnectAttempts >= this.maxReconnectAttempts) {
        toast.error('Failed to connect to server. Please check your connection.');
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
  }

  private handleMessage(data: WebSocketMessage) {
    const store = useConversationStore.getState();

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
        }
        break;

      case 'error':
        toast.error(data.payload.message || 'An error occurred');
        break;
    }
  }

  sendMessage(content: string) {
    if (!this.socket?.connected) {
      toast.error('Not connected to server');
      return;
    }

    const message: Message = {
      id: `msg-${Date.now()}`,
      role: 'user',
      content,
      timestamp: new Date(),
      status: 'sending',
    };

    const store = useConversationStore.getState();
    store.addMessage(message);

    this.socket.emit('message', {
      type: 'user_message',
      payload: {
        content,
        projectId: store.currentProject?.id,
      },
    });

    // Update message status
    setTimeout(() => {
      store.updateMessage(message.id, { status: 'sent' });
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
    });
  }

  disconnect() {
    if (this.socket) {
      this.socket.disconnect();
      this.socket = null;
    }
  }
}

export const websocketService = new WebSocketService();
EOF

# 유틸리티 함수 생성
cat > lib/utils.ts << 'EOF'
import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function formatDate(date: Date): string {
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const seconds = Math.floor(diff / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);

  if (seconds < 60) return 'just now';
  if (minutes < 60) return `${minutes}m ago`;
  if (hours < 24) return `${hours}h ago`;
  if (days < 7) return `${days}d ago`;
  
  return date.toLocaleDateString();
}

export function generateId(): string {
  return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
}
EOF

# API 서비스 생성
cat > lib/api.ts << 'EOF'
import axios from 'axios';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8087';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor
api.interceptors.request.use(
  (config) => {
    // Add auth token if available
    const token = localStorage.getItem('auth_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Handle unauthorized
      localStorage.removeItem('auth_token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const conversationAPI = {
  // Get conversation history
  getHistory: async () => {
    const response = await api.get('/api/conversations');
    return response.data;
  },

  // Get project details
  getProject: async (projectId: string) => {
    const response = await api.get(`/api/projects/${projectId}`);
    return response.data;
  },

  // Get all projects
  getProjects: async () => {
    const response = await api.get('/api/projects');
    return response.data;
  },

  // Create new project
  createProject: async (data: any) => {
    const response = await api.post('/api/projects', data);
    return response.data;
  },

  // Update project
  updateProject: async (projectId: string, data: any) => {
    const response = await api.put(`/api/projects/${projectId}`, data);
    return response.data;
  },
};

export default api;
EOF

# 환경 변수 파일 생성
cat > .env.local << 'EOF'
NEXT_PUBLIC_API_URL=http://localhost:8087
NEXT_PUBLIC_WS_URL=http://localhost:8087
EOF

# 메인 페이지 생성
cat > app/page.tsx << 'EOF'
'use client'

import { useEffect } from 'react';
import { websocketService } from '@/lib/websocket';
import ChatInterface from '@/components/chat/ChatInterface';
import ProjectSidebar from '@/components/project/ProjectSidebar';
import Header from '@/components/layout/Header';

export default function Home() {
  useEffect(() => {
    // Connect to WebSocket on mount
    const wsUrl = process.env.NEXT_PUBLIC_WS_URL || 'http://localhost:8087';
    websocketService.connect(wsUrl);

    // Cleanup on unmount
    return () => {
      websocketService.disconnect();
    };
  }, []);

  return (
    <div className="flex h-screen bg-background">
      {/* Sidebar */}
      <ProjectSidebar />
      
      {/* Main Content */}
      <div className="flex-1 flex flex-col">
        <Header />
        <main className="flex-1 overflow-hidden">
          <ChatInterface />
        </main>
      </div>
    </div>
  );
}
EOF

echo ""
echo "✅ Step 2 완료!"
echo "   - Zustand store 구성"
echo "   - WebSocket 서비스 구현"
echo "   - API 서비스 설정"
echo "   - 유틸리티 함수 생성"
echo ""
echo "Step 3로 진행합니다..."