'use client'

import { useEffect, useRef } from 'react';
import { websocketService } from '@/lib/websocket';
import { useConversationStore } from '@/lib/store';

export function useWebSocket(url?: string) {
  const wsUrl = url || process.env.NEXT_PUBLIC_WS_URL || 'http://localhost:8087';
  const reconnectTimeoutRef = useRef<NodeJS.Timeout>();
  const isConnected = useConversationStore((state) => state.isConnected);

  useEffect(() => {
    const connect = () => {
      websocketService.connect(wsUrl);
    };

    const handleReconnect = () => {
      if (!isConnected) {
        reconnectTimeoutRef.current = setTimeout(() => {
          console.log('Attempting to reconnect...');
          connect();
        }, 5000);
      }
    };

    // Initial connection
    connect();

    // Set up reconnection logic
    const interval = setInterval(() => {
      if (!isConnected) {
        handleReconnect();
      }
    }, 10000);

    return () => {
      clearInterval(interval);
      if (reconnectTimeoutRef.current) {
        clearTimeout(reconnectTimeoutRef.current);
      }
      websocketService.disconnect();
    };
  }, [wsUrl, isConnected]);

  return {
    isConnected,
    sendMessage: websocketService.sendMessage.bind(websocketService),
    sendProjectAction: websocketService.sendProjectAction.bind(websocketService),
  };
}
