import pytest
from datetime import datetime
from app.services.session_manager import SessionManager
from app.services.context_manager import ContextManager
from app.services.intent_classifier import IntentClassifier, Intent
from app.services.state_tracker import StateTracker, ConversationState


@pytest.fixture
async def session_manager():
    manager = SessionManager()
    # Mock Redis client
    manager.redis_client = None
    return manager


@pytest.fixture
async def context_manager():
    return ContextManager()


@pytest.fixture
async def intent_classifier():
    return IntentClassifier()


@pytest.fixture
async def state_tracker():
    return StateTracker()


@pytest.mark.asyncio
async def test_intent_classification(intent_classifier):
    """Test intent classification"""
    test_cases = [
        ("Create a new web application", Intent.PROJECT_CREATE),
        ("Help me build an API", Intent.PROJECT_CREATE),
        ("Change the database to PostgreSQL", Intent.PROJECT_MODIFY),
        ("Show me the project status", Intent.PROJECT_INFO),
        ("How do I create a project?", Intent.HELP),
        ("Hello!", Intent.GREETING)
    ]
    
    for message, expected_intent in test_cases:
        intent, confidence, metadata = await intent_classifier.classify(
            message, "test-session"
        )
        assert intent == expected_intent
        assert 0 <= confidence <= 1


@pytest.mark.asyncio
async def test_state_transitions(state_tracker):
    """Test conversation state transitions"""
    session_id = "test-session"
    
    # Initial state
    state = await state_tracker.get_conversation_state(session_id)
    assert state == ConversationState.INITIAL
    
    # Valid transition
    success = await state_tracker.transition_conversation_state(
        session_id,
        ConversationState.GATHERING_REQUIREMENTS
    )
    assert success
    
    # Invalid transition (directly to COMPLETED)
    success = await state_tracker.transition_conversation_state(
        session_id,
        ConversationState.COMPLETED
    )
    assert not success


@pytest.mark.asyncio
async def test_context_extraction(context_manager):
    """Test entity extraction from text"""
    text = "Create a React web app with Node.js backend and PostgreSQL database"
    
    entities = await context_manager.extract_entities(text)
    
    assert "react" in entities["technologies"]
    assert "node" in entities["technologies"]
    assert "postgresql" in entities["technologies"]
    assert "web app" in entities["project_types"]
