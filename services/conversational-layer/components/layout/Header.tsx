'use client'

import { useTheme } from 'next-themes';
import { useConversationStore } from '@/lib/store';
import { demoProjects, demoMessages, demoProgressUpdates } from '@/lib/demoData';
import { 
  Sun, 
  Moon, 
  Settings, 
  HelpCircle, 
  Wifi, 
  WifiOff,
  Sparkles,
  Laptop,
  ToggleLeft,
  ToggleRight
} from 'lucide-react';
import { cn } from '@/lib/utils';

export default function Header() {
  const { theme, setTheme } = useTheme();
  const { 
    isConnected, 
    isDemoMode, 
    setIsDemoMode,
    setCurrentProject,
    addMessage,
    addProgressUpdate,
    clearMessages,
    clearProgress,
    addProject
  } = useConversationStore();

  const toggleMode = () => {
    const newMode = !isDemoMode;
    setIsDemoMode(newMode);
    
    if (newMode) {
      // Switch to demo mode
      clearMessages();
      clearProgress();
      
      // Add demo data
      demoProjects.forEach(project => addProject(project));
      demoMessages.forEach(msg => addMessage(msg));
      demoProgressUpdates.forEach(update => addProgressUpdate(update));
      setCurrentProject(demoProjects[0]);
    } else {
      // Switch to real mode
      clearMessages();
      clearProgress();
      setCurrentProject(null);
      
      // Reconnect WebSocket
      if (typeof window !== 'undefined') {
        window.location.reload();
      }
    }
  };

  return (
    <header className="flex items-center justify-between px-6 py-4 border-b border-border bg-card">
      {/* Logo and Title */}
      <div className="flex items-center gap-3">
        <div className="p-2 rounded-lg bg-primary text-primary-foreground">
          <Sparkles size={24} />
        </div>
        <div>
          <h1 className="text-xl font-bold">GigaPress</h1>
          <p className="text-xs text-muted-foreground">AI-Powered Project Generation</p>
        </div>
      </div>

      {/* Actions */}
      <div className="flex items-center gap-2">
        {/* Mode Toggle */}
        <button
          onClick={toggleMode}
          className="flex items-center gap-2 px-3 py-1.5 rounded-full text-sm bg-muted hover:bg-accent transition-colors"
          title={isDemoMode ? 'Switch to Real Mode' : 'Switch to Demo Mode'}
        >
          {isDemoMode ? (
            <>
              <Laptop size={16} />
              <span className="font-medium">Demo Mode</span>
              <ToggleLeft size={20} className="text-muted-foreground" />
            </>
          ) : (
            <>
              <Wifi size={16} className="text-green-500" />
              <span className="font-medium">Real Mode</span>
              <ToggleRight size={20} className="text-primary" />
            </>
          )}
        </button>

        {/* Connection Status (only show in real mode) */}
        {!isDemoMode && (
          <div
            className={cn(
              'flex items-center gap-2 px-3 py-1.5 rounded-full text-sm',
              isConnected
                ? 'bg-green-500/10 text-green-600 dark:text-green-400'
                : 'bg-destructive/10 text-destructive'
            )}
          >
            {isConnected ? <Wifi size={16} /> : <WifiOff size={16} />}
            <span className="font-medium">
              {isConnected ? 'Connected' : 'Disconnected'}
            </span>
          </div>
        )}

        {/* Theme Toggle */}
        <button
          onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}
          className="p-2 rounded-lg hover:bg-accent transition-colors"
          title="Toggle theme"
        >
          {theme === 'dark' ? <Sun size={20} /> : <Moon size={20} />}
        </button>

        {/* Help */}
        <button
          className="p-2 rounded-lg hover:bg-accent transition-colors"
          title="Help"
        >
          <HelpCircle size={20} />
        </button>

        {/* Settings */}
        <button
          className="p-2 rounded-lg hover:bg-accent transition-colors"
          title="Settings"
        >
          <Settings size={20} />
        </button>
      </div>
    </header>
  );
}
