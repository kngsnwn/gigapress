'use client'

import { useParams, useRouter } from 'next/navigation';
import { useConversationStore } from '@/lib/store';
import DataFlowVisualization from '@/components/project/DataFlowVisualization';
import { 
  ArrowLeft,
  Code2, 
  Database, 
  Layout, 
  Server, 
  GitBranch,
  CheckCircle,
  AlertCircle,
  Clock,
  Loader2,
  Calendar,
  Package,
  Settings,
  FileCode,
  Folder,
  Download,
  Eye,
  Terminal
} from 'lucide-react';
import { cn } from '@/lib/utils';

export default function ProjectDetailPage() {
  const params = useParams();
  const router = useRouter();
  const projects = useConversationStore((state) => state.projects);
  const progressUpdates = useConversationStore((state) => state.progressUpdates);
  
  const project = projects.find(p => p.id === params.id);

  if (!project) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center space-y-4">
          <h1 className="text-2xl font-semibold">Project not found</h1>
          <button 
            onClick={() => router.push('/')}
            className="text-primary hover:underline"
          >
            Back to home
          </button>
        </div>
      </div>
    );
  }

  const statusConfig = {
    idle: { icon: Clock, color: 'text-muted-foreground', bg: 'bg-muted' },
    generating: { icon: Loader2, color: 'text-primary', bg: 'bg-primary/10', animate: true },
    updating: { icon: Loader2, color: 'text-primary', bg: 'bg-primary/10', animate: true },
    completed: { icon: CheckCircle, color: 'text-green-500', bg: 'bg-green-500/10' },
    error: { icon: AlertCircle, color: 'text-destructive', bg: 'bg-destructive/10' },
  };

  const status = statusConfig[project.status];
  const StatusIcon = status.icon;

  const components = [
    { icon: Layout, label: 'Frontend', key: 'frontend' },
    { icon: Server, label: 'Backend', key: 'backend' },
    { icon: Database, label: 'Database', key: 'database' },
    { icon: GitBranch, label: 'Version Control', key: 'vcs' },
  ];

  return (
    <div className="min-h-screen bg-background">
      <div className="max-w-7xl mx-auto px-4 py-8">
        <button
          onClick={() => router.push('/')}
          className="flex items-center gap-2 text-muted-foreground hover:text-foreground mb-6 transition-colors"
        >
          <ArrowLeft size={20} />
          <span>Back to chat</span>
        </button>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <div className="lg:col-span-2 space-y-6">
            <div className="bg-card rounded-lg border p-6">
              <div className="flex items-start justify-between mb-4">
                <div className="space-y-1">
                  <h1 className="text-2xl font-bold">{project.name}</h1>
                  <p className="text-muted-foreground">{project.description}</p>
                </div>
                <div className={cn(
                  'flex items-center gap-2 px-3 py-1.5 rounded-full text-sm',
                  status.bg
                )}>
                  <StatusIcon 
                    size={16} 
                    className={cn(status.color, status.animate && 'animate-spin')}
                  />
                  <span className={status.color}>{project.status}</span>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4 text-sm">
                <div className="flex items-center gap-2 text-muted-foreground">
                  <Package size={16} />
                  <span>Type: {project.type}</span>
                </div>
                <div className="flex items-center gap-2 text-muted-foreground">
                  <Settings size={16} />
                  <span>Version: {project.version}</span>
                </div>
                <div className="flex items-center gap-2 text-muted-foreground">
                  <Calendar size={16} />
                  <span>Last Modified: {new Date(project.lastModified).toLocaleDateString()}</span>
                </div>
              </div>
            </div>

            <div className="bg-card rounded-lg border p-6">
              <h2 className="text-lg font-semibold mb-4">Architecture Components</h2>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {components.map(({ icon: Icon, label, key }) => {
                  const hasComponent = project.architecture?.[key as keyof typeof project.architecture];
                  return (
                    <div
                      key={key}
                      className={cn(
                        'p-4 rounded-lg border-2 transition-all',
                        hasComponent 
                          ? 'border-primary bg-primary/5' 
                          : 'border-dashed border-muted-foreground/30 bg-muted/30'
                      )}
                    >
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-3">
                          <Icon size={20} className={hasComponent ? 'text-primary' : 'text-muted-foreground'} />
                          <span className="font-medium">{label}</span>
                        </div>
                        {hasComponent && (
                          <CheckCircle size={16} className="text-green-500" />
                        )}
                      </div>
                      {hasComponent && (
                        <div className="mt-2 text-sm text-muted-foreground">
                          {typeof hasComponent === 'object' && hasComponent.framework && (
                            <span>Framework: {hasComponent.framework}</span>
                          )}
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>

            <div className="bg-card rounded-lg border p-6">
              <h2 className="text-lg font-semibold mb-4">Service Architecture Flow</h2>
              <DataFlowVisualization />
            </div>

            {project.architecture && (
              <div className="bg-card rounded-lg border p-6">
                <h2 className="text-lg font-semibold mb-4">Technical Details</h2>
                <div className="space-y-4">
                  {project.architecture.frontend && (
                    <div className="space-y-2">
                      <h3 className="font-medium flex items-center gap-2">
                        <Layout size={16} />
                        Frontend
                      </h3>
                      <div className="ml-6 space-y-1 text-sm text-muted-foreground">
                        <p>Framework: {project.architecture.frontend.framework || 'Not specified'}</p>
                        {project.architecture.frontend.libraries && (
                          <p>Libraries: {project.architecture.frontend.libraries.join(', ')}</p>
                        )}
                      </div>
                    </div>
                  )}
                  
                  {project.architecture.backend && (
                    <div className="space-y-2">
                      <h3 className="font-medium flex items-center gap-2">
                        <Server size={16} />
                        Backend
                      </h3>
                      <div className="ml-6 space-y-1 text-sm text-muted-foreground">
                        <p>Framework: {project.architecture.backend.framework || 'Not specified'}</p>
                        {project.architecture.backend.language && (
                          <p>Language: {project.architecture.backend.language}</p>
                        )}
                      </div>
                    </div>
                  )}
                  
                  {project.architecture.database && (
                    <div className="space-y-2">
                      <h3 className="font-medium flex items-center gap-2">
                        <Database size={16} />
                        Database
                      </h3>
                      <div className="ml-6 space-y-1 text-sm text-muted-foreground">
                        <p>Type: {project.architecture.database.type || 'Not specified'}</p>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            )}

            <div className="bg-card rounded-lg border p-6">
              <h2 className="text-lg font-semibold mb-4">File Structure</h2>
              <div className="space-y-2">
                <div className="flex items-center gap-2 p-2 hover:bg-accent rounded cursor-pointer">
                  <Folder size={16} className="text-primary" />
                  <span className="text-sm">src/</span>
                </div>
                <div className="ml-6 space-y-2">
                  <div className="flex items-center gap-2 p-2 hover:bg-accent rounded cursor-pointer">
                    <Folder size={16} className="text-primary" />
                    <span className="text-sm">components/</span>
                  </div>
                  <div className="flex items-center gap-2 p-2 hover:bg-accent rounded cursor-pointer">
                    <Folder size={16} className="text-primary" />
                    <span className="text-sm">pages/</span>
                  </div>
                  <div className="flex items-center gap-2 p-2 hover:bg-accent rounded cursor-pointer">
                    <FileCode size={16} className="text-blue-500" />
                    <span className="text-sm">index.js</span>
                  </div>
                </div>
                <div className="flex items-center gap-2 p-2 hover:bg-accent rounded cursor-pointer">
                  <FileCode size={16} className="text-green-500" />
                  <span className="text-sm">package.json</span>
                </div>
                <div className="flex items-center gap-2 p-2 hover:bg-accent rounded cursor-pointer">
                  <FileCode size={16} className="text-yellow-500" />
                  <span className="text-sm">README.md</span>
                </div>
              </div>
            </div>
          </div>

          <div className="space-y-6">
            <div className="bg-card rounded-lg border p-6">
              <h2 className="text-lg font-semibold mb-4">Actions</h2>
              <div className="space-y-3">
                <button className="w-full flex items-center justify-center gap-2 p-3 rounded-lg bg-primary text-primary-foreground hover:bg-primary/90 transition-colors">
                  <Eye size={18} />
                  View Code
                </button>
                <button className="w-full flex items-center justify-center gap-2 p-3 rounded-lg border border-border hover:bg-accent transition-colors">
                  <Terminal size={18} />
                  Open in Terminal
                </button>
                <button className="w-full flex items-center justify-center gap-2 p-3 rounded-lg border border-border hover:bg-accent transition-colors">
                  <Download size={18} />
                  Download Project
                </button>
              </div>
            </div>

            <div className="bg-card rounded-lg border p-6">
              <h2 className="text-lg font-semibold mb-4">Progress History</h2>
              <div className="space-y-3 max-h-96 overflow-y-auto">
                {progressUpdates.length > 0 ? (
                  progressUpdates.map((update, index) => (
                    <div key={index} className="space-y-1">
                      <div className="flex items-center justify-between text-sm">
                        <span className="font-medium">{update.step}</span>
                        <span className="text-muted-foreground">
                          {update.progress}%
                        </span>
                      </div>
                      <div className="w-full bg-secondary rounded-full h-2">
                        <div 
                          className="bg-primary rounded-full h-2 transition-all"
                          style={{ width: `${update.progress}%` }}
                        />
                      </div>
                      <p className="text-xs text-muted-foreground">{update.message}</p>
                    </div>
                  ))
                ) : (
                  <p className="text-sm text-muted-foreground text-center">
                    No progress updates yet
                  </p>
                )}
              </div>
            </div>

            <div className="bg-card rounded-lg border p-6">
              <h2 className="text-lg font-semibold mb-4">Project Stats</h2>
              <div className="space-y-3 text-sm">
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Files</span>
                  <span className="font-medium">24</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Lines of Code</span>
                  <span className="font-medium">1,234</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Dependencies</span>
                  <span className="font-medium">12</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Size</span>
                  <span className="font-medium">2.4 MB</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}