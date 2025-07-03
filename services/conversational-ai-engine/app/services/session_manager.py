from typing import Dict, Any, Optional, List
from datetime import datetime, timedelta
import json
import redis.asyncio as redis
from redis.asyncio.client import Redis
import logging
import pickle
from config.settings import settings
from app.models.conversation import ConversationSession, Message, ProjectContext

logger = logging.getLogger(__name__)


class SessionManager:
    """Manage conversation sessions with Redis persistence"""
    
    def __init__(self):
        self.redis_client: Optional[Redis] = None
        self.session_ttl = 3600 * 24  # 24 hours
        
    async def initialize(self):
        """Initialize session manager with Redis connection"""
        try:
            self.redis_client = await redis.from_url(
                f"redis://{settings.redis_host}:{settings.redis_port}",
                password=settings.redis_password,
                decode_responses=False  # We'll handle encoding/decoding
            )
            await self.redis_client.ping()
            logger.info("Session manager initialized with Redis")
        except Exception as e:
            logger.error(f"Failed to initialize session manager: {str(e)}")
            raise
    
    async def create_session(self, session_id: str) -> ConversationSession:
        """Create a new conversation session"""
        session = ConversationSession(
            session_id=session_id,
            created_at=datetime.utcnow(),
            last_activity=datetime.utcnow()
        )
        
        await self.save_session(session)
        logger.info(f"Created new session: {session_id}")
        return session
    
    async def get_session(self, session_id: str) -> Optional[ConversationSession]:
        """Get session from Redis"""
        try:
            key = f"session:{session_id}"
            data = await self.redis_client.get(key)
            
            if not data:
                return None
            
            # Deserialize session
            session_dict = pickle.loads(data)
            session = ConversationSession(**session_dict)
            
            # Update last activity
            session.last_activity = datetime.utcnow()
            await self.save_session(session)
            
            return session
            
        except Exception as e:
            logger.error(f"Failed to get session {session_id}: {str(e)}")
            return None
    
    async def save_session(self, session: ConversationSession):
        """Save session to Redis"""
        try:
            key = f"session:{session.session_id}"
            data = pickle.dumps(session.dict())
            
            await self.redis_client.setex(
                key,
                self.session_ttl,
                data
            )
            
            # Also update session index
            await self._update_session_index(session.session_id)
            
        except Exception as e:
            logger.error(f"Failed to save session {session.session_id}: {str(e)}")
            raise
    
    async def delete_session(self, session_id: str) -> bool:
        """Delete a session"""
        try:
            key = f"session:{session_id}"
            result = await self.redis_client.delete(key)
            
            # Remove from index
            await self.redis_client.srem("sessions:active", session_id)
            
            logger.info(f"Deleted session: {session_id}")
            return result > 0
            
        except Exception as e:
            logger.error(f"Failed to delete session {session_id}: {str(e)}")
            return False
    
    async def list_active_sessions(self) -> List[str]:
        """List all active session IDs"""
        try:
            sessions = await self.redis_client.smembers("sessions:active")
            return [s.decode() if isinstance(s, bytes) else s for s in sessions]
        except Exception as e:
            logger.error(f"Failed to list active sessions: {str(e)}")
            return []
    
    async def add_message(
        self,
        session_id: str,
        role: str,
        content: str,
        metadata: Optional[Dict[str, Any]] = None
    ) -> Optional[Message]:
        """Add a message to a session"""
        session = await self.get_session(session_id)
        if not session:
            session = await self.create_session(session_id)
        
        message = Message(
            role=role,
            content=content,
            timestamp=datetime.utcnow(),
            metadata=metadata or {}
        )
        
        session.messages.append(message)
        session.last_activity = datetime.utcnow()
        
        await self.save_session(session)
        return message
    
    async def get_conversation_history(
        self,
        session_id: str,
        limit: Optional[int] = None
    ) -> List[Message]:
        """Get conversation history for a session"""
        session = await self.get_session(session_id)
        if not session:
            return []
        
        messages = session.messages
        if limit:
            messages = messages[-limit:]
        
        return messages
    
    async def update_session_context(
        self,
        session_id: str,
        context_update: Dict[str, Any]
    ):
        """Update session context"""
        session = await self.get_session(session_id)
        if session:
            session.context.update(context_update)
            await self.save_session(session)
    
    async def get_session_stats(self, session_id: str) -> Dict[str, Any]:
        """Get statistics for a session"""
        session = await self.get_session(session_id)
        if not session:
            return {}
        
        return {
            "session_id": session_id,
            "created_at": session.created_at.isoformat(),
            "last_activity": session.last_activity.isoformat(),
            "message_count": len(session.messages),
            "duration": (session.last_activity - session.created_at).total_seconds(),
            "has_project": "current_project" in session.context
        }
    
    async def cleanup_old_sessions(self, hours: int = 24):
        """Clean up sessions older than specified hours"""
        try:
            active_sessions = await self.list_active_sessions()
            cutoff_time = datetime.utcnow() - timedelta(hours=hours)
            cleaned = 0
            
            for session_id in active_sessions:
                session = await self.get_session(session_id)
                if session and session.last_activity < cutoff_time:
                    await self.delete_session(session_id)
                    cleaned += 1
            
            logger.info(f"Cleaned up {cleaned} old sessions")
            return cleaned
            
        except Exception as e:
            logger.error(f"Failed to cleanup old sessions: {str(e)}")
            return 0
    
    async def _update_session_index(self, session_id: str):
        """Update session index in Redis"""
        await self.redis_client.sadd("sessions:active", session_id)


# Singleton instance
session_manager = SessionManager()
