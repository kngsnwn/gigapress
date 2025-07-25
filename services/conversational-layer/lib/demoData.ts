import { Project, Message, ProgressUpdate } from '@/types';

export const demoProjects: Project[] = [
  {
    id: 'demo-1',
    name: 'E-Commerce Platform',
    type: 'Full Stack',
    status: 'completed',
    version: '1.0.0',
    lastModified: new Date(),
    description: 'A modern e-commerce platform with microservices architecture',
    architecture: {
      frontend: {
        framework: 'Next.js 14',
        libraries: ['React', 'Tailwind CSS', 'Framer Motion']
      },
      backend: {
        framework: 'Node.js + Express',
        language: 'TypeScript',
        services: ['Auth Service', 'Product Service', 'Order Service']
      },
      database: {
        type: 'PostgreSQL + Redis',
        orm: 'Prisma'
      },
      vcs: {
        type: 'Git',
        platform: 'GitHub'
      }
    }
  },
  {
    id: 'demo-2',
    name: 'Real-time Analytics Dashboard',
    type: 'Frontend',
    status: 'generating',
    version: '0.8.0',
    lastModified: new Date(),
    description: 'Interactive dashboard for real-time data visualization',
    architecture: {
      frontend: {
        framework: 'React + Vite',
        libraries: ['D3.js', 'Chart.js', 'Material-UI']
      }
    }
  }
];

export const demoMessages: Message[] = [
  {
    id: '1',
    role: 'user',
    content: 'Create an e-commerce platform with microservices',
    timestamp: new Date(Date.now() - 10000),
    status: 'sent'
  },
  {
    id: '2',
    role: 'assistant',
    content: "I'll help you create a modern e-commerce platform with microservices architecture. Let me set up the project structure for you.",
    timestamp: new Date(Date.now() - 8000),
    status: 'sent'
  },
  {
    id: '3',
    role: 'user',
    content: 'Add real-time analytics dashboard',
    timestamp: new Date(Date.now() - 5000),
    status: 'sent'
  },
  {
    id: '4',
    role: 'assistant',
    content: "I'm now setting up a real-time analytics dashboard with interactive data visualizations.",
    timestamp: new Date(Date.now() - 3000),
    status: 'sent'
  }
];

export const demoProgressUpdates: ProgressUpdate[] = [
  {
    step: 'Initializing project structure',
    progress: 100,
    message: 'Project structure created successfully',
    timestamp: new Date(Date.now() - 9000)
  },
  {
    step: 'Setting up frontend',
    progress: 100,
    message: 'Next.js application configured',
    timestamp: new Date(Date.now() - 8000)
  },
  {
    step: 'Creating backend services',
    progress: 100,
    message: 'Microservices architecture implemented',
    timestamp: new Date(Date.now() - 7000)
  },
  {
    step: 'Configuring database',
    progress: 100,
    message: 'PostgreSQL and Redis setup complete',
    timestamp: new Date(Date.now() - 6000)
  },
  {
    step: 'Building analytics dashboard',
    progress: 75,
    message: 'Implementing real-time data visualizations',
    timestamp: new Date(Date.now() - 2000)
  }
];