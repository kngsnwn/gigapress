"""CI/CD pipeline API endpoints"""
from fastapi import APIRouter, HTTPException
from app.schemas.base import BaseResponse
from app.schemas.cicd import CICDRequest, GitHubActionsConfig, JenkinsConfig, GitLabCIConfig
from app.services.cicd_service import cicd_service
from app.services.redis_service import redis_service

router = APIRouter()

@router.post("/pipeline", response_model=BaseResponse)
async def generate_cicd_pipeline(request: CICDRequest):
    """Generate CI/CD pipeline configuration"""
    try:
        pipeline_content = ""
        filename = ""
        
        if request.pipeline_type == "github-actions":
            # Generate GitHub Actions workflow
            workflow_jobs = cicd_service.generate_build_workflow(
                request.build_steps[0].get("type", "backend"),
                request.build_steps[0].get("framework", "express")
            )
            
            config = GitHubActionsConfig(
                name=f"{request.project_id} CI/CD",
                triggers={
                    "push": {"branches": ["main", "develop"]},
                    "pull_request": {"branches": ["main"]}
                },
                jobs=workflow_jobs
            )
            
            pipeline_content = cicd_service.generate_github_actions(config)
            filename = ".github/workflows/main.yml"
            
        elif request.pipeline_type == "jenkins":
            # Generate Jenkinsfile
            stages = []
            for step in request.build_steps:
                stages.append({
                    "name": step.get("name", "Build"),
                    "steps": step.get("commands", [])
                })
                
            config = JenkinsConfig(
                pipeline_name=request.project_id,
                stages=stages,
                environment={"PROJECT_ID": request.project_id}
            )
            
            pipeline_content = cicd_service.generate_jenkins_pipeline(config)
            filename = "Jenkinsfile"
            
        elif request.pipeline_type == "gitlab-ci":
            # Generate GitLab CI
            jobs = {}
            for i, step in enumerate(request.build_steps):
                jobs[f"build-{i}"] = {
                    "stage": "build",
                    "script": step.get("commands", [])
                }
                
            config = GitLabCIConfig(
                stages=["build", "test", "deploy"],
                variables={"PROJECT_ID": request.project_id},
                jobs=jobs
            )
            
            pipeline_content = cicd_service.generate_gitlab_ci(config)
            filename = ".gitlab-ci.yml"
            
        # Cache the result
        cache_key = f"cicd:{request.project_id}:{request.pipeline_type}"
        await redis_service.set(cache_key, pipeline_content)
        
        return BaseResponse(
            success=True,
            message=f"{request.pipeline_type} pipeline generated successfully",
            data={
                "pipeline": pipeline_content,
                "filename": filename,
                "type": request.pipeline_type
            }
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/templates/{pipeline_type}", response_model=BaseResponse)
async def get_pipeline_templates(pipeline_type: str):
    """Get available pipeline templates"""
    try:
        templates = {
            "github-actions": [
                "node-build-deploy",
                "java-gradle-build",
                "python-test-deploy",
                "docker-build-push"
            ],
            "jenkins": [
                "declarative-pipeline",
                "scripted-pipeline",
                "multibranch-pipeline"
            ],
            "gitlab-ci": [
                "docker-build",
                "kubernetes-deploy",
                "terraform-apply"
            ]
        }
        
        return BaseResponse(
            success=True,
            message="Templates retrieved successfully",
            data={
                "templates": templates.get(pipeline_type, []),
                "type": pipeline_type
            }
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
