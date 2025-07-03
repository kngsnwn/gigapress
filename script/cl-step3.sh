#!/bin/bash

# Step 3: 핵심 컴포넌트 개발
echo "🚀 Step 3: Conversational Layer - 핵심 컴포넌트 개발"
echo "=================================================="

cd services/conversational-layer

# ChatInterface 컴포넌트
cat > components/chat/ChatInterface.tsx << 'EOF'
'use client'

import { useConversationStore } from '@/lib/store';
import MessageList from './MessageList';
import InputBox from './InputBox';
import ProgressTracker from '../project/ProgressTracker';
import { cn } from '@/lib/utils';

export default function ChatInterface() {
  const { isTyping, progressUpdates } = useConversationStore();

  return (
    <div className="flex flex-col h-full">
      {/* Progress Tracker */}
      {progressUpdates.length > 0 && (
        <div className="border-b border-border">
          <ProgressTracker />
        </div>
      )}

      {/* Messages Area */}
      <div className="flex-1 overflow-hidden">
        <MessageList />
      </div>

      {/* Typing Indicator */}
      {isTyping && (
        <div className="px-4 py-2 text-sm text-muted-foreground">
          AI is typing...
        </div>
      )}

      {/* Input Area */}
      <div className="border-t border-border p-4">
        <InputBox />
      </div>
    </div>
  );
}
EOF

# MessageList 컴포넌트
cat > components/chat/MessageList.tsx << 'EOF'
'use client'

import { useEffect, useRef } from 'react';
import { useConversationStore } from '@/lib/store';
import MessageItem from './MessageItem';
import { cn } from '@/lib/utils';

export default function MessageList() {
  const messages = useConversationStore((state) => state.messages);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  if (messages.length === 0) {
    return (
      <div className="flex items-center justify-center h-full">
        <div className="text-center space-y-4 max-w-md">
          <h2 className="text-2xl font-semibold">Welcome to GigaPress</h2>
          <p className="text-muted-foreground">
            Start by describing the project you want to create. I can help you build
            web applications, mobile apps, APIs, and more using natural language.
          </p>
          <div className="grid grid-cols-1 gap-2 text-sm">
            <button className="p-3 text-left rounded-lg border border-border hover:bg-accent transition-colors">
              "Create a shopping mall with product catalog and reviews"
            </button>
            <button className="p-3 text-left rounded-lg border border-border hover:bg-accent transition-colors">
              "Build a task management app with team collaboration"
            </button>
            <button className="p-3 text-left rounded-lg border border-border hover:bg-accent transition-colors">
              "Generate a REST API for a blog platform"
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="h-full overflow-y-auto scrollbar-thin scrollbar-thumb-border">
      <div className="max-w-4xl mx-auto p-4 space-y-4">
        {messages.map((message) => (
          <MessageItem key={message.id} message={message} />
        ))}
        <div ref={messagesEndRef} />
      </div>
    </div>
  );
}
EOF

# MessageItem 컴포넌트
cat > components/chat/MessageItem.tsx << 'EOF'
'use client'

import { Message } from '@/types';
import { cn, formatDate } from '@/lib/utils';
import { User, Bot, AlertCircle } from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { vscDarkPlus } from 'react-syntax-highlighter/dist/esm/styles/prism';

interface MessageItemProps {
  message: Message;
}

export default function MessageItem({ message }: MessageItemProps) {
  const isUser = message.role === 'user';
  const isError = message.status === 'error';

  return (
    <div
      className={cn(
        'flex gap-3 group',
        isUser && 'flex-row-reverse'
      )}
    >
      {/* Avatar */}
      <div
        className={cn(
          'flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center',
          isUser ? 'bg-primary text-primary-foreground' : 'bg-secondary'
        )}
      >
        {isUser ? <User size={16} /> : <Bot size={16} />}
      </div>

      {/* Message Content */}
      <div className={cn('flex-1 space-y-1', isUser && 'flex flex-col items-end')}>
        <div
          className={cn(
            'rounded-lg px-4 py-2 max-w-[80%] prose prose-sm dark:prose-invert',
            isUser
              ? 'bg-primary text-primary-foreground prose-invert'
              : 'bg-secondary',
            isError && 'bg-destructive text-destructive-foreground'
          )}
        >
          {isError && (
            <div className="flex items-center gap-2 mb-2">
              <AlertCircle size={16} />
              <span className="text-sm font-medium">Error</span>
            </div>
          )}
          
          <ReactMarkdown
            components={{
              code({ node, inline, className, children, ...props }) {
                const match = /language-(\w+)/.exec(className || '');
                return !inline && match ? (
                  <SyntaxHighlighter
                    style={vscDarkPlus}
                    language={match[1]}
                    PreTag="div"
                    {...props}
                  >
                    {String(children).replace(/\n$/, '')}
                  </SyntaxHighlighter>
                ) : (
                  <code className={className} {...props}>
                    {children}
                  </code>
                );
              },
            }}
          >
            {message.content}
          </ReactMarkdown>
        </div>
        
        {/* Timestamp */}
        <div className="text-xs text-muted-foreground opacity-0 group-hover:opacity-100 transition-opacity">
          {formatDate(message.timestamp)}
        </div>
      </div>
    </div>
  );
}
EOF

# InputBox 컴포넌트
cat > components/chat/InputBox.tsx << 'EOF'
'use client'

import { useState, useRef, KeyboardEvent } from 'react';
import { websocketService } from '@/lib/websocket';
import { useConversationStore } from '@/lib/store';
import { Send, Paperclip, Mic, Square } from 'lucide-react';
import { cn } from '@/lib/utils';
import toast from 'react-hot-toast';

export default function InputBox() {
  const [input, setInput] = useState('');
  const [isRecording, setIsRecording] = useState(false);
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const { isConnected } = useConversationStore();

  const handleSend = () => {
    if (!input.trim()) return;
    
    if (!isConnected) {
      toast.error('Not connected to server. Please wait...');
      return;
    }

    websocketService.sendMessage(input.trim());
    setInput('');
    
    // Reset textarea height
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto';
    }
  };

  const handleKeyDown = (e: KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setInput(e.target.value);
    
    // Auto-resize textarea
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto';
      textareaRef.current.style.height = `${textareaRef.current.scrollHeight}px`;
    }
  };

  const toggleRecording = () => {
    setIsRecording(!isRecording);
    toast.info(isRecording ? 'Recording stopped' : 'Recording started');
  };

  return (
    <div className="relative flex items-end gap-2">
      <div className="flex-1 relative">
        <textarea
          ref={textareaRef}
          value={input}
          onChange={handleInputChange}
          onKeyDown={handleKeyDown}
          placeholder="Describe your project or ask for modifications..."
          className={cn(
            'w-full resize-none rounded-lg border border-input bg-background px-3 py-2 pr-12',
            'text-sm ring-offset-background placeholder:text-muted-foreground',
            'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring',
            'disabled:cursor-not-allowed disabled:opacity-50',
            'min-h-[44px] max-h-[200px]'
          )}
          rows={1}
          disabled={!isConnected}
        />
        
        {/* Attachment button */}
        <button
          className="absolute right-2 bottom-2 p-1.5 rounded hover:bg-accent transition-colors"
          onClick={() => toast.info('File attachment coming soon!')}
          disabled={!isConnected}
        >
          <Paperclip size={18} className="text-muted-foreground" />
        </button>
      </div>

      {/* Voice input button */}
      <button
        className={cn(
          'p-2.5 rounded-lg transition-colors',
          isRecording
            ? 'bg-destructive text-destructive-foreground hover:bg-destructive/90'
            : 'bg-secondary hover:bg-secondary/80'
        )}
        onClick={toggleRecording}
        disabled={!isConnected}
      >
        {isRecording ? <Square size={20} /> : <Mic size={20} />}
      </button>

      {/* Send button */}
      <button
        className={cn(
          'p-2.5 rounded-lg transition-colors',
          input.trim() && isConnected
            ? 'bg-primary text-primary-foreground hover:bg-primary/90'
            : 'bg-secondary text-muted-foreground'
        )}
        onClick={handleSend}
        disabled={!input.trim() || !isConnected}
      >
        <Send size={20} />
      </button>
    </div>
  );
}
EOF

# ProgressTracker 컴포넌트
cat > components/project/ProgressTracker.tsx << 'EOF'
'use client'

import { useConversationStore } from '@/lib/store';
import { CheckCircle2, Circle, Loader2 } from 'lucide-react';
import { cn } from '@/lib/utils';

export default function ProgressTracker() {
  const progressUpdates = useConversationStore((state) => state.progressUpdates);
  
  if (progressUpdates.length === 0) return null;

  const latestUpdate = progressUpdates[progressUpdates.length - 1];
  const uniqueSteps = Array.from(
    new Map(progressUpdates.map(u => [u.step, u])).values()
  );

  return (
    <div className="p-4 space-y-4">
      {/* Current Progress */}
      <div className="space-y-2">
        <div className="flex items-center justify-between text-sm">
          <span className="font-medium">{latestUpdate.step}</span>
          <span className="text-muted-foreground">{latestUpdate.progress}%</span>
        </div>
        <div className="relative h-2 bg-secondary rounded-full overflow-hidden">
          <div
            className="absolute top-0 left-0 h-full bg-primary transition-all duration-500 ease-out"
            style={{ width: `${latestUpdate.progress}%` }}
          />
        </div>
        <p className="text-sm text-muted-foreground">{latestUpdate.message}</p>
      </div>

      {/* Steps Overview */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-2">
        {uniqueSteps.map((step, index) => {
          const isCompleted = step.progress === 100;
          const isCurrent = step.step === latestUpdate.step;
          
          return (
            <div
              key={step.step}
              className={cn(
                'flex items-center gap-2 p-2 rounded-lg text-sm',
                isCurrent && 'bg-secondary',
                isCompleted && 'text-muted-foreground'
              )}
            >
              {isCompleted ? (
                <CheckCircle2 size={16} className="text-green-500" />
              ) : isCurrent ? (
                <Loader2 size={16} className="animate-spin text-primary" />
              ) : (
                <Circle size={16} className="text-muted-foreground" />
              )}
              <span className="truncate">{step.step}</span>
            </div>
          );
        })}
      </div>
    </div>
  );
}
EOF

# ProjectStatus 컴포넌트
cat > components/project/ProjectStatus.tsx << 'EOF'
'use client'

import { useConversationStore } from '@/lib/store';
import { 
  Code2, 
  Database, 
  Layout, 
  Server, 
  GitBranch,
  CheckCircle,
  AlertCircle,
  Clock,
  Loader2
} from 'lucide-react';
import { cn } from '@/lib/utils';

export default function ProjectStatus() {
  const currentProject = useConversationStore((state) => state.currentProject);

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
      </div>

      {/* Components Status */}
      <div className="space-y-3">
        <h4 className="text-sm font-medium">Components</h4>
        <div className="space-y-2">
          {components.map(({ icon: Icon, label, key }) => {
            const hasComponent = currentProject.architecture?.[key];
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

      {/* Architecture Details */}
      {currentProject.architecture && (
        <div className="space-y-3">
          <h4 className="text-sm font-medium">Architecture Details</h4>
          <div className="space-y-2 text-sm">
            {currentProject.architecture.frontend && (
              <div>
                <span className="font-medium">Frontend:</span>{' '}
                <span className="text-muted-foreground">
                  {currentProject.architecture.frontend.framework}
                </span>
              </div>
            )}
            {currentProject.architecture.backend && (
              <div>
                <span className="font-medium">Backend:</span>{' '}
                <span className="text-muted-foreground">
                  {currentProject.architecture.backend.framework}
                </span>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Action Buttons */}
      <div className="space-y-2">
        <button className="w-full p-2 text-sm rounded-lg bg-primary text-primary-foreground hover:bg-primary/90 transition-colors">
          View Code
        </button>
        <button className="w-full p-2 text-sm rounded-lg border border-border hover:bg-accent transition-colors">
          Download Project
        </button>
      </div>
    </div>
  );
}
EOF

# ProjectSidebar 컴포넌트
cat > components/project/ProjectSidebar.tsx << 'EOF'
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
EOF

echo ""
echo "✅ Step 3 완료!"
echo "   - ChatInterface 컴포넌트"
echo "   - MessageList & MessageItem 컴포넌트"
echo "   - InputBox 컴포넌트"
echo "   - ProgressTracker 컴포넌트"
echo "   - ProjectStatus & ProjectSidebar 컴포넌트"
echo ""
echo "Step 4로 진행합니다..."