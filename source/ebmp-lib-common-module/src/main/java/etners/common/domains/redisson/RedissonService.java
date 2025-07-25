package etners.common.domains.redisson;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedissonService {

  private final RedissonClient redissonClient;

  public boolean lock(String lockKey, long tryLockSeconds, long successLockSeconds, String bucketKey, int milliseconds) {
    RLock lock = redissonClient.getLock(lockKey);
    try {
      if (lock.tryLock(tryLockSeconds, successLockSeconds, TimeUnit.SECONDS)) {
        return redissonClient.getBucket(bucketKey).setIfAbsent(true, Duration.ofMillis(milliseconds));
      }
    } catch (InterruptedException e) {
      if (lock.isHeldByCurrentThread()) {
        lock.unlock();
      } else {
        throw new RuntimeException();
      }
    }
    return false;
  }

  public boolean unlock(String lockKey, String bucketKey) {
    RLock lock = redissonClient.getLock(lockKey);
    if (lock.isHeldByCurrentThread()) {
      lock.unlock();
      var bucket = redissonClient.getBucket(bucketKey);
      if (bucket.isExists()) {
        bucket.delete();
      }
      return true;
    }
    return false;
  }

}
