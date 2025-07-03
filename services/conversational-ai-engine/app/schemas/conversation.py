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
