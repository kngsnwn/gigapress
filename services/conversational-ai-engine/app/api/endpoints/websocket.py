from fastapi import APIRouter, WebSocket, WebSocketDisconnect, Depends
from typing import Dict, Any
import json
import logging

from app.services.websocket_manager import websocket_manager
from app.services.event_driven_conversation import event_driven_conversation
from app.services.session_manager import session_manager

router = APIRouter()
logger = logging.getLogger(__name__)


@router.websocket("/ws/{session_id}")
async def websocket_endpoint(websocket: WebSocket, session_id: str):
    """WebSocket endpoint for real-time communication"""
    await websocket_manager.connect(websocket, session_id)
    
    try:
        while True:
            # Receive message from client
            data = await websocket.receive_text()
            
            try:
                message_data = json.loads(data)
                message_type = message_data.get("type", "chat")
                
                if message_type == "chat":
                    # Process chat message
                    response = await event_driven_conversation.process_message_with_events(
                        message=message_data.get("message", ""),
                        session_id=session_id,
                        context=message_data.get("context", {})
                    )
                    
                    # Send response
                    await websocket_manager.send_to_websocket(
                        websocket,
                        {
                            "type": "chat_response",
                            "data": response
                        }
                    )
                
                elif message_type == "ping":
                    # Respond to ping
                    await websocket_manager.send_to_websocket(
                        websocket,
                        {"type": "pong"}
                    )
                
                elif message_type == "get_status":
                    # Get session status
                    stats = await session_manager.get_session_stats(session_id)
                    await websocket_manager.send_to_websocket(
                        websocket,
                        {
                            "type": "status",
                            "data": stats
                        }
                    )
                
            except json.JSONDecodeError:
                await websocket_manager.send_to_websocket(
                    websocket,
                    {
                        "type": "error",
                        "message": "Invalid JSON format"
                    }
                )
            except Exception as e:
                logger.error(f"WebSocket message processing error: {str(e)}")
                await websocket_manager.send_to_websocket(
                    websocket,
                    {
                        "type": "error",
                        "message": str(e)
                    }
                )
                
    except WebSocketDisconnect:
        websocket_manager.disconnect(websocket, session_id)
        logger.info(f"Client disconnected from session: {session_id}")
    except Exception as e:
        logger.error(f"WebSocket error: {str(e)}")
        websocket_manager.disconnect(websocket, session_id)


@router.get("/ws/active-sessions")
async def get_active_websocket_sessions() -> Dict[str, Any]:
    """Get information about active WebSocket sessions"""
    return {
        "active_sessions": websocket_manager.get_active_sessions(),
        "total_connections": websocket_manager.get_connection_count()
    }
