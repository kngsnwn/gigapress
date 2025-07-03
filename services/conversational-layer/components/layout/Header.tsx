'use client'

import { useTheme } from 'next-themes';
import { useConversationStore } from '@/lib/store';
import { 
  Sun, 
  Moon, 
  Settings, 
  HelpCircle, 
  Wifi, 
  WifiOff,
  Sparkles
} from 'lucide-react';
import { cn } from '@/lib/utils';

export default function Header() {
  const { theme, setTheme } = useTheme();
  const isConnected = useConversationStore((state) => state.isConnected);

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
        {/* Connection Status */}
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
