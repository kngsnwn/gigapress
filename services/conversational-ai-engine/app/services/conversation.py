from typing import Dict, Any, List, Optional, AsyncGenerator
from datetime import datetime
import json
import logging
from langchain.schema import HumanMessage, AIMessage

from app.core.langchain_config import langchain_service
from app.services.chains import chain_service
from app.models.conversation import ConversationSession, Message
from app.core.exceptions import ValidationException

logger = logging.getLogger(__name__)


class ConversationService:
    """Service for managing conversations"""
    
    def __init__(self):
        self.sessions: Dict[str, ConversationSession] = {}
        
    async def initialize(self):
        """Initialize conversation service"""
        await langchain_service.initialize()
        await chain_service.initialize()
        logger.info("Conversation service initialized")
    
    async def process_message(
        self,
        message: str,
        session_id: str,
        context: Optional[Dict[str, Any]] = None
    ) -> Dict[str, Any]:
        """Process a user message and generate response"""
        try:
            # Get or create session
            session = self._get_or_create_session(session_id)
            
            # Add user message to history
            user_message = Message(
                role="user",
                content=message,
                timestamp=datetime.utcnow()
            )
            session.messages.append(user_message)
            
            # Analyze intent
            intent_analysis = await langchain_service.analyze_intent(message)
            
            # Get conversation chain
            chain = await langchain_service.get_conversation_chain(session_id)
            
            # Generate response
            response = await chain.arun(input=message)
            
            # Add AI response to history
            ai_message = Message(
                role="assistant",
                content=response,
                timestamp=datetime.utcnow(),
                metadata=intent_analysis
            )
            session.messages.append(ai_message)
            
            # Update session
            session.last_activity = datetime.utcnow()
            session.context.update(context or {})
            
            return {
                "response": response,
                "session_id": session_id,
                "intent": intent_analysis,
                "timestamp": ai_message.timestamp,
                "message_count": len(session.messages)
            }
            
        except Exception as e:
            logger.error(f"Failed to process message: {str(e)}")
            raise
    
    async def stream_response(
        self,
        message: str,
        session_id: str,
        context: Optional[Dict[str, Any]] = None
    ) -> AsyncGenerator[str, None]:
        """Stream response tokens as they're generated"""
        try:
            session = self._get_or_create_session(session_id)
            
            # Add user message
            user_message = Message(
                role="user",
                content=message,
                timestamp=datetime.utcnow()
            )
            session.messages.append(user_message)
            
            # Stream response
            chain = await langchain_service.get_conversation_chain(session_id)
            
            full_response = ""
            async for token in chain.astream({"input": message}):
                if "response" in token:
                    chunk = token["response"]
                    full_response += chunk
                    yield chunk
            
            # Save complete response
            ai_message = Message(
                role="assistant",
                content=full_response,
                timestamp=datetime.utcnow()
            )
            session.messages.append(ai_message)
            
        except Exception as e:
            logger.error(f"Failed to stream response: {str(e)}")
            yield f"Error: {str(e)}"
    
    def get_session(self, session_id: str) -> Optional[ConversationSession]:
        """Get session by ID"""
        return self.sessions.get(session_id)
    
    def _get_or_create_session(self, session_id: str) -> ConversationSession:
        """Get existing session or create new one"""
        if session_id not in self.sessions:
            self.sessions[session_id] = ConversationSession(
                session_id=session_id,
                created_at=datetime.utcnow(),
                last_activity=datetime.utcnow()
            )
        return self.sessions[session_id]
    
    def clear_session(self, session_id: str) -> bool:
        """Clear a conversation session"""
        if session_id in self.sessions:
            del self.sessions[session_id]
            langchain_service.clear_memory(session_id)
            logger.info(f"Cleared session: {session_id}")
            return True
        return False
    
    def get_session_history(self, session_id: str) -> List[Dict[str, Any]]:
        """Get conversation history for a session"""
        session = self.sessions.get(session_id)
        if not session:
            return []
        
        return [
            {
                "role": msg.role,
                "content": msg.content,
                "timestamp": msg.timestamp.isoformat(),
                "metadata": msg.metadata
            }
            for msg in session.messages
        ]
    
    async def analyze_project_request(
        self,
        description: str,
        session_id: str
    ) -> Dict[str, Any]:
        """Analyze a project generation request"""
        session = self._get_or_create_session(session_id)
        
        # Extract requirements
        requirements = await chain_service.analyze_project_request(
            description=description,
            context=session.context
        )
        
        # Create implementation plan
        plan = await chain_service.plan_project_implementation(requirements)
        
        # Store in session context
        session.context["current_project"] = {
            "requirements": requirements.dict(),
            "plan": plan
        }
        
        return {
            "requirements": requirements.dict(),
            "plan": plan,
            "session_id": session_id
        }


# Singleton instance
conversation_service = ConversationService()
