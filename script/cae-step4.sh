#!/bin/bash

# Step 4: Conversation Management System
echo "🚀 Setting up Conversational AI Engine - Step 4: Conversation Management"

cd conversational-ai-engine

# Create session manager
echo "📝 Creating session manager..."
cat > app/services/session_manager.py << 'EOF'
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
EOF

# Create context manager
echo "📝 Creating context manager..."
cat > app/services/context_manager.py << 'EOF'
from typing import Dict, Any, List, Optional
from datetime import datetime
import logging
from app.models.conversation import ProjectContext
from app.services.session_manager import session_manager

logger = logging.getLogger(__name__)


class ContextManager:
    """Manage conversation context and project state"""
    
    def __init__(self):
        self.project_contexts: Dict[str, ProjectContext] = {}
    
    async def initialize(self):
        """Initialize context manager"""
        logger.info("Context manager initialized")
    
    async def get_or_create_project_context(
        self,
        session_id: str,
        project_id: Optional[str] = None
    ) -> ProjectContext:
        """Get or create project context for a session"""
        session = await session_manager.get_session(session_id)
        if not session:
            raise ValueError(f"Session {session_id} not found")
        
        # Check if project context exists in session
        if "project_context" in session.context:
            return ProjectContext(**session.context["project_context"])
        
        # Create new project context
        context = ProjectContext(project_id=project_id)
        await self.update_project_context(session_id, context)
        
        return context
    
    async def update_project_context(
        self,
        session_id: str,
        context: ProjectContext
    ):
        """Update project context in session"""
        await session_manager.update_session_context(
            session_id,
            {"project_context": context.dict()}
        )
    
    async def add_modification(
        self,
        session_id: str,
        modification: Dict[str, Any]
    ):
        """Add a modification to project history"""
        context = await self.get_or_create_project_context(session_id)
        
        modification["timestamp"] = datetime.utcnow().isoformat()
        context.modifications.append(modification)
        
        await self.update_project_context(session_id, context)
    
    async def update_project_state(
        self,
        session_id: str,
        state_update: Dict[str, Any]
    ):
        """Update current project state"""
        context = await self.get_or_create_project_context(session_id)
        context.current_state.update(state_update)
        
        await self.update_project_context(session_id, context)
    
    async def get_relevant_context(
        self,
        session_id: str,
        include_history: bool = True
    ) -> Dict[str, Any]:
        """Get relevant context for AI processing"""
        session = await session_manager.get_session(session_id)
        if not session:
            return {}
        
        context = {
            "session_id": session_id,
            "message_count": len(session.messages)
        }
        
        # Add project context if exists
        if "project_context" in session.context:
            project_context = ProjectContext(**session.context["project_context"])
            context["project"] = {
                "id": project_context.project_id,
                "type": project_context.project_type,
                "current_state": project_context.current_state,
                "requirements": project_context.requirements,
                "modification_count": len(project_context.modifications)
            }
        
        # Add recent conversation history
        if include_history:
            recent_messages = await session_manager.get_conversation_history(
                session_id,
                limit=5
            )
            context["recent_conversation"] = [
                {
                    "role": msg.role,
                    "content": msg.content[:100] + "..." if len(msg.content) > 100 else msg.content
                }
                for msg in recent_messages
            ]
        
        return context
    
    async def extract_entities(self, text: str) -> Dict[str, List[str]]:
        """Extract entities from text (simplified version)"""
        entities = {
            "technologies": [],
            "features": [],
            "project_types": []
        }
        
        # Technology keywords
        tech_keywords = [
            "react", "vue", "angular", "node", "python", "java", "spring",
            "django", "fastapi", "postgresql", "mysql", "mongodb", "redis",
            "docker", "kubernetes", "aws", "azure", "gcp"
        ]
        
        # Feature keywords
        feature_keywords = [
            "authentication", "authorization", "api", "database", "frontend",
            "backend", "mobile", "responsive", "real-time", "chat", "payment",
            "search", "analytics", "dashboard", "admin"
        ]
        
        # Project type keywords
        project_keywords = [
            "web app", "mobile app", "api", "microservice", "website",
            "platform", "system", "tool", "application"
        ]
        
        text_lower = text.lower()
        
        # Extract technologies
        for tech in tech_keywords:
            if tech in text_lower:
                entities["technologies"].append(tech)
        
        # Extract features
        for feature in feature_keywords:
            if feature in text_lower:
                entities["features"].append(feature)
        
        # Extract project types
        for project in project_keywords:
            if project in text_lower:
                entities["project_types"].append(project)
        
        return entities
    
    async def determine_intent(
        self,
        message: str,
        context: Dict[str, Any]
    ) -> Dict[str, Any]:
        """Determine user intent from message and context"""
        message_lower = message.lower()
        
        # Check for project creation intent
        create_keywords = ["create", "build", "make", "develop", "generate", "new"]
        if any(keyword in message_lower for keyword in create_keywords):
            if not context.get("project"):
                return {
                    "primary": "PROJECT_CREATE",
                    "confidence": 0.9,
                    "entities": await self.extract_entities(message)
                }
        
        # Check for modification intent
        modify_keywords = ["change", "modify", "update", "add", "remove", "delete", "edit"]
        if any(keyword in message_lower for keyword in modify_keywords):
            if context.get("project"):
                return {
                    "primary": "PROJECT_MODIFY",
                    "confidence": 0.85,
                    "entities": await self.extract_entities(message)
                }
        
        # Check for information intent
        info_keywords = ["what", "how", "why", "when", "where", "status", "show", "list"]
        if any(keyword in message_lower for keyword in info_keywords):
            return {
                "primary": "INFORMATION_REQUEST",
                "confidence": 0.8,
                "sub_intent": "project_info" if context.get("project") else "general_info"
            }
        
        # Check for help intent
        help_keywords = ["help", "guide", "tutorial", "example", "how to"]
        if any(keyword in message_lower for keyword in help_keywords):
            return {
                "primary": "HELP_REQUEST",
                "confidence": 0.9
            }
        
        # Default to clarification needed
        return {
            "primary": "CLARIFICATION_NEEDED",
            "confidence": 0.6,
            "reason": "Unable to determine clear intent"
        }


# Singleton instance
context_manager = ContextManager()
EOF

# Create intent classifier
echo "📝 Creating intent classifier..."
cat > app/services/intent_classifier.py << 'EOF'
from typing import Dict, Any, List, Tuple
from enum import Enum
import re
import logging
from app.services.context_manager import context_manager

logger = logging.getLogger(__name__)


class Intent(Enum):
    """User intent types"""
    PROJECT_CREATE = "project_create"
    PROJECT_MODIFY = "project_modify"
    PROJECT_INFO = "project_info"
    CLARIFICATION = "clarification"
    GENERAL_QUERY = "general_query"
    HELP = "help"
    GREETING = "greeting"
    UNKNOWN = "unknown"


class IntentClassifier:
    """Classify user intents from messages"""
    
    def __init__(self):
        self.intent_patterns = self._build_intent_patterns()
        
    def _build_intent_patterns(self) -> Dict[Intent, List[re.Pattern]]:
        """Build regex patterns for intent classification"""
        return {
            Intent.PROJECT_CREATE: [
                re.compile(r'\b(create|build|make|develop|generate|start)\s+(?:a\s+)?(?:new\s+)?(project|app|application|website|api|service)\b', re.I),
                re.compile(r'\b(i\s+want|i\s+need|help\s+me)\s+(?:to\s+)?(create|build|make)\b', re.I),
                re.compile(r'\b(new|fresh)\s+(project|application|app)\b', re.I)
            ],
            Intent.PROJECT_MODIFY: [
                re.compile(r'\b(change|modify|update|add|remove|delete|edit)\s+(?:the\s+)?\w+', re.I),
                re.compile(r'\b(can\s+you|please|i\s+want\s+to)\s+(change|modify|update)', re.I),
                re.compile(r'\b(implement|integrate|include)\s+\w+\s+(?:feature|functionality)', re.I)
            ],
            Intent.PROJECT_INFO: [
                re.compile(r'\b(show|display|what|get)\s+(?:me\s+)?(?:the\s+)?(status|info|information|details|project)\b', re.I),
                re.compile(r'\b(current|existing)\s+(project|state|status)\b', re.I),
                re.compile(r'\bproject\s+(details|info|status)\b', re.I)
            ],
            Intent.HELP: [
                re.compile(r'\b(help|guide|how\s+to|tutorial|example|what\s+can)\b', re.I),
                re.compile(r'\b(explain|tell\s+me)\s+(?:about|how)\b', re.I),
                re.compile(r'\b(?:i\s+don\'t\s+understand|confused|not\s+sure)\b', re.I)
            ],
            Intent.GREETING: [
                re.compile(r'^(hi|hello|hey|greetings|good\s+(morning|afternoon|evening))[\s!]*$', re.I),
                re.compile(r'^(how\s+are\s+you|what\'s\s+up)[\s?]*$', re.I)
            ]
        }
    
    async def classify(
        self,
        message: str,
        session_id: str
    ) -> Tuple[Intent, float, Dict[str, Any]]:
        """Classify intent with confidence score and metadata"""
        # Get context
        context = await context_manager.get_relevant_context(session_id, include_history=True)
        
        # Clean message
        message_clean = message.strip().lower()
        
        # Check patterns
        intent_scores: Dict[Intent, float] = {}
        
        for intent, patterns in self.intent_patterns.items():
            max_score = 0.0
            for pattern in patterns:
                if pattern.search(message):
                    # Base score for pattern match
                    score = 0.7
                    
                    # Adjust based on context
                    if intent == Intent.PROJECT_MODIFY and context.get("project"):
                        score += 0.2  # Boost if project exists
                    elif intent == Intent.PROJECT_CREATE and not context.get("project"):
                        score += 0.2  # Boost if no project exists
                    
                    max_score = max(max_score, score)
            
            if max_score > 0:
                intent_scores[intent] = max_score
        
        # Get best intent
        if intent_scores:
            best_intent = max(intent_scores.items(), key=lambda x: x[1])
            intent, confidence = best_intent
        else:
            # Use context-based classification
            intent, confidence = await self._context_based_classification(message, context)
        
        # Extract metadata
        metadata = await self._extract_metadata(message, intent)
        
        logger.info(f"Classified intent: {intent.value} (confidence: {confidence:.2f})")
        
        return intent, confidence, metadata
    
    async def _context_based_classification(
        self,
        message: str,
        context: Dict[str, Any]
    ) -> Tuple[Intent, float]:
        """Classify based on context when no patterns match"""
        # Check if it's a follow-up to previous conversation
        if context.get("recent_conversation"):
            last_message = context["recent_conversation"][-1] if context["recent_conversation"] else None
            if last_message and last_message["role"] == "assistant":
                # Likely a clarification or response
                return Intent.CLARIFICATION, 0.6
        
        # Check message length and structure
        if len(message.split()) < 5:
            # Short message, might be clarification
            return Intent.CLARIFICATION, 0.5
        
        # Default to unknown
        return Intent.UNKNOWN, 0.3
    
    async def _extract_metadata(
        self,
        message: str,
        intent: Intent
    ) -> Dict[str, Any]:
        """Extract metadata based on intent"""
        metadata = {
            "intent": intent.value,
            "entities": await context_manager.extract_entities(message)
        }
        
        # Intent-specific metadata
        if intent == Intent.PROJECT_CREATE:
            # Extract project type
            project_types = ["web app", "mobile app", "api", "microservice", "website"]
            for ptype in project_types:
                if ptype in message.lower():
                    metadata["project_type"] = ptype
                    break
        
        elif intent == Intent.PROJECT_MODIFY:
            # Extract modification type
            mod_types = ["add", "remove", "change", "update", "delete"]
            for mtype in mod_types:
                if mtype in message.lower():
                    metadata["modification_type"] = mtype
                    break
        
        return metadata
    
    def get_intent_description(self, intent: Intent) -> str:
        """Get human-readable description of intent"""
        descriptions = {
            Intent.PROJECT_CREATE: "Create a new project",
            Intent.PROJECT_MODIFY: "Modify existing project",
            Intent.PROJECT_INFO: "Get project information",
            Intent.CLARIFICATION: "Provide clarification",
            Intent.GENERAL_QUERY: "General question",
            Intent.HELP: "Request for help",
            Intent.GREETING: "Greeting",
            Intent.UNKNOWN: "Unknown intent"
        }
        return descriptions.get(intent, "Unknown")


# Singleton instance
intent_classifier = IntentClassifier()
EOF

# Create state tracker
echo "📝 Creating state tracker..."
cat > app/services/state_tracker.py << 'EOF'
from typing import Dict, Any, List, Optional
from enum import Enum
from datetime import datetime
import logging
from app.services.session_manager import session_manager
from app.services.context_manager import context_manager

logger = logging.getLogger(__name__)


class ConversationState(Enum):
    """Conversation state types"""
    INITIAL = "initial"
    GATHERING_REQUIREMENTS = "gathering_requirements"
    CONFIRMING_DETAILS = "confirming_details"
    PROCESSING = "processing"
    AWAITING_FEEDBACK = "awaiting_feedback"
    COMPLETED = "completed"
    ERROR = "error"


class ProjectState(Enum):
    """Project state types"""
    NOT_STARTED = "not_started"
    PLANNING = "planning"
    IN_PROGRESS = "in_progress"
    MODIFYING = "modifying"
    COMPLETED = "completed"
    FAILED = "failed"


class StateTracker:
    """Track conversation and project states"""
    
    def __init__(self):
        self.state_transitions = self._define_state_transitions()
    
    def _define_state_transitions(self) -> Dict[ConversationState, List[ConversationState]]:
        """Define valid state transitions"""
        return {
            ConversationState.INITIAL: [
                ConversationState.GATHERING_REQUIREMENTS,
                ConversationState.CONFIRMING_DETAILS,
                ConversationState.ERROR
            ],
            ConversationState.GATHERING_REQUIREMENTS: [
                ConversationState.CONFIRMING_DETAILS,
                ConversationState.GATHERING_REQUIREMENTS,  # Can loop
                ConversationState.ERROR
            ],
            ConversationState.CONFIRMING_DETAILS: [
                ConversationState.PROCESSING,
                ConversationState.GATHERING_REQUIREMENTS,  # Back for more info
                ConversationState.ERROR
            ],
            ConversationState.PROCESSING: [
                ConversationState.AWAITING_FEEDBACK,
                ConversationState.COMPLETED,
                ConversationState.ERROR
            ],
            ConversationState.AWAITING_FEEDBACK: [
                ConversationState.PROCESSING,  # Make changes
                ConversationState.COMPLETED,
                ConversationState.GATHERING_REQUIREMENTS,  # Major changes
                ConversationState.ERROR
            ],
            ConversationState.COMPLETED: [
                ConversationState.GATHERING_REQUIREMENTS,  # New request
                ConversationState.INITIAL
            ],
            ConversationState.ERROR: [
                ConversationState.INITIAL,  # Restart
                ConversationState.GATHERING_REQUIREMENTS  # Try again
            ]
        }
    
    async def get_conversation_state(self, session_id: str) -> ConversationState:
        """Get current conversation state"""
        session = await session_manager.get_session(session_id)
        if not session:
            return ConversationState.INITIAL
        
        state_str = session.metadata.get("conversation_state", ConversationState.INITIAL.value)
        return ConversationState(state_str)
    
    async def get_project_state(self, session_id: str) -> ProjectState:
        """Get current project state"""
        context = await context_manager.get_relevant_context(session_id)
        if not context.get("project"):
            return ProjectState.NOT_STARTED
        
        project = context["project"]
        state_str = project.get("state", ProjectState.NOT_STARTED.value)
        return ProjectState(state_str)
    
    async def transition_conversation_state(
        self,
        session_id: str,
        new_state: ConversationState,
        metadata: Optional[Dict[str, Any]] = None
    ) -> bool:
        """Transition to a new conversation state"""
        current_state = await self.get_conversation_state(session_id)
        
        # Check if transition is valid
        valid_transitions = self.state_transitions.get(current_state, [])
        if new_state not in valid_transitions:
            logger.warning(
                f"Invalid state transition: {current_state.value} -> {new_state.value}"
            )
            return False
        
        # Update state
        session = await session_manager.get_session(session_id)
        if session:
            session.metadata["conversation_state"] = new_state.value
            session.metadata["state_updated_at"] = datetime.utcnow().isoformat()
            
            if metadata:
                session.metadata.update(metadata)
            
            await session_manager.save_session(session)
            
            logger.info(
                f"Conversation state transition: {current_state.value} -> {new_state.value}"
            )
            return True
        
        return False
    
    async def update_project_state(
        self,
        session_id: str,
        new_state: ProjectState,
        metadata: Optional[Dict[str, Any]] = None
    ) -> bool:
        """Update project state"""
        await context_manager.update_project_state(
            session_id,
            {
                "state": new_state.value,
                "state_updated_at": datetime.utcnow().isoformat(),
                **(metadata or {})
            }
        )
        
        logger.info(f"Project state updated to: {new_state.value}")
        return True
    
    async def should_gather_more_info(self, session_id: str) -> bool:
        """Determine if more information is needed"""
        context = await context_manager.get_relevant_context(session_id)
        
        if not context.get("project"):
            return True
        
        project = context["project"]
        required_fields = ["type", "requirements", "current_state"]
        
        for field in required_fields:
            if not project.get(field):
                return True
        
        # Check if requirements are complete
        requirements = project.get("requirements", {})
        if not requirements or len(requirements) < 3:
            return True
        
        return False
    
    async def get_next_action(
        self,
        session_id: str,
        intent: str
    ) -> Dict[str, Any]:
        """Determine next action based on state and intent"""
        conv_state = await self.get_conversation_state(session_id)
        proj_state = await self.get_project_state(session_id)
        
        # Decision matrix
        if conv_state == ConversationState.INITIAL:
            if intent == "PROJECT_CREATE":
                return {
                    "action": "gather_requirements",
                    "next_state": ConversationState.GATHERING_REQUIREMENTS,
                    "message": "I'll help you create a new project. Can you tell me more about what you want to build?"
                }
            elif intent == "HELP":
                return {
                    "action": "provide_help",
                    "next_state": ConversationState.INITIAL,
                    "message": "I can help you create projects, modify existing ones, or answer questions."
                }
        
        elif conv_state == ConversationState.GATHERING_REQUIREMENTS:
            if await self.should_gather_more_info(session_id):
                return {
                    "action": "ask_clarification",
                    "next_state": ConversationState.GATHERING_REQUIREMENTS,
                    "message": "I need more information to proceed."
                }
            else:
                return {
                    "action": "confirm_details",
                    "next_state": ConversationState.CONFIRMING_DETAILS,
                    "message": "Let me confirm the details before we proceed."
                }
        
        elif conv_state == ConversationState.CONFIRMING_DETAILS:
            return {
                "action": "start_processing",
                "next_state": ConversationState.PROCESSING,
                "message": "Great! I'll start creating your project now."
            }
        
        # Default action
        return {
            "action": "continue_conversation",
            "next_state": conv_state,
            "message": "How can I help you with your project?"
        }
    
    async def get_state_summary(self, session_id: str) -> Dict[str, Any]:
        """Get comprehensive state summary"""
        conv_state = await self.get_conversation_state(session_id)
        proj_state = await self.get_project_state(session_id)
        context = await context_manager.get_relevant_context(session_id)
        
        summary = {
            "conversation_state": conv_state.value,
            "project_state": proj_state.value,
            "has_active_project": bool(context.get("project")),
            "message_count": context.get("message_count", 0),
            "session_duration": None
        }
        
        # Calculate session duration
        session = await session_manager.get_session(session_id)
        if session:
            duration = datetime.utcnow() - session.created_at
            summary["session_duration"] = duration.total_seconds()
        
        return summary


# Singleton instance
state_tracker = StateTracker()
EOF

# Update conversation endpoint with state management
echo "📝 Updating conversation endpoint with state management..."
cat > app/api/endpoints/conversation_v2.py << 'EOF'
from fastapi import APIRouter, HTTPException, Depends
from typing import Dict, Any, List
from datetime import datetime
import logging

from app.schemas.conversation import (
    ConversationRequest,
    ConversationResponse,
    SessionInfo,
    StateInfo
)
from app.services.session_manager import session_manager
from app.services.context_manager import context_manager
from app.services.intent_classifier import intent_classifier
from app.services.state_tracker import state_tracker, ConversationState
from app.services.conversation import conversation_service

router = APIRouter()
logger = logging.getLogger(__name__)


@router.post("/chat", response_model=ConversationResponse)
async def enhanced_chat(request: ConversationRequest) -> ConversationResponse:
    """Enhanced chat endpoint with state management"""
    try:
        # Classify intent
        intent, confidence, metadata = await intent_classifier.classify(
            request.message,
            request.session_id
        )
        
        # Get current state
        current_state = await state_tracker.get_conversation_state(request.session_id)
        
        # Determine next action
        next_action = await state_tracker.get_next_action(
            request.session_id,
            intent.value
        )
        
        # Process message with context
        response_data = await conversation_service.process_message(
            message=request.message,
            session_id=request.session_id,
            context={
                "intent": intent.value,
                "confidence": confidence,
                "metadata": metadata,
                "current_state": current_state.value,
                "next_action": next_action
            }
        )
        
        # Update state if needed
        if next_action["next_state"] != current_state:
            await state_tracker.transition_conversation_state(
                request.session_id,
                next_action["next_state"]
            )
        
        # Add state info to response
        response_data["state_info"] = {
            "conversation_state": next_action["next_state"].value,
            "intent": intent.value,
            "confidence": confidence,
            "action": next_action["action"]
        }
        
        return ConversationResponse(**response_data)
        
    except Exception as e:
        logger.error(f"Chat error: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/sessions/{session_id}/info", response_model=SessionInfo)
async def get_session_info(session_id: str) -> SessionInfo:
    """Get detailed session information"""
    session = await session_manager.get_session(session_id)
    if not session:
        raise HTTPException(status_code=404, detail="Session not found")
    
    stats = await session_manager.get_session_stats(session_id)
    state_summary = await state_tracker.get_state_summary(session_id)
    
    return SessionInfo(
        session_id=session_id,
        created_at=session.created_at,
        last_activity=session.last_activity,
        message_count=len(session.messages),
        **state_summary
    )


@router.get("/sessions/{session_id}/context")
async def get_session_context(session_id: str) -> Dict[str, Any]:
    """Get session context"""
    context = await context_manager.get_relevant_context(session_id)
    if not context:
        raise HTTPException(status_code=404, detail="Session not found")
    
    return context


@router.post("/sessions/{session_id}/state")
async def update_session_state(
    session_id: str,
    state_update: StateInfo
) -> Dict[str, str]:
    """Manually update session state (for testing/admin)"""
    if state_update.conversation_state:
        success = await state_tracker.transition_conversation_state(
            session_id,
            ConversationState(state_update.conversation_state)
        )
        if not success:
            raise HTTPException(status_code=400, detail="Invalid state transition")
    
    return {"status": "updated"}


@router.get("/sessions/{session_id}/history")
async def get_conversation_history(
    session_id: str,
    limit: int = 50
) -> List[Dict[str, Any]]:
    """Get conversation history"""
    messages = await session_manager.get_conversation_history(session_id, limit)
    
    return [
        {
            "id": msg.id,
            "role": msg.role,
            "content": msg.content,
            "timestamp": msg.timestamp.isoformat(),
            "metadata": msg.metadata
        }
        for msg in messages
    ]


@router.delete("/sessions/{session_id}")
async def delete_session(session_id: str) -> Dict[str, str]:
    """Delete a session and all its data"""
    success = await session_manager.delete_session(session_id)
    if not success:
        raise HTTPException(status_code=404, detail="Session not found")
    
    return {"status": "deleted", "session_id": session_id}


@router.get("/sessions/active")
async def list_active_sessions() -> List[str]:
    """List all active session IDs"""
    return await session_manager.list_active_sessions()
EOF

# Create schemas for conversation
echo "📝 Creating conversation schemas..."
cat > app/schemas/conversation.py << 'EOF'
from pydantic import BaseModel, Field
from typing import Dict, Any, Optional, List
from datetime import datetime
import uuid


class ConversationRequest(BaseModel):
    """Enhanced conversation request"""
    message: str = Field(..., description="User message")
    session_id: str = Field(default_factory=lambda: str(uuid.uuid4()))
    context: Dict[str, Any] = Field(default_factory=dict)
    stream: bool = Field(default=False, description="Enable streaming response")


class ConversationResponse(BaseModel):
    """Enhanced conversation response"""
    response: str
    session_id: str
    timestamp: datetime
    intent: Optional[str] = None
    state_info: Optional[Dict[str, Any]] = None
    metadata: Dict[str, Any] = Field(default_factory=dict)


class SessionInfo(BaseModel):
    """Session information"""
    session_id: str
    created_at: datetime
    last_activity: datetime
    message_count: int
    conversation_state: str
    project_state: str
    has_active_project: bool
    session_duration: Optional[float] = None


class StateInfo(BaseModel):
    """State update information"""
    conversation_state: Optional[str] = None
    project_state: Optional[str] = None
    metadata: Dict[str, Any] = Field(default_factory=dict)


class MessageHistory(BaseModel):
    """Message history item"""
    id: str
    role: str
    content: str
    timestamp: datetime
    metadata: Dict[str, Any] = Field(default_factory=dict)
EOF

# Create conversation models
echo "📝 Creating conversation models..."
cat > app/models/conversation.py << 'EOF'
from pydantic import BaseModel, Field
from typing import List, Dict, Any, Optional
from datetime import datetime
import uuid


class Message(BaseModel):
    """Conversation message model"""
    id: str = Field(default_factory=lambda: str(uuid.uuid4()))
    role: str = Field(..., description="Role: user or assistant")
    content: str = Field(..., description="Message content")
    timestamp: datetime = Field(default_factory=datetime.utcnow)
    metadata: Dict[str, Any] = Field(default_factory=dict)


class ConversationSession(BaseModel):
    """Conversation session model"""
    session_id: str = Field(..., description="Unique session identifier")
    messages: List[Message] = Field(default_factory=list)
    context: Dict[str, Any] = Field(default_factory=dict)
    created_at: datetime = Field(default_factory=datetime.utcnow)
    last_activity: datetime = Field(default_factory=datetime.utcnow)
    metadata: Dict[str, Any] = Field(default_factory=dict)
    
    class Config:
        json_encoders = {
            datetime: lambda v: v.isoformat()
        }


class ProjectContext(BaseModel):
    """Project context within a conversation"""
    project_id: Optional[str] = None
    project_type: Optional[str] = None
    current_state: Dict[str, Any] = Field(default_factory=dict)
    requirements: Dict[str, Any] = Field(default_factory=dict)
    modifications: List[Dict[str, Any]] = Field(default_factory=list)
EOF

# Create tests for conversation management
echo "🧪 Creating tests for conversation management..."
cat > tests/test_conversation_management.py << 'EOF'
import pytest
from datetime import datetime
from app.services.session_manager import SessionManager
from app.services.context_manager import ContextManager
from app.services.intent_classifier import IntentClassifier, Intent
from app.services.state_tracker import StateTracker, ConversationState


@pytest.fixture
async def session_manager():
    manager = SessionManager()
    # Mock Redis client
    manager.redis_client = None
    return manager


@pytest.fixture
async def context_manager():
    return ContextManager()


@pytest.fixture
async def intent_classifier():
    return IntentClassifier()


@pytest.fixture
async def state_tracker():
    return StateTracker()


@pytest.mark.asyncio
async def test_intent_classification(intent_classifier):
    """Test intent classification"""
    test_cases = [
        ("Create a new web application", Intent.PROJECT_CREATE),
        ("Help me build an API", Intent.PROJECT_CREATE),
        ("Change the database to PostgreSQL", Intent.PROJECT_MODIFY),
        ("Show me the project status", Intent.PROJECT_INFO),
        ("How do I create a project?", Intent.HELP),
        ("Hello!", Intent.GREETING)
    ]
    
    for message, expected_intent in test_cases:
        intent, confidence, metadata = await intent_classifier.classify(
            message, "test-session"
        )
        assert intent == expected_intent
        assert 0 <= confidence <= 1


@pytest.mark.asyncio
async def test_state_transitions(state_tracker):
    """Test conversation state transitions"""
    session_id = "test-session"
    
    # Initial state
    state = await state_tracker.get_conversation_state(session_id)
    assert state == ConversationState.INITIAL
    
    # Valid transition
    success = await state_tracker.transition_conversation_state(
        session_id,
        ConversationState.GATHERING_REQUIREMENTS
    )
    assert success
    
    # Invalid transition (directly to COMPLETED)
    success = await state_tracker.transition_conversation_state(
        session_id,
        ConversationState.COMPLETED
    )
    assert not success


@pytest.mark.asyncio
async def test_context_extraction(context_manager):
    """Test entity extraction from text"""
    text = "Create a React web app with Node.js backend and PostgreSQL database"
    
    entities = await context_manager.extract_entities(text)
    
    assert "react" in entities["technologies"]
    assert "node" in entities["technologies"]
    assert "postgresql" in entities["technologies"]
    assert "web app" in entities["project_types"]
EOF

echo "✅ Step 4 completed! Conversation management system implemented."
echo "📊 Created:"
echo "   - Session manager with Redis persistence"
echo "   - Context manager for conversation context"
echo "   - Intent classifier for user intent detection"
echo "   - State tracker for conversation and project states"
echo "   - Enhanced conversation endpoints"
echo "   - Schemas for API models"
echo "   - Tests for conversation management"

echo ""
echo "Next step: Run setup_step5_mcp_integration.sh"