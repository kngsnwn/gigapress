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
