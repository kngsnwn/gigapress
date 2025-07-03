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
