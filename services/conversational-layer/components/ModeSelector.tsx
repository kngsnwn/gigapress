'use client'

import { useConversationStore } from '@/lib/store';
import { demoProjects, demoMessages, demoProgressUpdates } from '@/lib/demoData';
import { Laptop, Wifi, WifiOff } from 'lucide-react';
import { cn } from '@/lib/utils';

interface ModeSelectorProps {
  onClose: () => void;
}

export default function ModeSelector({ onClose }: ModeSelectorProps) {
  const { isDemoMode, setIsDemoMode, setCurrentProject, addMessage, addProgressUpdate, clearMessages, clearProgress, addProject } = useConversationStore();

  const handleModeChange = (demo: boolean) => {
    setIsDemoMode(demo);
    
    if (demo) {
      // Load demo data
      clearMessages();
      clearProgress();
      
      // Add demo projects
      demoProjects.forEach(project => addProject(project));
      
      // Add demo messages
      demoMessages.forEach(msg => addMessage(msg));
      
      // Add demo progress updates
      demoProgressUpdates.forEach(update => addProgressUpdate(update));
      
      // Set demo project
      setCurrentProject(demoProjects[0]);
    } else {
      // Clear demo data when switching to real mode
      clearMessages();
      clearProgress();
      setCurrentProject(null);
    }
    
    // Close the selector
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/80 backdrop-blur-sm">
      <div className="bg-card rounded-lg border p-8 max-w-md w-full mx-4 shadow-lg">
        <h2 className="text-2xl font-bold text-center mb-6">Choose Mode</h2>
        
        <div className="space-y-4">
          <button
            onClick={() => handleModeChange(true)}
            className={cn(
              "w-full p-6 rounded-lg border-2 transition-all",
              "hover:scale-[1.02] hover:shadow-md",
              isDemoMode ? "border-primary bg-primary/10" : "border-border"
            )}
          >
            <div className="flex items-center gap-4">
              <div className="p-3 rounded-full bg-primary/20">
                <Laptop className="w-6 h-6 text-primary" />
              </div>
              <div className="text-left">
                <h3 className="font-semibold">Demo Mode</h3>
                <p className="text-sm text-muted-foreground">
                  View with sample data (no connection required)
                </p>
              </div>
            </div>
          </button>

          <button
            onClick={() => handleModeChange(false)}
            className={cn(
              "w-full p-6 rounded-lg border-2 transition-all",
              "hover:scale-[1.02] hover:shadow-md",
              !isDemoMode ? "border-primary bg-primary/10" : "border-border"
            )}
          >
            <div className="flex items-center gap-4">
              <div className="p-3 rounded-full bg-green-500/20">
                <Wifi className="w-6 h-6 text-green-500" />
              </div>
              <div className="text-left">
                <h3 className="font-semibold">Real Mode</h3>
                <p className="text-sm text-muted-foreground">
                  Connect to actual services
                </p>
              </div>
            </div>
          </button>
        </div>

        <div className="mt-6 p-4 rounded-lg bg-muted/50">
          <p className="text-xs text-muted-foreground text-center">
            {isDemoMode ? (
              <>
                <WifiOff className="inline w-3 h-3 mr-1" />
                Demo mode active - No WebSocket connection
              </>
            ) : (
              <>
                <Wifi className="inline w-3 h-3 mr-1" />
                Real mode - WebSocket connection required
              </>
            )}
          </p>
        </div>
      </div>
    </div>
  );
}