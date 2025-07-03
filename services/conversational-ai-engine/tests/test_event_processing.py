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
