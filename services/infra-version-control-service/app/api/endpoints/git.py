"""Git version control API endpoints"""
from fastapi import APIRouter, HTTPException
from app.schemas.base import BaseResponse
from app.schemas.git import GitInitRequest, GitCommit, GitBranch, GitRepository
from app.services.git_service import git_service
from app.services.kafka_service import kafka_service

router = APIRouter()

@router.post("/init", response_model=BaseResponse)
async def initialize_repository(request: GitInitRequest):
    """Initialize a new Git repository"""
    try:
        # Create repository
        repo = GitRepository(
            project_id=request.project_id,
            repo_name=request.repo_name,
            default_branch="main"
        )
        
        success = git_service.init_repository(repo)
        if not success:
            raise HTTPException(status_code=500, detail="Failed to initialize repository")
            
        # Create .gitignore
        if request.gitignore_template:
            git_service.create_gitignore(
                request.project_id,
                request.repo_name,
                request.gitignore_template
            )
            
        # Create README.md
        if request.include_readme:
            git_service.create_readme(
                request.project_id,
                request.repo_name,
                {
                    "project_name": request.repo_name,
                    "description": f"Repository for {request.repo_name}",
                    "license": request.license
                }
            )
            
        # Initial commit
        commit = GitCommit(
            message=request.initial_commit_message,
            files=["."]
        )
        git_service.commit(request.project_id, request.repo_name, commit)
        
        # Publish event
        await kafka_service.publish_event("git-events", {
            "event": "repository_initialized",
            "project_id": request.project_id,
            "repo_name": request.repo_name
        })
        
        return BaseResponse(
            success=True,
            message="Repository initialized successfully",
            data={
                "project_id": request.project_id,
                "repo_name": request.repo_name,
                "default_branch": "main"
            }
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/commit", response_model=BaseResponse)
async def create_commit(project_id: str, repo_name: str, commit: GitCommit):
    """Create a new commit"""
    try:
        success = git_service.commit(project_id, repo_name, commit)
        if not success:
            raise HTTPException(status_code=500, detail="Failed to create commit")
            
        # Publish event
        await kafka_service.publish_event("git-events", {
            "event": "commit_created",
            "project_id": project_id,
            "repo_name": repo_name,
            "message": commit.message
        })
        
        return BaseResponse(
            success=True,
            message="Commit created successfully",
            data={
                "message": commit.message,
                "files": commit.files
            }
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/branch", response_model=BaseResponse)
async def create_branch(project_id: str, repo_name: str, branch: GitBranch):
    """Create a new branch"""
    try:
        success = git_service.create_branch(project_id, repo_name, branch)
        if not success:
            raise HTTPException(status_code=500, detail="Failed to create branch")
            
        return BaseResponse(
            success=True,
            message="Branch created successfully",
            data={
                "branch_name": branch.name,
                "from_branch": branch.from_branch
            }
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/branches/{project_id}/{repo_name}", response_model=BaseResponse)
async def list_branches(project_id: str, repo_name: str):
    """List all branches in a repository"""
    try:
        branches = git_service.get_branches(project_id, repo_name)
        
        return BaseResponse(
            success=True,
            message="Branches retrieved successfully",
            data={
                "branches": branches,
                "total": len(branches)
            }
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/workflow", response_model=BaseResponse)
async def generate_git_workflow(project_id: str, workflow_type: str = "gitflow"):
    """Generate Git workflow configuration"""
    try:
        workflows = {
            "gitflow": {
                "branches": {
                    "main": "Production-ready code",
                    "develop": "Integration branch",
                    "feature/*": "New features",
                    "release/*": "Release preparation",
                    "hotfix/*": "Emergency fixes"
                },
                "rules": [
                    "Feature branches merge into develop",
                    "Release branches merge into main and develop",
                    "Hotfix branches merge into main and develop"
                ]
            },
            "github-flow": {
                "branches": {
                    "main": "Production-ready code",
                    "feature/*": "All changes"
                },
                "rules": [
                    "All changes through feature branches",
                    "Pull requests for code review",
                    "Deploy from main"
                ]
            }
        }
        
        return BaseResponse(
            success=True,
            message="Git workflow configuration generated",
            data={
                "workflow": workflows.get(workflow_type, workflows["gitflow"]),
                "type": workflow_type
            }
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
