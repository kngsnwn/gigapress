"""Git-related schemas"""
from typing import List, Optional
from datetime import datetime
from pydantic import BaseModel

class GitRepository(BaseModel):
    project_id: str
    repo_name: str
    remote_url: Optional[str] = None
    default_branch: str = "main"
    description: Optional[str] = None
    
class GitCommit(BaseModel):
    message: str
    files: List[str]
    author_name: Optional[str] = None
    author_email: Optional[str] = None
    
class GitBranch(BaseModel):
    name: str
    from_branch: str = "main"
    
class GitInitRequest(BaseModel):
    project_id: str
    repo_name: str
    include_readme: bool = True
    gitignore_template: str = "node"  # node, java, python, etc.
    license: Optional[str] = "MIT"
    initial_commit_message: str = "Initial commit"
