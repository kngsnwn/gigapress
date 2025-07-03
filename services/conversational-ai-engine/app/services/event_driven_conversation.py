from typing import Dict, Any, Optional
import logging
from datetime import datetime

from app.services.conversation import conversation_service
from app.services.event_producer import event_producer
from app.services.kafka_consumer import kafka_consumer
from app.services.websocket_manager import websocket_manager

logger = logging.getLogger(__name__)


class EventDrivenConversationService:
    """Event-driven extensions for conversation service"""
    
    async def process_message_with_events(
        self,
        message: str,
        session_id: str,
        context: Optional[Dict[str, Any]] = None
    ) -> Dict[str, Any]:
        """Process message and emit events"""
        try:
            # Send conversation started event
            await event_producer.send_conversation_event(
                session_id,
                "message.received",
                {
                    "message": message[:100],  # First 100 chars
                    "has_context": bool(context)
                }
            )
            
            # Process message
            result = await conversation_service.process_message(
                message, session_id, context
            )
            
            # Send response generated event
            await event_producer.send_conversation_event(
                session_id,
                "response.generated",
                {
                    "response_length": len(result.get("response", "")),
                    "intent": result.get("intent", {}).get("intent")
                }
            )
            
            return result
            
        except Exception as e:
            # Send error event
            await event_producer.send_error_event(
                "conversation_error",
                str(e),
                {"session_id": session_id},
                session_id
            )
            raise
    
    async def handle_project_creation_with_events(
        self,
        session_id: str,
        requirements: Dict[str, Any]
    ) -> Dict[str, Any]:
        """Handle project creation with event notifications"""
        try:
            # Send project creation started event
            await event_producer.send_project_event(
                "pending",
                "creation.started",
                {
                    "requirements": requirements,
                    "project_type": requirements.get("project_type")
                },
                session_id
            )
            
            # Track progress
            progress_steps = [
                ("Analyzing requirements", 0.1),
                ("Setting up project structure", 0.3),
                ("Generating backend", 0.5),
                ("Generating frontend", 0.7),
                ("Setting up infrastructure", 0.9),
                ("Finalizing project", 1.0)
            ]
            
            # Simulate progress updates
            for step, progress in progress_steps:
                await event_producer.send_progress_event(
                    step,
                    progress,
                    session_id
                )
                
                # Also send via WebSocket for real-time updates
                await websocket_manager.send_to_session(
                    session_id,
                    {
                        "type": "progress",
                        "task": step,
                        "progress": progress
                    }
                )
            
            # Create project (actual implementation would call MCP)
            from app.services.mcp_integration import mcp_integration_service
            result = await mcp_integration_service.create_project(
                session_id,
                requirements
            )
            
            # Send completion event
            await event_producer.send_project_event(
                result.get("project_id", "unknown"),
                "creation.completed",
                {
                    "status": "success",
                    "project_id": result.get("project_id")
                },
                session_id
            )
            
            return result
            
        except Exception as e:
            # Send failure event
            await event_producer.send_project_event(
                "unknown",
                "creation.failed",
                {
                    "error": str(e),
                    "requirements": requirements
                },
                session_id
            )
            raise
    
    async def setup_event_handlers(self):
        """Setup handlers for conversation-related events"""
        
        async def handle_external_update(event_data: Dict[str, Any]):
            """Handle updates from external services"""
            session_id = event_data.get("data", {}).get("sessionId")
            if session_id:
                # Notify the user about the update
                await websocket_manager.send_to_session(
                    session_id,
                    {
                        "type": "external_update",
                        "data": event_data.get("data", {})
                    }
                )
        
        # Register handlers
        kafka_consumer.register_handler("external.update", handle_external_update)
        
        logger.info("Event-driven conversation handlers registered")


# Singleton instance
event_driven_conversation = EventDrivenConversationService()
