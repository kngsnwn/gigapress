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
