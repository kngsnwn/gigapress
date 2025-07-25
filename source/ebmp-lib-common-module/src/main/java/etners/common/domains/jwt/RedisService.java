package etners.common.domains.jwt;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RedisService {

  private final RedisTemplate<String, String> redisTemplate;

  public String getValues(String key) {
    ValueOperations<String, String> values = redisTemplate.opsForValue();
    return values.get(key);
  }

  public Object get(String key) {
    return redisTemplate.opsForValue().get(key);
  }

  public void setValues(String key, String value) {
    ValueOperations<String, String> values = redisTemplate.opsForValue();
    values.set(key, value);
  }

  public void setHashOps(String key, HashMap<String, String> value) {
    redisTemplate.opsForHash().putAll(key, value);
  }

  public String getHashOps(String key, String hashKey) {
    return redisTemplate.opsForHash().hasKey(key, hashKey) ? (String) redisTemplate.opsForHash().get(key, hashKey) : "";
  }

  @Transactional
  public void setValuesWithTimeout(String key, String value, long timeout) {
    redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.MILLISECONDS);
  }

  public void setSets(String key, String... values) {
    redisTemplate.opsForSet().add(key, values);
  }

  public Set getSets(String key) {
    return redisTemplate.opsForSet().members(key);
  }

  @Transactional
  public void deleteValues(String key) {
    redisTemplate.delete(key);
  }

  public boolean isLogout(String key) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(key));
  }

  public boolean checkExistsValue(String value) {
    return !value.equals("false");
  }

  public void push(String key, String value) {
    redisTemplate.opsForList().leftPush(key, value);
  }

  public String pop(String key) {
    return redisTemplate.opsForList().rightPop(key);
  }

}
