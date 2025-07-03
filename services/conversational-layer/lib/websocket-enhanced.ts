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
