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
