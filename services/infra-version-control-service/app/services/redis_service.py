"""Redis service for caching"""
import json
from typing import Any, Optional
import redis.asyncio as redis
from app.core.config import settings
from app.core.logging import setup_logging

logger = setup_logging()

class RedisService:
    def __init__(self):
        self.redis_client: Optional[redis.Redis] = None
        
    async def connect(self):
        """Connect to Redis"""
        try:
            self.redis_client = await redis.from_url(
                f"redis://:{settings.REDIS_PASSWORD}@{settings.REDIS_HOST}:{settings.REDIS_PORT}",
                encoding="utf-8",
                decode_responses=True
            )
            await self.redis_client.ping()
            logger.info("Connected to Redis")
        except Exception as e:
            logger.error(f"Failed to connect to Redis: {e}")
            raise
            
    async def disconnect(self):
        """Disconnect from Redis"""
        if self.redis_client:
            await self.redis_client.close()
            logger.info("Disconnected from Redis")
            
    async def get(self, key: str) -> Optional[Any]:
        """Get value from Redis"""
        if not self.redis_client:
            return None
        try:
            value = await self.redis_client.get(key)
            return json.loads(value) if value else None
        except Exception as e:
            logger.error(f"Redis get error: {e}")
            return None
            
    async def set(self, key: str, value: Any, expire: int = 3600) -> bool:
        """Set value in Redis with expiration"""
        if not self.redis_client:
            return False
        try:
            await self.redis_client.set(
                key, 
                json.dumps(value), 
                ex=expire
            )
            return True
        except Exception as e:
            logger.error(f"Redis set error: {e}")
            return False
            
    async def delete(self, key: str) -> bool:
        """Delete key from Redis"""
        if not self.redis_client:
            return False
        try:
            await self.redis_client.delete(key)
            return True
        except Exception as e:
            logger.error(f"Redis delete error: {e}")
            return False

redis_service = RedisService()
