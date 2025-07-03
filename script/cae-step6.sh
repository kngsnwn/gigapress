#!/bin/bash

# Step 6: Event Processing System
echo "🚀 Setting up Conversational AI Engine - Step 6: Event Processing System"

cd conversational-ai-engine

# Create Kafka consumer
echo "📝 Creating Kafka consumer..."
cat > app/services/kafka_consumer.py << 'EOF'
import asyncio
import json
import logging
from typing import Dict, Any, Callable, Optional, List
from datetime import datetime
from aiokafka import AIOKafkaConsumer
from aiokafka.errors import KafkaError
from config.settings import settings

logger = logging.getLogger(__name__)


class KafkaEventConsumer:
    """Kafka consumer for processing events"""
    
    def __init__(self):
        self.consumer: Optional[AIOKafkaConsumer] = None
        self.handlers: Dict[str, List[Callable]] = {}
        self.running = False
        self.tasks = []
        
    async def initialize(self):
        """Initialize Kafka consumer"""
        try:
            self.consumer = AIOKafkaConsumer(
                *settings.kafka_topics,
                bootstrap_servers=settings.kafka_bootstrap_servers,
                group_id=settings.kafka_consumer_group,
                auto_offset_reset="latest",
                enable_auto_commit=True,
                value_deserializer=lambda v: json.loads(v.decode('utf-8')),
                key_deserializer=lambda k: k.decode('utf-8') if k else None
            )
            
            await self.consumer.start()
            logger.info(f"Kafka consumer initialized for topics: {settings.kafka_topics}")
            
        except Exception as e:
            logger.error(f"Failed to initialize Kafka consumer: {str(e)}")
            raise
    
    async def shutdown(self):
        """Shutdown Kafka consumer"""
        self.running = False
        
        # Cancel all tasks
        for task in self.tasks:
            task.cancel()
        
        if self.consumer:
            await self.consumer.stop()
            logger.info("Kafka consumer stopped")
    
    def register_handler(self, event_type: str, handler: Callable):
        """Register an event handler"""
        if event_type not in self.handlers:
            self.handlers[event_type] = []
        
        self.handlers[event_type].append(handler)
        logger.info(f"Registered handler for event type: {event_type}")
    
    async def start_consuming(self):
        """Start consuming events"""
        if self.running:
            logger.warning("Consumer already running")
            return
        
        self.running = True
        logger.info("Starting event consumption...")
        
        try:
            async for msg in self.consumer:
                if not self.running:
                    break
                
                # Process message in background
                task = asyncio.create_task(self._process_message(msg))
                self.tasks.append(task)
                
                # Clean up completed tasks
                self.tasks = [t for t in self.tasks if not t.done()]
                
        except Exception as e:
            logger.error(f"Error in event consumption: {str(e)}")
            raise
        finally:
            self.running = False
    
    async def _process_message(self, message):
        """Process a single message"""
        try:
            event_data = message.value
            event_type = event_data.get("type", "unknown")
            
            logger.info(
                f"Processing event: {event_type} from topic: {message.topic}, "
                f"partition: {message.partition}, offset: {message.offset}"
            )
            
            # Get handlers for this event type
            handlers = self.handlers.get(event_type, [])
            handlers.extend(self.handlers.get("*", []))  # Wildcard handlers
            
            if not handlers:
                logger.warning(f"No handlers registered for event type: {event_type}")
                return
            
            # Execute all handlers
            for handler in handlers:
                try:
                    if asyncio.iscoroutinefunction(handler):
                        await handler(event_data)
                    else:
                        handler(event_data)
                except Exception as e:
                    logger.error(f"Handler error for {event_type}: {str(e)}")
                    
        except Exception as e:
            logger.error(f"Failed to process message: {str(e)}")
    
    async def send_test_event(self, event_type: str, data: Dict[str, Any]):
        """Send a test event (for development)"""
        from aiokafka import AIOKafkaProducer
        
        producer = AIOKafkaProducer(
            bootstrap_servers=settings.kafka_bootstrap_servers,
            value_serializer=lambda v: json.dumps(v).encode('utf-8')
        )
        
        await producer.start()
        
        try:
            event = {
                "type": event_type,
                "timestamp": datetime.utcnow().isoformat(),
                "data": data
            }
            
            await producer.send_and_wait(
                settings.kafka_topics[0],
                event
            )
            
            logger.info(f"Sent test event: {event_type}")
            
        finally:
            await producer.stop()


# Singleton instance
kafka_consumer = KafkaEventConsumer()
EOF

# Create event handlers
echo "📝 Creating event handlers..."
cat > app/services/event_handlers.py << 'EOF'
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
EOF

# Create WebSocket manager
echo "📝 Creating WebSocket manager..."
cat > app/services/websocket_manager.py << 'EOF'
from typing import Dict, Set, Any
from fastapi import WebSocket
import json
import logging
from datetime import datetime

logger = logging.getLogger(__name__)


class WebSocketManager:
    """Manage WebSocket connections"""
    
    def __init__(self):
        # Map session_id to set of connections
        self.connections: Dict[str, Set[WebSocket]] = {}
        
    async def connect(self, websocket: WebSocket, session_id: str):
        """Accept a new WebSocket connection"""
        await websocket.accept()
        
        if session_id not in self.connections:
            self.connections[session_id] = set()
        
        self.connections[session_id].add(websocket)
        logger.info(f"WebSocket connected for session: {session_id}")
        
        # Send welcome message
        await self.send_to_websocket(
            websocket,
            {
                "type": "connected",
                "session_id": session_id,
                "timestamp": datetime.utcnow().isoformat()
            }
        )
    
    def disconnect(self, websocket: WebSocket, session_id: str):
        """Remove a WebSocket connection"""
        if session_id in self.connections:
            self.connections[session_id].discard(websocket)
            
            # Remove empty sets
            if not self.connections[session_id]:
                del self.connections[session_id]
        
        logger.info(f"WebSocket disconnected for session: {session_id}")
    
    async def send_to_session(self, session_id: str, data: Dict[str, Any]):
        """Send data to all connections for a session"""
        if session_id not in self.connections:
            return
        
        disconnected = set()
        
        for websocket in self.connections[session_id]:
            try:
                await self.send_to_websocket(websocket, data)
            except Exception as e:
                logger.error(f"Failed to send to websocket: {str(e)}")
                disconnected.add(websocket)
        
        # Remove disconnected websockets
        for ws in disconnected:
            self.connections[session_id].discard(ws)
    
    async def send_to_websocket(self, websocket: WebSocket, data: Dict[str, Any]):
        """Send data to a specific websocket"""
        await websocket.send_json(data)
    
    async def broadcast(self, data: Dict[str, Any]):
        """Broadcast to all connected clients"""
        all_websockets = set()
        for session_sockets in self.connections.values():
            all_websockets.update(session_sockets)
        
        disconnected = set()
        
        for websocket in all_websockets:
            try:
                await self.send_to_websocket(websocket, data)
            except Exception:
                disconnected.add(websocket)
        
        # Clean up disconnected sockets
        for session_id, sockets in self.connections.items():
            for ws in disconnected:
                sockets.discard(ws)
    
    def get_active_sessions(self) -> List[str]:
        """Get list of sessions with active WebSocket connections"""
        return list(self.connections.keys())
    
    def get_connection_count(self) -> int:
        """Get total number of active connections"""
        return sum(len(sockets) for sockets in self.connections.values())


# Singleton instance
websocket_manager = WebSocketManager()
EOF

# Create event producer
echo "📝 Creating event producer..."
cat > app/services/event_producer.py << 'EOF'
import json
import logging
from typing import Dict, Any, Optional
from datetime import datetime
from aiokafka import AIOKafkaProducer
from config.settings import settings

logger = logging.getLogger(__name__)


class EventProducer:
    """Produce events to Kafka"""
    
    def __init__(self):
        self.producer: Optional[AIOKafkaProducer] = None
        
    async def initialize(self):
        """Initialize Kafka producer"""
        try:
            self.producer = AIOKafkaProducer(
                bootstrap_servers=settings.kafka_bootstrap_servers,
                value_serializer=lambda v: json.dumps(v).encode('utf-8'),
                key_serializer=lambda k: k.encode('utf-8') if k else None
            )
            
            await self.producer.start()
            logger.info("Event producer initialized")
            
        except Exception as e:
            logger.error(f"Failed to initialize event producer: {str(e)}")
            raise
    
    async def shutdown(self):
        """Shutdown producer"""
        if self.producer:
            await self.producer.stop()
            logger.info("Event producer stopped")
    
    async def send_event(
        self,
        event_type: str,
        data: Dict[str, Any],
        key: Optional[str] = None,
        session_id: Optional[str] = None
    ):
        """Send an event to Kafka"""
        try:
            event = {
                "type": event_type,
                "timestamp": datetime.utcnow().isoformat(),
                "source": "conversational-ai-engine",
                "data": data
            }
            
            if session_id:
                event["data"]["sessionId"] = session_id
            
            # Determine topic based on event type
            topic = self._get_topic_for_event(event_type)
            
            await self.producer.send_and_wait(
                topic,
                value=event,
                key=key
            )
            
            logger.info(f"Sent event: {event_type} to topic: {topic}")
            
        except Exception as e:
            logger.error(f"Failed to send event {event_type}: {str(e)}")
            raise
    
    def _get_topic_for_event(self, event_type: str) -> str:
        """Determine the appropriate topic for an event type"""
        # Map event types to topics
        topic_mapping = {
            "project": "project-updates",
            "conversation": "conversation-events",
            "validation": "project-updates",
            "error": "conversation-events"
        }
        
        # Get the prefix of the event type
        prefix = event_type.split('.')[0]
        
        return topic_mapping.get(prefix, settings.kafka_topics[0])
    
    # Convenience methods for common events
    
    async def send_conversation_event(
        self,
        session_id: str,
        event_subtype: str,
        data: Dict[str, Any]
    ):
        """Send a conversation-related event"""
        await self.send_event(
            f"conversation.{event_subtype}",
            data,
            key=session_id,
            session_id=session_id
        )
    
    async def send_project_event(
        self,
        project_id: str,
        event_subtype: str,
        data: Dict[str, Any],
        session_id: Optional[str] = None
    ):
        """Send a project-related event"""
        data["projectId"] = project_id
        await self.send_event(
            f"project.{event_subtype}",
            data,
            key=project_id,
            session_id=session_id
        )
    
    async def send_error_event(
        self,
        error_type: str,
        message: str,
        details: Optional[Dict[str, Any]] = None,
        session_id: Optional[str] = None
    ):
        """Send an error event"""
        await self.send_event(
            "error",
            {
                "errorType": error_type,
                "message": message,
                "details": details or {}
            },
            session_id=session_id
        )
    
    async def send_progress_event(
        self,
        task: str,
        progress: float,
        session_id: str,
        details: Optional[Dict[str, Any]] = None
    ):
        """Send a progress update event"""
        await self.send_event(
            "progress.update",
            {
                "task": task,
                "progress": progress,
                "details": details or {}
            },
            session_id=session_id
        )


# Singleton instance
event_producer = EventProducer()
EOF

# Create event-driven conversation service
echo "📝 Creating event-driven conversation service..."
cat > app/services/event_driven_conversation.py << 'EOF'
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
EOF

# Create WebSocket endpoint
echo "📝 Creating WebSocket endpoint..."
cat > app/api/endpoints/websocket.py << 'EOF'
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
EOF

# Update startup to include event processing
echo "📝 Updating startup to include event processing..."
cat >> app/core/startup.py << 'EOF'

# Add event processing initialization
from app.services.kafka_consumer import kafka_consumer
from app.services.event_producer import event_producer
from app.services.event_handlers import register_event_handlers
from app.services.event_driven_conversation import event_driven_conversation

async def initialize_event_processing():
    """Initialize event processing components"""
    try:
        # Initialize producer
        await event_producer.initialize()
        
        # Initialize consumer
        await kafka_consumer.initialize()
        
        # Register event handlers
        register_event_handlers()
        
        # Setup conversation event handlers
        await event_driven_conversation.setup_event_handlers()
        
        # Start consuming in background
        import asyncio
        asyncio.create_task(kafka_consumer.start_consuming())
        
        logger.info("Event processing initialized")
        
    except Exception as e:
        logger.error(f"Failed to initialize event processing: {str(e)}")
        raise

# Update main initialization
async def initialize_all_services():
    """Initialize all services including events"""
    await initialize_services()
    await initialize_event_processing()

# Update shutdown
async def shutdown_all_services():
    """Shutdown all services including events"""
    await shutdown_services()
    await event_producer.shutdown()
    await kafka_consumer.shutdown()
EOF

# Create tests for event processing
echo "🧪 Creating tests for event processing..."
cat > tests/test_event_processing.py << 'EOF'
import pytest
import asyncio
from unittest.mock import Mock, AsyncMock, patch
from app.services.kafka_consumer import KafkaEventConsumer
from app.services.event_producer import EventProducer
from app.services.websocket_manager import WebSocketManager
from app.services.event_handlers import EventHandlers


@pytest.fixture
def kafka_consumer():
    """Kafka consumer fixture"""
    consumer = KafkaEventConsumer()
    consumer.consumer = Mock()
    return consumer


@pytest.fixture
def event_producer():
    """Event producer fixture"""
    producer = EventProducer()
    producer.producer = Mock()
    return producer


@pytest.fixture
def websocket_manager():
    """WebSocket manager fixture"""
    return WebSocketManager()


@pytest.mark.asyncio
async def test_event_handler_registration(kafka_consumer):
    """Test event handler registration"""
    handler = Mock()
    kafka_consumer.register_handler("test.event", handler)
    
    assert "test.event" in kafka_consumer.handlers
    assert handler in kafka_consumer.handlers["test.event"]


@pytest.mark.asyncio
async def test_event_producer_send(event_producer):
    """Test event sending"""
    event_producer.producer.send_and_wait = AsyncMock()
    
    await event_producer.send_event(
        "test.event",
        {"data": "test"},
        session_id="test-session"
    )
    
    event_producer.producer.send_and_wait.assert_called_once()
    call_args = event_producer.producer.send_and_wait.call_args
    assert call_args[1]["value"]["type"] == "test.event"
    assert call_args[1]["value"]["data"]["sessionId"] == "test-session"


@pytest.mark.asyncio
async def test_websocket_manager_connect():
    """Test WebSocket connection management"""
    manager = WebSocketManager()
    websocket = Mock()
    websocket.accept = AsyncMock()
    websocket.send_json = AsyncMock()
    
    await manager.connect(websocket, "test-session")
    
    assert "test-session" in manager.connections
    assert websocket in manager.connections["test-session"]
    websocket.accept.assert_called_once()


@pytest.mark.asyncio
async def test_project_update_handler():
    """Test project update event handler"""
    with patch('app.services.context_manager.context_manager') as mock_context:
        with patch('app.services.websocket_manager.websocket_manager') as mock_ws:
            mock_context.update_project_state = AsyncMock()
            mock_ws.send_to_session = AsyncMock()
            
            event_data = {
                "data": {
                    "projectId": "test-123",
                    "updateType": "backend_generated",
                    "sessionId": "test-session"
                }
            }
            
            await EventHandlers.handle_project_update(event_data)
            
            mock_context.update_project_state.assert_called_once()
            mock_ws.send_to_session.assert_called_once()


@pytest.mark.asyncio
async def test_event_driven_conversation():
    """Test event-driven conversation processing"""
    from app.services.event_driven_conversation import event_driven_conversation
    
    with patch('app.services.conversation.conversation_service') as mock_conv:
        with patch('app.services.event_producer.event_producer') as mock_producer:
            mock_conv.process_message = AsyncMock(return_value={
                "response": "Test response",
                "intent": {"intent": "test"}
            })
            mock_producer.send_conversation_event = AsyncMock()
            
            result = await event_driven_conversation.process_message_with_events(
                "Test message",
                "test-session"
            )
            
            assert result["response"] == "Test response"
            assert mock_producer.send_conversation_event.call_count == 2
EOF

# Update API router to include WebSocket
echo "📝 Updating API router to include WebSocket..."
cat >> app/api/router.py << 'EOF'

# Add WebSocket endpoints
from app.api.endpoints import websocket
api_router.include_router(
    websocket.router,
    prefix="/realtime",
    tags=["WebSocket"]
)
EOF

# Create example WebSocket client
echo "📝 Creating example WebSocket client..."
cat > examples/websocket_client.py << 'EOF'
import asyncio
import websockets
import json
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


async def test_websocket():
    """Test WebSocket connection"""
    uri = "ws://localhost:8087/api/v1/realtime/ws/test-session-123"
    
    async with websockets.connect(uri) as websocket:
        logger.info("Connected to WebSocket")
        
        # Send a chat message
        await websocket.send(json.dumps({
            "type": "chat",
            "message": "Create a new web application with user authentication"
        }))
        
        # Listen for responses
        while True:
            try:
                response = await websocket.recv()
                data = json.loads(response)
                logger.info(f"Received: {data['type']}")
                
                if data["type"] == "chat_response":
                    logger.info(f"Response: {data['data']['response']}")
                elif data["type"] == "progress":
                    logger.info(f"Progress: {data['task']} - {data['progress']*100}%")
                elif data["type"] == "error":
                    logger.error(f"Error: {data['message']}")
                    
            except websockets.exceptions.ConnectionClosed:
                logger.info("Connection closed")
                break
            except Exception as e:
                logger.error(f"Error: {str(e)}")
                break


if __name__ == "__main__":
    asyncio.run(test_websocket())
EOF

echo "✅ Step 6 completed! Event processing system implemented."
echo "📊 Created:"
echo "   - Kafka consumer for event processing"
echo "   - Event handlers for various event types"
echo "   - WebSocket manager for real-time communication"
echo "   - Event producer for sending events"
echo "   - Event-driven conversation service"
echo "   - WebSocket API endpoint"
echo "   - Tests for event processing"
echo "   - Example WebSocket client"

echo ""
echo "Next step: Run setup_step7_api_completion.sh"