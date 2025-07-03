"""Template service for generating configuration files"""
from typing import Dict, Any
from pathlib import Path
from jinja2 import Environment, FileSystemLoader, Template
from app.core.logging import setup_logging

logger = setup_logging()

class TemplateService:
    def __init__(self):
        template_dir = Path(__file__).parent.parent / "templates"
        self.env = Environment(
            loader=FileSystemLoader(str(template_dir)),
            trim_blocks=True,
            lstrip_blocks=True
        )
        
    def render_template(self, template_path: str, context: Dict[str, Any]) -> str:
        """Render a template with given context"""
        try:
            template = self.env.get_template(template_path)
            return template.render(**context)
        except Exception as e:
            logger.error(f"Template rendering error: {e}")
            raise
            
    def render_string(self, template_string: str, context: Dict[str, Any]) -> str:
        """Render a template string with given context"""
        try:
            template = Template(template_string)
            return template.render(**context)
        except Exception as e:
            logger.error(f"String template rendering error: {e}")
            raise

template_service = TemplateService()
