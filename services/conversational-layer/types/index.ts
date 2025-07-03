export interface Message {
  id: string;
  role: 'user' | 'assistant' | 'system';
  content: string;
  timestamp: Date;
  status?: 'sending' | 'sent' | 'error';
}

export interface Project {
  id: string;
  name: string;
  type: string;
  status: 'idle' | 'generating' | 'updating' | 'completed' | 'error';
  version: string;
  lastModified: Date;
  description?: string;
  architecture?: {
    frontend?: any;
    backend?: any;
    infrastructure?: any;
  };
}

export interface ProgressUpdate {
  step: string;
  progress: number;
  message: string;
  timestamp: Date;
}

export interface WebSocketMessage {
  type: 'message' | 'progress' | 'project_update' | 'error';
  payload: any;
}
