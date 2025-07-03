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
