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
