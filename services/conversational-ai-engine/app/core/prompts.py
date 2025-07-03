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
