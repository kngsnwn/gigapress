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
