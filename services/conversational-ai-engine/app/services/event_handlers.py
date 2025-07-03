import logging
from typing import Dict, Any
from datetime import datetime

from app.services.session_manager import session_manager
from app.services.context_manager import context_manager
from app.services.state_tracker import state_tracker, ProjectState
from app.services.websocket_manager import websocket_manager

logger = logging.getLogger(__name__)


class EventHandlers:
    """Event handlers for various system events"""
    
    @staticmethod
    async def handle_project_update(event_data: Dict[str, Any]):
        """Handle project update events"""
        try:
            project_id = event_data.get("data", {}).get("projectId")
            update_type = event_data.get("data", {}).get("updateType")
            session_id = event_data.get("data", {}).get("sessionId")
            
            if not all([project_id, update_type, session_id]):
                logger.warning("Incomplete project update event")
                return
            
            logger.info(f"Handling project update: {project_id} - {update_type}")
            
            # Update session context
            await context_manager.update_project_state(
                session_id,
                {
                    "last_update": datetime.utcnow().isoformat(),
                    "update_type": update_type,
                    "update_details": event_data.get("data", {})
                }
            )
            
            # Send WebSocket notification
            await websocket_manager.send_to_session(
                session_id,
                {
                    "type": "project_update",
                    "project_id": project_id,
                    "update_type": update_type,
                    "timestamp": datetime.utcnow().isoformat()
                }
            )
            
        except Exception as e:
            logger.error(f"Failed to handle project update: {str(e)}")
    
    @staticmethod
    async def handle_generation_complete(event_data: Dict[str, Any]):
        """Handle generation completion events"""
        try:
            project_id = event_data.get("data", {}).get("projectId")
            session_id = event_data.get("data", {}).get("sessionId")
            status = event_data.get("data", {}).get("status")
            
            logger.info(f"Generation complete for project: {project_id}")
            
            # Update project state
            if session_id:
                if status == "success":
                    await state_tracker.update_project_state(
                        session_id,
                        ProjectState.COMPLETED
                    )
                else:
                    await state_tracker.update_project_state(
                        session_id,
                        ProjectState.FAILED,
                        {"error": event_data.get("data", {}).get("error")}
                    )
                
                # Notify via WebSocket
                await websocket_manager.send_to_session(
                    session_id,
                    {
                        "type": "generation_complete",
                        "project_id": project_id,
                        "status": status,
                        "details": event_data.get("data", {})
                    }
                )
            
        except Exception as e:
            logger.error(f"Failed to handle generation complete: {str(e)}")
    
    @staticmethod
    async def handle_validation_result(event_data: Dict[str, Any]):
        """Handle validation result events"""
        try:
            project_id = event_data.get("data", {}).get("projectId")
            validation_type = event_data.get("data", {}).get("validationType")
            results = event_data.get("data", {}).get("results", {})
            session_id = event_data.get("data", {}).get("sessionId")
            
            logger.info(
                f"Validation result for project {project_id}: "
                f"{validation_type} - {results.get('status')}"
            )
            
            if session_id:
                # Update context with validation results
                await context_manager.update_project_state(
                    session_id,
                    {
                        "last_validation": {
                            "type": validation_type,
                            "status": results.get("status"),
                            "timestamp": datetime.utcnow().isoformat(),
                            "issues": results.get("issues", [])
                        }
                    }
                )
                
                # Notify if there are issues
                if results.get("issues"):
                    await websocket_manager.send_to_session(
                        session_id,
                        {
                            "type": "validation_issues",
                            "project_id": project_id,
                            "issues": results["issues"]
                        }
                    )
            
        except Exception as e:
            logger.error(f"Failed to handle validation result: {str(e)}")
    
    @staticmethod
    async def handle_error_event(event_data: Dict[str, Any]):
        """Handle error events"""
        try:
            error_type = event_data.get("data", {}).get("errorType")
            error_message = event_data.get("data", {}).get("message")
            session_id = event_data.get("data", {}).get("sessionId")
            
            logger.error(f"Error event: {error_type} - {error_message}")
            
            if session_id:
                # Add error to session
                await session_manager.add_message(
                    session_id,
                    "system",
                    f"An error occurred: {error_message}",
                    {"error_type": error_type}
                )
                
                # Notify via WebSocket
                await websocket_manager.send_to_session(
                    session_id,
                    {
                        "type": "error",
                        "error_type": error_type,
                        "message": error_message
                    }
                )
            
        except Exception as e:
            logger.error(f"Failed to handle error event: {str(e)}")
    
    @staticmethod
    async def handle_progress_update(event_data: Dict[str, Any]):
        """Handle progress update events"""
        try:
            task = event_data.get("data", {}).get("task")
            progress = event_data.get("data", {}).get("progress", 0)
            session_id = event_data.get("data", {}).get("sessionId")
            
            if session_id:
                # Send progress update via WebSocket
                await websocket_manager.send_to_session(
                    session_id,
                    {
                        "type": "progress",
                        "task": task,
                        "progress": progress,
                        "timestamp": datetime.utcnow().isoformat()
                    }
                )
            
        except Exception as e:
            logger.error(f"Failed to handle progress update: {str(e)}")


# Create handler registry
def register_event_handlers():
    """Register all event handlers"""
    from app.services.kafka_consumer import kafka_consumer
    
    # Project events
    kafka_consumer.register_handler("project.updated", EventHandlers.handle_project_update)
    kafka_consumer.register_handler("project.generation.complete", EventHandlers.handle_generation_complete)
    
    # Validation events
    kafka_consumer.register_handler("validation.complete", EventHandlers.handle_validation_result)
    
    # Error events
    kafka_consumer.register_handler("error", EventHandlers.handle_error_event)
    
    # Progress events
    kafka_consumer.register_handler("progress.update", EventHandlers.handle_progress_update)
    
    # Wildcard handler for logging
    kafka_consumer.register_handler("*", lambda e: logger.debug(f"Event received: {e.get('type')}"))
    
    logger.info("Event handlers registered")
