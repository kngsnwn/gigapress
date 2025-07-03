'use client'

import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useIsMobile } from '@/lib/hooks/useMediaQuery';
import { useConversationStore } from '@/lib/store';
import MessageList from './MessageList';
import InputBox from './InputBox';
import ProgressTracker from '../project/ProgressTracker';
import ProjectStatus from '../project/ProjectStatus';
import { Menu, X, FolderOpen } from 'lucide-react';
import { cn } from '@/lib/utils';
import { slideIn } from '@/lib/animations';

export default function ChatInterfaceMobile() {
  const [showProjects, setShowProjects] = useState(false);
  const { isTyping, progressUpdates, currentProject } = useConversationStore();

  return (
    <div className="flex flex-col h-full relative">
      {/* Mobile Header */}
      <div className="flex items-center justify-between p-4 border-b border-border md:hidden">
        <button
          onClick={() => setShowProjects(!showProjects)}
          className="p-2 rounded-lg hover:bg-accent transition-colors"
        >
          {showProjects ? <X size={20} /> : <Menu size={20} />}
        </button>
        
        {currentProject && (
          <div className="flex-1 mx-4">
            <p className="text-sm font-medium truncate">{currentProject.name}</p>
          </div>
        )}
        
        <button className="p-2 rounded-lg hover:bg-accent transition-colors">
          <FolderOpen size={20} />
        </button>
      </div>

      {/* Mobile Project Drawer */}
      <AnimatePresence>
        {showProjects && (
          <>
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 0.5 }}
              exit={{ opacity: 0 }}
              className="fixed inset-0 bg-black z-40 md:hidden"
              onClick={() => setShowProjects(false)}
            />
            <motion.div
              {...slideIn}
              className="fixed left-0 top-0 bottom-0 w-80 bg-card border-r border-border z-50 md:hidden"
            >
              <div className="p-4 border-b border-border">
                <h2 className="font-semibold">Projects</h2>
              </div>
              <div className="overflow-y-auto">
                <ProjectStatus />
              </div>
            </motion.div>
          </>
        )}
      </AnimatePresence>

      {/* Progress Tracker */}
      {progressUpdates.length > 0 && (
        <motion.div
          initial={{ height: 0 }}
          animate={{ height: 'auto' }}
          exit={{ height: 0 }}
          className="border-b border-border overflow-hidden"
        >
          <ProgressTracker />
        </motion.div>
      )}

      {/* Messages Area */}
      <div className="flex-1 overflow-hidden">
        <MessageList />
      </div>

      {/* Typing Indicator */}
      <AnimatePresence>
        {isTyping && (
          <motion.div
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 10 }}
            className="px-4 py-2 text-sm text-muted-foreground"
          >
            AI is typing...
          </motion.div>
        )}
      </AnimatePresence>

      {/* Input Area */}
      <div className="border-t border-border p-4">
        <InputBox />
      </div>
    </div>
  );
}
