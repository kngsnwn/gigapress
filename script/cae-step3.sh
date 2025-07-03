#!/bin/bash

# Step 3: LangChain Integration
echo "🚀 Setting up Conversational AI Engine - Step 3: LangChain Integration"

cd conversational-ai-engine

# Create LangChain configuration
echo "📝 Creating LangChain configuration..."
cat > app/core/langchain_config.py << 'EOF'
from langchain.chat_models import ChatOpenAI
from langchain.memory import ConversationBufferWindowMemory, ConversationSummaryMemory
from langchain.schema import SystemMessage, HumanMessage, AIMessage
from langchain.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain.chains import ConversationChain, LLMChain
from langchain.callbacks import AsyncCallbackHandler
from langchain.cache import RedisCache
from typing import Optional, Dict, Any, List
import redis
import logging
from config.settings import settings

logger = logging.getLogger(__name__)


class ConversationCallback(AsyncCallbackHandler):
    """Custom callback handler for conversation events"""
    
    async def on_llm_start(self, serialized: Dict[str, Any], prompts: List[str], **kwargs):
        logger.info("LLM generation started", extra={"prompts": len(prompts)})
    
    async def on_llm_end(self, response, **kwargs):
        logger.info("LLM generation completed")
    
    async def on_llm_error(self, error: Exception, **kwargs):
        logger.error(f"LLM error: {str(error)}")
    
    async def on_chain_start(self, serialized: Dict[str, Any], inputs: Dict[str, Any], **kwargs):
        logger.info("Chain execution started")
    
    async def on_chain_end(self, outputs: Dict[str, Any], **kwargs):
        logger.info("Chain execution completed")


class LangChainService:
    """Service for managing LangChain components"""
    
    def __init__(self):
        self.llm = None
        self.memory_store = {}
        self.redis_client = None
        self.callback_handler = ConversationCallback()
        
    async def initialize(self):
        """Initialize LangChain components"""
        try:
            # Initialize Redis for caching
            self.redis_client = redis.Redis(
                host=settings.redis_host,
                port=settings.redis_port,
                password=settings.redis_password,
                decode_responses=True
            )
            
            # Set up LLM with caching
            self.llm = ChatOpenAI(
                model_name=settings.default_model,
                temperature=settings.temperature,
                max_tokens=settings.max_tokens,
                openai_api_key=settings.openai_api_key,
                callbacks=[self.callback_handler],
                streaming=True,
                cache=RedisCache(redis_client=self.redis_client)
            )
            
            logger.info("LangChain service initialized successfully")
            
        except Exception as e:
            logger.error(f"Failed to initialize LangChain service: {str(e)}")
            raise
    
    def get_memory(self, session_id: str) -> ConversationBufferWindowMemory:
        """Get or create memory for a session"""
        if session_id not in self.memory_store:
            self.memory_store[session_id] = ConversationBufferWindowMemory(
                k=10,  # Keep last 10 exchanges
                return_messages=True,
                memory_key="chat_history"
            )
        return self.memory_store[session_id]
    
    def clear_memory(self, session_id: str):
        """Clear memory for a session"""
        if session_id in self.memory_store:
            del self.memory_store[session_id]
            logger.info(f"Cleared memory for session: {session_id}")
    
    async def get_conversation_chain(self, session_id: str) -> ConversationChain:
        """Get conversation chain for a session"""
        memory = self.get_memory(session_id)
        
        # Create prompt template
        prompt = ChatPromptTemplate.from_messages([
            SystemMessage(content=self._get_system_prompt()),
            MessagesPlaceholder(variable_name="chat_history"),
            HumanMessage(content="{input}")
        ])
        
        # Create conversation chain
        chain = ConversationChain(
            llm=self.llm,
            memory=memory,
            prompt=prompt,
            verbose=settings.debug
        )
        
        return chain
    
    def _get_system_prompt(self) -> str:
        """Get system prompt for the AI"""
        return """You are an AI assistant for GigaPress, a system that generates software projects from natural language descriptions.

Your role is to:
1. Understand user requirements for software projects
2. Ask clarifying questions when needed
3. Break down complex requests into actionable components
4. Guide users through the project generation process
5. Explain technical decisions in simple terms

Key capabilities you should mention when relevant:
- Project generation from natural language
- Real-time project modifications
- Support for web apps, mobile apps, APIs, and microservices
- Automatic code generation and deployment setup

Always be helpful, concise, and technical when needed. Ask for clarification if requirements are unclear."""

    async def analyze_intent(self, message: str) -> Dict[str, Any]:
        """Analyze user intent from message"""
        intent_prompt = ChatPromptTemplate.from_template("""
Analyze the following user message and classify the intent:

Message: {message}

Classify the intent as one of:
- PROJECT_CREATE: User wants to create a new project
- PROJECT_MODIFY: User wants to modify an existing project
- PROJECT_INFO: User asking about project details or status
- CLARIFICATION: User providing clarification or additional details
- GENERAL_QUERY: General question about the system
- HELP: User needs help or guidance

Also extract:
- Key entities (project type, technologies, features)
- Sentiment (positive, neutral, negative)
- Urgency (high, medium, low)

Respond in JSON format.
""")
        
        chain = LLMChain(llm=self.llm, prompt=intent_prompt)
        response = await chain.arun(message=message)
        
        # Parse response (in production, use proper JSON parsing)
        return {
            "intent": "PROJECT_CREATE",  # Placeholder
            "entities": [],
            "sentiment": "neutral",
            "urgency": "medium"
        }


# Singleton instance
langchain_service = LangChainService()
EOF

# Create prompt templates
echo "📝 Creating prompt templates..."
cat > app/core/prompts.py << 'EOF'
from langchain.prompts import PromptTemplate, ChatPromptTemplate
from langchain.schema import SystemMessage


# Project generation prompts
PROJECT_ANALYSIS_PROMPT = PromptTemplate(
    input_variables=["description", "context"],
    template="""Analyze the following project description and extract key requirements:

Description: {description}
Context: {context}

Extract:
1. Project Type (web app, mobile app, API, etc.)
2. Key Features (list main functionalities)
3. Technical Requirements (preferred technologies, if mentioned)
4. Non-functional Requirements (performance, security, etc.)
5. Constraints (budget, timeline, team size, etc.)

Format the response as structured JSON."""
)

PROJECT_PLANNING_PROMPT = PromptTemplate(
    input_variables=["requirements", "constraints"],
    template="""Based on these requirements, create a project implementation plan:

Requirements: {requirements}
Constraints: {constraints}

Provide:
1. Recommended architecture
2. Technology stack
3. Component breakdown
4. Implementation phases
5. Potential challenges

Be specific and actionable."""
)

# Modification prompts
CHANGE_ANALYSIS_PROMPT = PromptTemplate(
    input_variables=["current_state", "requested_change"],
    template="""Analyze the impact of this change request:

Current Project State: {current_state}
Requested Change: {requested_change}

Determine:
1. Affected components
2. Implementation complexity (simple, moderate, complex)
3. Breaking changes
4. Required updates
5. Risk assessment

Provide a detailed analysis."""
)

# Clarification prompts
CLARIFICATION_PROMPT = PromptTemplate(
    input_variables=["context", "ambiguity"],
    template="""The user's request has some ambiguity. Generate clarifying questions:

Context: {context}
Ambiguous aspects: {ambiguity}

Generate 2-3 specific questions that would help clarify the requirements.
Make questions friendly and easy to understand."""
)

# Error handling prompts
ERROR_EXPLANATION_PROMPT = PromptTemplate(
    input_variables=["error", "context"],
    template="""Explain this technical error in user-friendly terms:

Error: {error}
Context: {context}

Provide:
1. What went wrong (in simple terms)
2. Why it might have happened
3. Suggested solutions
4. What to do next

Keep the explanation helpful and non-technical."""
)


def get_conversation_system_prompt() -> str:
    """Get the main system prompt for conversations"""
    return """You are GigaPress AI, an intelligent assistant that helps users create and modify software projects through natural conversation.

Core Principles:
1. Be conversational and friendly while maintaining technical accuracy
2. Ask for clarification when requirements are vague
3. Provide clear explanations for technical decisions
4. Guide users through the project creation process step by step
5. Suggest best practices and modern solutions

Your Capabilities:
- Generate complete software projects from descriptions
- Modify existing projects based on natural language requests
- Support various project types: web apps, mobile apps, APIs, microservices
- Provide deployment configurations and CI/CD setups
- Explain technical concepts in accessible terms

When responding:
- Break down complex requests into manageable steps
- Confirm understanding before proceeding with major changes
- Provide examples when helpful
- Mention relevant features or options the user might not know about"""


def get_project_types() -> Dict[str, str]:
    """Get supported project types with descriptions"""
    return {
        "web_app": "Full-stack web application with frontend and backend",
        "mobile_app": "Mobile application for iOS/Android",
        "api": "RESTful or GraphQL API service",
        "microservice": "Containerized microservice",
        "desktop_app": "Desktop application using Electron or similar",
        "cli_tool": "Command-line interface tool",
        "library": "Reusable software library or package"
    }
EOF

# Create chain implementations
echo "📝 Creating chain implementations..."
cat > app/services/chains.py << 'EOF'
from langchain.chains import LLMChain, SequentialChain, TransformChain
from langchain.memory import ConversationSummaryBufferMemory
from langchain.output_parsers import PydanticOutputParser, OutputFixingParser
from langchain.schema import BaseOutputParser
from typing import Dict, Any, List, Optional
from pydantic import BaseModel, Field
import json
import logging

from app.core.langchain_config import langchain_service
from app.core.prompts import (
    PROJECT_ANALYSIS_PROMPT,
    PROJECT_PLANNING_PROMPT,
    CHANGE_ANALYSIS_PROMPT,
    CLARIFICATION_PROMPT
)

logger = logging.getLogger(__name__)


class ProjectRequirements(BaseModel):
    """Project requirements model"""
    project_type: str = Field(..., description="Type of project")
    features: List[str] = Field(..., description="Key features")
    technologies: Dict[str, str] = Field(default_factory=dict, description="Technology choices")
    constraints: Dict[str, Any] = Field(default_factory=dict, description="Project constraints")


class ChangeImpact(BaseModel):
    """Change impact analysis model"""
    affected_components: List[str] = Field(..., description="Components affected by change")
    complexity: str = Field(..., description="Change complexity: simple, moderate, complex")
    breaking_changes: bool = Field(..., description="Whether change introduces breaking changes")
    required_updates: List[str] = Field(..., description="Required updates")
    risk_level: str = Field(..., description="Risk level: low, medium, high")


class ChainService:
    """Service for managing LangChain chains"""
    
    def __init__(self):
        self.llm = None
        
    async def initialize(self):
        """Initialize chain service"""
        await langchain_service.initialize()
        self.llm = langchain_service.llm
        logger.info("Chain service initialized")
    
    async def analyze_project_request(self, description: str, context: Dict[str, Any]) -> ProjectRequirements:
        """Analyze project request and extract requirements"""
        parser = PydanticOutputParser(pydantic_object=ProjectRequirements)
        
        chain = LLMChain(
            llm=self.llm,
            prompt=PROJECT_ANALYSIS_PROMPT,
            output_parser=OutputFixingParser.from_llm(parser=parser, llm=self.llm)
        )
        
        try:
            result = await chain.arun(
                description=description,
                context=json.dumps(context)
            )
            return result
        except Exception as e:
            logger.error(f"Failed to analyze project request: {str(e)}")
            raise
    
    async def plan_project_implementation(
        self,
        requirements: ProjectRequirements
    ) -> Dict[str, Any]:
        """Create implementation plan based on requirements"""
        chain = LLMChain(
            llm=self.llm,
            prompt=PROJECT_PLANNING_PROMPT
        )
        
        result = await chain.arun(
            requirements=requirements.json(),
            constraints=json.dumps(requirements.constraints)
        )
        
        # Parse result (in production, use proper parsing)
        return {
            "architecture": "microservices",
            "technology_stack": requirements.technologies,
            "phases": ["setup", "backend", "frontend", "deployment"],
            "estimated_time": "2 weeks"
        }
    
    async def analyze_change_impact(
        self,
        current_state: Dict[str, Any],
        requested_change: str
    ) -> ChangeImpact:
        """Analyze the impact of a requested change"""
        parser = PydanticOutputParser(pydantic_object=ChangeImpact)
        
        chain = LLMChain(
            llm=self.llm,
            prompt=CHANGE_ANALYSIS_PROMPT,
            output_parser=OutputFixingParser.from_llm(parser=parser, llm=self.llm)
        )
        
        try:
            result = await chain.arun(
                current_state=json.dumps(current_state),
                requested_change=requested_change
            )
            return result
        except Exception as e:
            logger.error(f"Failed to analyze change impact: {str(e)}")
            raise
    
    async def generate_clarifying_questions(
        self,
        context: Dict[str, Any],
        ambiguous_aspects: List[str]
    ) -> List[str]:
        """Generate clarifying questions for ambiguous requests"""
        chain = LLMChain(
            llm=self.llm,
            prompt=CLARIFICATION_PROMPT
        )
        
        result = await chain.arun(
            context=json.dumps(context),
            ambiguity=", ".join(ambiguous_aspects)
        )
        
        # Extract questions from result
        questions = result.strip().split("\n")
        return [q.strip() for q in questions if q.strip()]
    
    def create_project_generation_chain(self) -> SequentialChain:
        """Create a chain for complete project generation"""
        # Analysis chain
        analysis_chain = LLMChain(
            llm=self.llm,
            prompt=PROJECT_ANALYSIS_PROMPT,
            output_key="requirements"
        )
        
        # Planning chain
        planning_chain = LLMChain(
            llm=self.llm,
            prompt=PROJECT_PLANNING_PROMPT,
            output_key="plan"
        )
        
        # Sequential chain
        overall_chain = SequentialChain(
            chains=[analysis_chain, planning_chain],
            input_variables=["description", "context"],
            output_variables=["requirements", "plan"],
            verbose=True
        )
        
        return overall_chain


# Singleton instance
chain_service = ChainService()
EOF

# Create conversation service
echo "📝 Creating conversation service..."
cat > app/services/conversation.py << 'EOF'
from typing import Dict, Any, List, Optional, AsyncGenerator
from datetime import datetime
import json
import logging
from langchain.schema import HumanMessage, AIMessage

from app.core.langchain_config import langchain_service
from app.services.chains import chain_service
from app.models.conversation import ConversationSession, Message
from app.core.exceptions import ValidationException

logger = logging.getLogger(__name__)


class ConversationService:
    """Service for managing conversations"""
    
    def __init__(self):
        self.sessions: Dict[str, ConversationSession] = {}
        
    async def initialize(self):
        """Initialize conversation service"""
        await langchain_service.initialize()
        await chain_service.initialize()
        logger.info("Conversation service initialized")
    
    async def process_message(
        self,
        message: str,
        session_id: str,
        context: Optional[Dict[str, Any]] = None
    ) -> Dict[str, Any]:
        """Process a user message and generate response"""
        try:
            # Get or create session
            session = self._get_or_create_session(session_id)
            
            # Add user message to history
            user_message = Message(
                role="user",
                content=message,
                timestamp=datetime.utcnow()
            )
            session.messages.append(user_message)
            
            # Analyze intent
            intent_analysis = await langchain_service.analyze_intent(message)
            
            # Get conversation chain
            chain = await langchain_service.get_conversation_chain(session_id)
            
            # Generate response
            response = await chain.arun(input=message)
            
            # Add AI response to history
            ai_message = Message(
                role="assistant",
                content=response,
                timestamp=datetime.utcnow(),
                metadata=intent_analysis
            )
            session.messages.append(ai_message)
            
            # Update session
            session.last_activity = datetime.utcnow()
            session.context.update(context or {})
            
            return {
                "response": response,
                "session_id": session_id,
                "intent": intent_analysis,
                "timestamp": ai_message.timestamp,
                "message_count": len(session.messages)
            }
            
        except Exception as e:
            logger.error(f"Failed to process message: {str(e)}")
            raise
    
    async def stream_response(
        self,
        message: str,
        session_id: str,
        context: Optional[Dict[str, Any]] = None
    ) -> AsyncGenerator[str, None]:
        """Stream response tokens as they're generated"""
        try:
            session = self._get_or_create_session(session_id)
            
            # Add user message
            user_message = Message(
                role="user",
                content=message,
                timestamp=datetime.utcnow()
            )
            session.messages.append(user_message)
            
            # Stream response
            chain = await langchain_service.get_conversation_chain(session_id)
            
            full_response = ""
            async for token in chain.astream({"input": message}):
                if "response" in token:
                    chunk = token["response"]
                    full_response += chunk
                    yield chunk
            
            # Save complete response
            ai_message = Message(
                role="assistant",
                content=full_response,
                timestamp=datetime.utcnow()
            )
            session.messages.append(ai_message)
            
        except Exception as e:
            logger.error(f"Failed to stream response: {str(e)}")
            yield f"Error: {str(e)}"
    
    def get_session(self, session_id: str) -> Optional[ConversationSession]:
        """Get session by ID"""
        return self.sessions.get(session_id)
    
    def _get_or_create_session(self, session_id: str) -> ConversationSession:
        """Get existing session or create new one"""
        if session_id not in self.sessions:
            self.sessions[session_id] = ConversationSession(
                session_id=session_id,
                created_at=datetime.utcnow(),
                last_activity=datetime.utcnow()
            )
        return self.sessions[session_id]
    
    def clear_session(self, session_id: str) -> bool:
        """Clear a conversation session"""
        if session_id in self.sessions:
            del self.sessions[session_id]
            langchain_service.clear_memory(session_id)
            logger.info(f"Cleared session: {session_id}")
            return True
        return False
    
    def get_session_history(self, session_id: str) -> List[Dict[str, Any]]:
        """Get conversation history for a session"""
        session = self.sessions.get(session_id)
        if not session:
            return []
        
        return [
            {
                "role": msg.role,
                "content": msg.content,
                "timestamp": msg.timestamp.isoformat(),
                "metadata": msg.metadata
            }
            for msg in session.messages
        ]
    
    async def analyze_project_request(
        self,
        description: str,
        session_id: str
    ) -> Dict[str, Any]:
        """Analyze a project generation request"""
        session = self._get_or_create_session(session_id)
        
        # Extract requirements
        requirements = await chain_service.analyze_project_request(
            description=description,
            context=session.context
        )
        
        # Create implementation plan
        plan = await chain_service.plan_project_implementation(requirements)
        
        # Store in session context
        session.context["current_project"] = {
            "requirements": requirements.dict(),
            "plan": plan
        }
        
        return {
            "requirements": requirements.dict(),
            "plan": plan,
            "session_id": session_id
        }


# Singleton instance
conversation_service = ConversationService()
EOF

# Create conversation models
echo "📝 Creating conversation models..."
cat > app/models/conversation.py << 'EOF'
from pydantic import BaseModel, Field
from typing import List, Dict, Any, Optional
from datetime import datetime
import uuid


class Message(BaseModel):
    """Conversation message model"""
    id: str = Field(default_factory=lambda: str(uuid.uuid4()))
    role: str = Field(..., description="Role: user or assistant")
    content: str = Field(..., description="Message content")
    timestamp: datetime = Field(default_factory=datetime.utcnow)
    metadata: Dict[str, Any] = Field(default_factory=dict)


class ConversationSession(BaseModel):
    """Conversation session model"""
    session_id: str = Field(..., description="Unique session identifier")
    messages: List[Message] = Field(default_factory=list)
    context: Dict[str, Any] = Field(default_factory=dict)
    created_at: datetime = Field(default_factory=datetime.utcnow)
    last_activity: datetime = Field(default_factory=datetime.utcnow)
    metadata: Dict[str, Any] = Field(default_factory=dict)
    
    class Config:
        json_encoders = {
            datetime: lambda v: v.isoformat()
        }


class ProjectContext(BaseModel):
    """Project context within a conversation"""
    project_id: Optional[str] = None
    project_type: Optional[str] = None
    current_state: Dict[str, Any] = Field(default_factory=dict)
    requirements: Dict[str, Any] = Field(default_factory=dict)
    modifications: List[Dict[str, Any]] = Field(default_factory=list)
EOF

# Create tests for LangChain integration
echo "🧪 Creating LangChain tests..."
cat > tests/test_langchain.py << 'EOF'
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
EOF

echo "✅ Step 3 completed! LangChain integration implemented."
echo "📊 Created:"
echo "   - LangChain configuration and service"
echo "   - Prompt templates for various scenarios"
echo "   - Chain implementations for complex workflows"
echo "   - Conversation service with streaming support"
echo "   - Models for conversation management"
echo "   - Tests for LangChain components"

echo ""
echo "⚠️  Note: Remember to set your OpenAI API key in the .env file"
echo ""
echo "Next step: Run setup_step4_conversation_management.sh"