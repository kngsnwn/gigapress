"""Git service for version control operations"""
import os
import shutil
from pathlib import Path
from typing import List, Dict, Any, Optional
from git import Repo, GitCommandError
from app.core.config import settings
from app.core.logging import setup_logging
from app.schemas.git import GitRepository, GitCommit, GitBranch
from app.services.template_service import template_service

logger = setup_logging()

class GitService:
    def __init__(self):
        self.repos_dir = Path("repositories")
        self.repos_dir.mkdir(exist_ok=True)
        
    def init_repository(self, repo: GitRepository) -> bool:
        """Initialize a new Git repository"""
        try:
            repo_path = self.repos_dir / repo.project_id / repo.repo_name
            repo_path.mkdir(parents=True, exist_ok=True)
            
            # Initialize repository
            git_repo = Repo.init(repo_path)
            
            # Set default branch
            git_repo.git.checkout("-b", repo.default_branch)
            
            # Configure git
            with git_repo.config_writer() as config:
                config.set_value("user", "name", settings.GIT_AUTHOR_NAME)
                config.set_value("user", "email", settings.GIT_AUTHOR_EMAIL)
                
            logger.info(f"Initialized repository: {repo_path}")
            return True
            
        except Exception as e:
            logger.error(f"Failed to initialize repository: {e}")
            return False
            
    def create_gitignore(self, project_id: str, repo_name: str, template: str) -> bool:
        """Create .gitignore file from template"""
        try:
            repo_path = self.repos_dir / project_id / repo_name
            gitignore_content = template_service.render_template(
                f"git/gitignore/{template}.gitignore.j2",
                {}
            )
            
            gitignore_path = repo_path / ".gitignore"
            gitignore_path.write_text(gitignore_content)
            
            return True
        except Exception as e:
            logger.error(f"Failed to create .gitignore: {e}")
            return False
            
    def create_readme(self, project_id: str, repo_name: str, content: Dict[str, Any]) -> bool:
        """Create README.md file"""
        try:
            repo_path = self.repos_dir / project_id / repo_name
            readme_content = template_service.render_template(
                "git/README.md.j2",
                content
            )
            
            readme_path = repo_path / "README.md"
            readme_path.write_text(readme_content)
            
            return True
        except Exception as e:
            logger.error(f"Failed to create README: {e}")
            return False
            
    def commit(self, project_id: str, repo_name: str, commit: GitCommit) -> bool:
        """Make a commit"""
        try:
            repo_path = self.repos_dir / project_id / repo_name
            repo = Repo(repo_path)
            
            # Add files
            if commit.files:
                repo.index.add(commit.files)
            else:
                repo.git.add(A=True)  # Add all files
                
            # Commit
            repo.index.commit(
                commit.message,
                author=f"{commit.author_name or settings.GIT_AUTHOR_NAME} <{commit.author_email or settings.GIT_AUTHOR_EMAIL}>"
            )
            
            logger.info(f"Created commit: {commit.message}")
            return True
            
        except Exception as e:
            logger.error(f"Failed to commit: {e}")
            return False
            
    def create_branch(self, project_id: str, repo_name: str, branch: GitBranch) -> bool:
        """Create a new branch"""
        try:
            repo_path = self.repos_dir / project_id / repo_name
            repo = Repo(repo_path)
            
            # Create and checkout new branch
            repo.git.checkout("-b", branch.name, branch.from_branch)
            
            logger.info(f"Created branch: {branch.name}")
            return True
            
        except Exception as e:
            logger.error(f"Failed to create branch: {e}")
            return False
            
    def get_branches(self, project_id: str, repo_name: str) -> List[str]:
        """Get list of branches"""
        try:
            repo_path = self.repos_dir / project_id / repo_name
            repo = Repo(repo_path)
            
            return [ref.name for ref in repo.references if ref.name.startswith("refs/heads/")]
            
        except Exception as e:
            logger.error(f"Failed to get branches: {e}")
            return []
            
    def generate_github_actions(self, workflow_config: Dict[str, Any]) -> str:
        """Generate GitHub Actions workflow"""
        return template_service.render_template(
            "cicd/github-actions.yml.j2",
            workflow_config
        )
        
    def generate_gitlab_ci(self, pipeline_config: Dict[str, Any]) -> str:
        """Generate GitLab CI pipeline"""
        return template_service.render_template(
            "cicd/gitlab-ci.yml.j2",
            pipeline_config
        )

git_service = GitService()
