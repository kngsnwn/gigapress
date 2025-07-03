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
