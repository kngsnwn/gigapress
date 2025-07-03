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
