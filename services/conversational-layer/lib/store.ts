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
