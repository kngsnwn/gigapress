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
