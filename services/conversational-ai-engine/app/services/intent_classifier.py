from typing import Dict, Any, List, Tuple
from enum import Enum
import re
import logging
from app.services.context_manager import context_manager

logger = logging.getLogger(__name__)


class Intent(Enum):
    """User intent types"""
    PROJECT_CREATE = "project_create"
    PROJECT_MODIFY = "project_modify"
    PROJECT_INFO = "project_info"
    CLARIFICATION = "clarification"
    GENERAL_QUERY = "general_query"
    HELP = "help"
    GREETING = "greeting"
    UNKNOWN = "unknown"


class IntentClassifier:
    """Classify user intents from messages"""
    
    def __init__(self):
        self.intent_patterns = self._build_intent_patterns()
        
    def _build_intent_patterns(self) -> Dict[Intent, List[re.Pattern]]:
        """Build regex patterns for intent classification"""
        return {
            Intent.PROJECT_CREATE: [
                re.compile(r'\b(create|build|make|develop|generate|start)\s+(?:a\s+)?(?:new\s+)?(project|app|application|website|api|service)\b', re.I),
                re.compile(r'\b(i\s+want|i\s+need|help\s+me)\s+(?:to\s+)?(create|build|make)\b', re.I),
                re.compile(r'\b(new|fresh)\s+(project|application|app)\b', re.I)
            ],
            Intent.PROJECT_MODIFY: [
                re.compile(r'\b(change|modify|update|add|remove|delete|edit)\s+(?:the\s+)?\w+', re.I),
                re.compile(r'\b(can\s+you|please|i\s+want\s+to)\s+(change|modify|update)', re.I),
                re.compile(r'\b(implement|integrate|include)\s+\w+\s+(?:feature|functionality)', re.I)
            ],
            Intent.PROJECT_INFO: [
                re.compile(r'\b(show|display|what|get)\s+(?:me\s+)?(?:the\s+)?(status|info|information|details|project)\b', re.I),
                re.compile(r'\b(current|existing)\s+(project|state|status)\b', re.I),
                re.compile(r'\bproject\s+(details|info|status)\b', re.I)
            ],
            Intent.HELP: [
                re.compile(r'\b(help|guide|how\s+to|tutorial|example|what\s+can)\b', re.I),
                re.compile(r'\b(explain|tell\s+me)\s+(?:about|how)\b', re.I),
                re.compile(r'\b(?:i\s+don\'t\s+understand|confused|not\s+sure)\b', re.I)
            ],
            Intent.GREETING: [
                re.compile(r'^(hi|hello|hey|greetings|good\s+(morning|afternoon|evening))[\s!]*$', re.I),
                re.compile(r'^(how\s+are\s+you|what\'s\s+up)[\s?]*$', re.I)
            ]
        }
    
    async def classify(
        self,
        message: str,
        session_id: str
    ) -> Tuple[Intent, float, Dict[str, Any]]:
        """Classify intent with confidence score and metadata"""
        # Get context
        context = await context_manager.get_relevant_context(session_id, include_history=True)
        
        # Clean message
        message_clean = message.strip().lower()
        
        # Check patterns
        intent_scores: Dict[Intent, float] = {}
        
        for intent, patterns in self.intent_patterns.items():
            max_score = 0.0
            for pattern in patterns:
                if pattern.search(message):
                    # Base score for pattern match
                    score = 0.7
                    
                    # Adjust based on context
                    if intent == Intent.PROJECT_MODIFY and context.get("project"):
                        score += 0.2  # Boost if project exists
                    elif intent == Intent.PROJECT_CREATE and not context.get("project"):
                        score += 0.2  # Boost if no project exists
                    
                    max_score = max(max_score, score)
            
            if max_score > 0:
                intent_scores[intent] = max_score
        
        # Get best intent
        if intent_scores:
            best_intent = max(intent_scores.items(), key=lambda x: x[1])
            intent, confidence = best_intent
        else:
            # Use context-based classification
            intent, confidence = await self._context_based_classification(message, context)
        
        # Extract metadata
        metadata = await self._extract_metadata(message, intent)
        
        logger.info(f"Classified intent: {intent.value} (confidence: {confidence:.2f})")
        
        return intent, confidence, metadata
    
    async def _context_based_classification(
        self,
        message: str,
        context: Dict[str, Any]
    ) -> Tuple[Intent, float]:
        """Classify based on context when no patterns match"""
        # Check if it's a follow-up to previous conversation
        if context.get("recent_conversation"):
            last_message = context["recent_conversation"][-1] if context["recent_conversation"] else None
            if last_message and last_message["role"] == "assistant":
                # Likely a clarification or response
                return Intent.CLARIFICATION, 0.6
        
        # Check message length and structure
        if len(message.split()) < 5:
            # Short message, might be clarification
            return Intent.CLARIFICATION, 0.5
        
        # Default to unknown
        return Intent.UNKNOWN, 0.3
    
    async def _extract_metadata(
        self,
        message: str,
        intent: Intent
    ) -> Dict[str, Any]:
        """Extract metadata based on intent"""
        metadata = {
            "intent": intent.value,
            "entities": await context_manager.extract_entities(message)
        }
        
        # Intent-specific metadata
        if intent == Intent.PROJECT_CREATE:
            # Extract project type
            project_types = ["web app", "mobile app", "api", "microservice", "website"]
            for ptype in project_types:
                if ptype in message.lower():
                    metadata["project_type"] = ptype
                    break
        
        elif intent == Intent.PROJECT_MODIFY:
            # Extract modification type
            mod_types = ["add", "remove", "change", "update", "delete"]
            for mtype in mod_types:
                if mtype in message.lower():
                    metadata["modification_type"] = mtype
                    break
        
        return metadata
    
    def get_intent_description(self, intent: Intent) -> str:
        """Get human-readable description of intent"""
        descriptions = {
            Intent.PROJECT_CREATE: "Create a new project",
            Intent.PROJECT_MODIFY: "Modify existing project",
            Intent.PROJECT_INFO: "Get project information",
            Intent.CLARIFICATION: "Provide clarification",
            Intent.GENERAL_QUERY: "General question",
            Intent.HELP: "Request for help",
            Intent.GREETING: "Greeting",
            Intent.UNKNOWN: "Unknown intent"
        }
        return descriptions.get(intent, "Unknown")


# Singleton instance
intent_classifier = IntentClassifier()
