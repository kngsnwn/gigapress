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
