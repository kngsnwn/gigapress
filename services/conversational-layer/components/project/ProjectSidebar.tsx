'use client'

import { useState } from 'react';
import { useConversationStore } from '@/lib/store';
import ProjectStatus from './ProjectStatus';
import { ChevronLeft, ChevronRight, Plus, FolderOpen } from 'lucide-react';
import { cn } from '@/lib/utils';

export default function ProjectSidebar() {
  const [isCollapsed, setIsCollapsed] = useState(false);
  const { projects, currentProject, setCurrentProject } = useConversationStore();

  return (
    <div
      className={cn(
        'relative flex flex-col border-r border-border bg-card transition-all duration-300',
        isCollapsed ? 'w-16' : 'w-80'
      )}
    >
      {/* Toggle Button */}
      <button
        onClick={() => setIsCollapsed(!isCollapsed)}
        className="absolute -right-3 top-6 z-10 p-1 rounded-full border border-border bg-background hover:bg-accent transition-colors"
      >
        {isCollapsed ? <ChevronRight size={16} /> : <ChevronLeft size={16} />}
      </button>

      {/* Header */}
      <div className="p-4 border-b border-border">
        {!isCollapsed ? (
          <div className="flex items-center justify-between">
            <h2 className="font-semibold">Projects</h2>
            <button className="p-1.5 rounded hover:bg-accent transition-colors">
              <Plus size={18} />
            </button>
          </div>
        ) : (
          <div className="flex justify-center">
            <FolderOpen size={20} />
          </div>
        )}
      </div>

      {/* Projects List */}
      {!isCollapsed && (
        <div className="flex-1 overflow-y-auto">
          {projects.length === 0 ? (
            <div className="p-4 text-center text-sm text-muted-foreground">
              No projects yet
            </div>
          ) : (
            <div className="p-2 space-y-1">
              {projects.map((project) => (
                <button
                  key={project.id}
                  onClick={() => setCurrentProject(project)}
                  className={cn(
                    'w-full p-3 text-left rounded-lg transition-colors',
                    currentProject?.id === project.id
                      ? 'bg-accent'
                      : 'hover:bg-accent/50'
                  )}
                >
                  <div className="font-medium text-sm">{project.name}</div>
                  <div className="text-xs text-muted-foreground mt-1">
                    {project.type} • {project.status}
                  </div>
                </button>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Project Status */}
      {!isCollapsed && currentProject && (
        <div className="border-t border-border">
          <ProjectStatus />
        </div>
      )}
    </div>
  );
}
