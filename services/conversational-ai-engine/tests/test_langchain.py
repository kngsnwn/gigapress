import pytest
from unittest.mock import Mock, patch
from app.core.langchain_config import LangChainService
from app.services.chains import ChainService, ProjectRequirements


@pytest.fixture
def mock_llm():
    """Mock LLM for testing"""
    mock = Mock()
    mock.arun = Mock(return_value="Test response")
    return mock


@pytest.fixture
def langchain_service():
    """LangChain service fixture"""
    service = LangChainService()
    return service


@pytest.fixture
def chain_service():
    """Chain service fixture"""
    service = ChainService()
    return service


@pytest.mark.asyncio
async def test_langchain_initialization(langchain_service):
    """Test LangChain service initialization"""
    with patch('redis.Redis'):
        await langchain_service.initialize()
        assert langchain_service.llm is not None
        assert langchain_service.redis_client is not None


@pytest.mark.asyncio
async def test_memory_management(langchain_service):
    """Test conversation memory management"""
    session_id = "test-session"
    
    # Get memory
    memory1 = langchain_service.get_memory(session_id)
    assert memory1 is not None
    
    # Get same memory again
    memory2 = langchain_service.get_memory(session_id)
    assert memory1 is memory2
    
    # Clear memory
    langchain_service.clear_memory(session_id)
    memory3 = langchain_service.get_memory(session_id)
    assert memory3 is not memory1


@pytest.mark.asyncio
async def test_project_analysis(chain_service, mock_llm):
    """Test project requirement analysis"""
    chain_service.llm = mock_llm
    
    mock_llm.arun.return_value = """{
        "project_type": "web_app",
        "features": ["user_auth", "dashboard"],
        "technologies": {"frontend": "react", "backend": "nodejs"},
        "constraints": {"timeline": "2 weeks"}
    }"""
    
    result = await chain_service.analyze_project_request(
        "Create a web app with user authentication",
        {}
    )
    
    assert isinstance(result, dict)
    mock_llm.arun.assert_called_once()
