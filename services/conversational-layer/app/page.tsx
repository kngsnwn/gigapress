'use client'

import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { websocketService } from '@/lib/websocket';
import { useIsMobile } from '@/lib/hooks/useMediaQuery';
import ChatInterface from '@/components/chat/ChatInterface';
import ChatInterfaceMobile from '@/components/chat/ChatInterfaceMobile';
import ProjectSidebar from '@/components/project/ProjectSidebar';
import Header from '@/components/layout/Header';
import Loading from '@/components/ui/Loading';
import ModeSelector from '@/components/ModeSelector';
import { fadeIn } from '@/lib/animations';
import { useConversationStore } from '@/lib/store';

export default function Home() {
  const isMobile = useIsMobile();
  const isConnected = useConversationStore((state) => state.isConnected);
  const isDemoMode = useConversationStore((state) => state.isDemoMode);
  const [showModeSelector, setShowModeSelector] = useState(true);

  useEffect(() => {
    // Only connect to WebSocket if not in demo mode
    if (!isDemoMode && !showModeSelector) {
      const wsUrl = process.env.NEXT_PUBLIC_WS_URL || 'http://localhost:8087';
      websocketService.connect(wsUrl);
    }

    // Cleanup on unmount
    return () => {
      if (!isDemoMode) {
        websocketService.disconnect();
      }
    };
  }, [isDemoMode, showModeSelector]);

  // Show mode selector first
  if (showModeSelector) {
    return <ModeSelector onClose={() => setShowModeSelector(false)} />;
  }

  // Show loading screen while connecting (only in real mode)
  if (!isDemoMode && !isConnected && !isMobile) {
    return (
      <div className="flex h-screen items-center justify-center">
        <Loading size="lg" text="Connecting to GigaPress..." />
      </div>
    );
  }

  return (
    <motion.div
      {...fadeIn}
      className="flex h-screen bg-background"
    >
      {/* Desktop Layout */}
      {!isMobile && (
        <>
          {/* Sidebar */}
          <ProjectSidebar />
          
          {/* Main Content */}
          <div className="flex-1 flex flex-col">
            <Header />
            <main className="flex-1 overflow-hidden">
              <ChatInterface />
            </main>
          </div>
        </>
      )}

      {/* Mobile Layout */}
      {isMobile && (
        <div className="flex flex-col h-full">
          <Header />
          <main className="flex-1 overflow-hidden">
            <ChatInterfaceMobile />
          </main>
        </div>
      )}
    </motion.div>
  );
}
