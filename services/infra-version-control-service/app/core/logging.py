"""Logging configuration"""
import logging
import sys
from pathlib import Path

def setup_logging():
    """Setup logging configuration"""
    # Create logs directory
    Path("logs").mkdir(exist_ok=True)
    
    # Configure logging
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
        handlers=[
            logging.StreamHandler(sys.stdout),
            logging.FileHandler('logs/infra-version-control.log')
        ]
    )
    
    return logging.getLogger(__name__)
