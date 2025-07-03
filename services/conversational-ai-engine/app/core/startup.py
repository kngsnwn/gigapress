
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
