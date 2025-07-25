'use client'

import { useConversationStore } from '@/lib/store';
import { useRouter } from 'next/navigation';
import { 
  Code2, 
  Database, 
  Layout, 
  Server, 
  GitBranch,
  CheckCircle,
  AlertCircle,
  Clock,
  Loader2,
  ExternalLink
} from 'lucide-react';
import { cn } from '@/lib/utils';

export default function ProjectStatus() {
  const currentProject = useConversationStore((state) => state.currentProject);
  const router = useRouter();

  if (!currentProject) {
    return (
      <div className="p-6 text-center text-muted-foreground">
        <p>No project selected</p>
      </div>
    );
  }

  const statusIcon = {
    idle: <Clock size={16} className="text-muted-foreground" />,
    generating: <Loader2 size={16} className="animate-spin text-primary" />,
    updating: <Loader2 size={16} className="animate-spin text-primary" />,
    completed: <CheckCircle size={16} className="text-green-500" />,
    error: <AlertCircle size={16} className="text-destructive" />,
  };

  const components = [
    { icon: Layout, label: 'Frontend', key: 'frontend' },
    { icon: Server, label: 'Backend', key: 'backend' },
    { icon: Database, label: 'Database', key: 'database' },
    { icon: GitBranch, label: 'Version Control', key: 'vcs' },
  ];

  return (
    <div className="p-6 space-y-6">
      {/* Project Header */}
      <div className="space-y-2">
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-semibold">{currentProject.name}</h3>
          <div className="flex items-center gap-2">
            {statusIcon[currentProject.status]}
            <span className="text-sm capitalize">{currentProject.status}</span>
          </div>
        </div>
        <p className="text-sm text-muted-foreground">{currentProject.description}</p>
        <div className="flex items-center gap-4 text-xs text-muted-foreground">
          <span>Type: {currentProject.type}</span>
          <span>Version: {currentProject.version}</span>
        </div>
        <button 
          onClick={() => router.push(`/project/${currentProject.id}`)}
          className="flex items-center gap-1 text-xs text-primary hover:underline"
        >
          View more details
          <ExternalLink size={12} />
        </button>
      </div>

      {/* Components Status */}
      <div className="space-y-3">
        <h4 className="text-sm font-medium">Components</h4>
        <div className="space-y-2">
          {components.map(({ icon: Icon, label, key }) => {
            const hasComponent = currentProject.architecture?.[key as keyof typeof currentProject.architecture];
            return (
              <div
                key={key}
                className={cn(
                  'flex items-center gap-3 p-3 rounded-lg border',
                  hasComponent ? 'border-border' : 'border-dashed border-muted-foreground/30'
                )}
              >
                <Icon size={18} className={hasComponent ? 'text-primary' : 'text-muted-foreground'} />
                <span className="flex-1 text-sm">{label}</span>
                {hasComponent && (
                  <CheckCircle size={14} className="text-green-500" />
                )}
              </div>
            );
          })}
        </div>
      </div>


      {/* Quick Actions */}
      <div className="space-y-2">
        <button 
          onClick={() => router.push(`/project/${currentProject.id}`)}
          className="w-full p-2 text-sm rounded-lg bg-primary text-primary-foreground hover:bg-primary/90 transition-colors"
        >
          Open Project Details
        </button>
      </div>
    </div>
  );
}
