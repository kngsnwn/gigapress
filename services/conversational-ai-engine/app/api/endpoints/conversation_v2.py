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
