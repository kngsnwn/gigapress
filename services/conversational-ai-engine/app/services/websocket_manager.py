from typing import Dict, Set, Any
from fastapi import WebSocket
import json
import logging
from datetime import datetime

logger = logging.getLogger(__name__)


class WebSocketManager:
    """Manage WebSocket connections"""
    
    def __init__(self):
        # Map session_id to set of connections
        self.connections: Dict[str, Set[WebSocket]] = {}
        
    async def connect(self, websocket: WebSocket, session_id: str):
        """Accept a new WebSocket connection"""
        await websocket.accept()
        
        if session_id not in self.connections:
            self.connections[session_id] = set()
        
        self.connections[session_id].add(websocket)
        logger.info(f"WebSocket connected for session: {session_id}")
        
        # Send welcome message
        await self.send_to_websocket(
            websocket,
            {
                "type": "connected",
                "session_id": session_id,
                "timestamp": datetime.utcnow().isoformat()
            }
        )
    
    def disconnect(self, websocket: WebSocket, session_id: str):
        """Remove a WebSocket connection"""
        if session_id in self.connections:
            self.connections[session_id].discard(websocket)
            
            # Remove empty sets
            if not self.connections[session_id]:
                del self.connections[session_id]
        
        logger.info(f"WebSocket disconnected for session: {session_id}")
    
    async def send_to_session(self, session_id: str, data: Dict[str, Any]):
        """Send data to all connections for a session"""
        if session_id not in self.connections:
            return
        
        disconnected = set()
        
        for websocket in self.connections[session_id]:
            try:
                await self.send_to_websocket(websocket, data)
            except Exception as e:
                logger.error(f"Failed to send to websocket: {str(e)}")
                disconnected.add(websocket)
        
        # Remove disconnected websockets
        for ws in disconnected:
            self.connections[session_id].discard(ws)
    
    async def send_to_websocket(self, websocket: WebSocket, data: Dict[str, Any]):
        """Send data to a specific websocket"""
        await websocket.send_json(data)
    
    async def broadcast(self, data: Dict[str, Any]):
        """Broadcast to all connected clients"""
        all_websockets = set()
        for session_sockets in self.connections.values():
            all_websockets.update(session_sockets)
        
        disconnected = set()
        
        for websocket in all_websockets:
            try:
                await self.send_to_websocket(websocket, data)
            except Exception:
                disconnected.add(websocket)
        
        # Clean up disconnected sockets
        for session_id, sockets in self.connections.items():
            for ws in disconnected:
                sockets.discard(ws)
    
    def get_active_sessions(self) -> List[str]:
        """Get list of sessions with active WebSocket connections"""
        return list(self.connections.keys())
    
    def get_connection_count(self) -> int:
        """Get total number of active connections"""
        return sum(len(sockets) for sockets in self.connections.values())


# Singleton instance
websocket_manager = WebSocketManager()
