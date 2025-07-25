package etners.common.util.redis;

import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.output.StatusOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandType;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.StreamInfo.XInfoStream;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

public abstract class AbstractRedisOperator {

  protected final RedisTemplate<String, String> redisTemplate;
  protected final RedisTemplate<String, Object> streamRedisTemplate;

  protected String taskModeKey;

  protected AbstractRedisOperator(RedisTemplate<String, String> redisTemplate, RedisTemplate<String, Object> streamRedisTemplate, String taskModeKey) {
    this.redisTemplate = redisTemplate;
    this.streamRedisTemplate = streamRedisTemplate;
    this.taskModeKey = taskModeKey;
  }

  public String getValues(String key) {
    ValueOperations<String, String> values = redisTemplate.opsForValue();
    return values.get(key);
  }


  public Object get(String key) {
    return streamRedisTemplate.opsForValue().get(key);
  }


  public void setValues(String key, String value) {
    redisTemplate.opsForValue().set(key, value);
  }


  public void setHashOps(String key, HashMap<String, String> value) {
    redisTemplate.opsForHash().putAll(key, value);
  }


  public String getHashOps(String key, String hashKey) {
    return redisTemplate.opsForHash().hasKey(key, hashKey) ? (String) redisTemplate.opsForHash().get(key, hashKey) : "";
  }


  public Object getHashValues(String key, String hashKey) {
    return streamRedisTemplate.opsForHash().hasKey(key, hashKey) ? redisTemplate.opsForHash().get(key, hashKey) : null;
  }


  public void setValuesWithTimeout(String key, String value, long timeout) {
    redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.MILLISECONDS);
  }


  public void setSets(String key, String... values) {
    redisTemplate.opsForSet().add(key, values);
  }


  public Set getSets(String key) {
    return redisTemplate.opsForSet().members(key);
  }


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


  public void ackStream(String consumerGroupName, MapRecord<String, Object, Object> message) {
    streamRedisTemplate.opsForStream().acknowledge(consumerGroupName, message);
  }


  public void ackStream(String streamKey, String consumerGroupName, String recordId) {
    streamRedisTemplate.opsForStream().acknowledge(streamKey, consumerGroupName, recordId);
  }


  public void claimStream(PendingMessage pendingMessage, String consumerName) {
    RedisAsyncCommands commands = (RedisAsyncCommands) streamRedisTemplate.getConnectionFactory().getConnection().getNativeConnection();

    CommandArgs<String, String> args = new CommandArgs<>(StringCodec.UTF8)
      .add(pendingMessage.getIdAsString())
      .add(pendingMessage.getGroupName())
      .add(consumerName)
      .add("20")
      .add(pendingMessage.getIdAsString());
    commands.dispatch(CommandType.XCLAIM, new StatusOutput(StringCodec.UTF8), args);
  }


  public MapRecord<String, Object, Object> findStreamMessageById(String streamKey, String id) {
    List<MapRecord<String, Object, Object>> mapRecordList = findStreamMessageByRange(streamKey, id, id);
    if (mapRecordList.isEmpty()) {
      return null;
    }
    return mapRecordList.get(0);
  }


  public List<MapRecord<String, Object, Object>> findStreamMessageByRange(String streamKey, String startId, String endId) {
    return streamRedisTemplate.opsForStream().range(streamKey, Range.closed(startId, endId));
  }


  public PendingMessages findStreamPendingMessages(String streamKey, String consumerGroupName, String consumerName) {
    return streamRedisTemplate.opsForStream().pending(streamKey, Consumer.from(consumerGroupName, consumerName), Range.unbounded(), 100L);
  }


  public long increaseHashValue(String key, String hashKey) {
    return redisTemplate.opsForHash().increment(key, hashKey, 1);
  }


  public void trim(String streamKey, int maxLength) {
    streamRedisTemplate.opsForStream().trim(streamKey, maxLength);
  }


  public XInfoStream getStreamInfo(String streamKey) {
    return streamRedisTemplate.opsForStream().info(streamKey);
  }


  public String getTaskEnabled(String solCd) {
    return (String) redisTemplate.opsForHash().get(taskModeKey, solCd);
  }


  public boolean isStreamEnabled(String solCd) {
    return "stream".equals(getTaskEnabled(solCd));
  }


  public boolean isSchedulerEnabled(String solCd) {
    return "scheduler".equals(getTaskEnabled(solCd));
  }


  public String updateTaskMode(String solCd, String mode) {
    redisTemplate.opsForHash().put(taskModeKey, solCd, mode);
    return getTaskEnabled(solCd);
  }


  public List<String> getHashKeys(String key) {
    return Optional.of(redisTemplate.opsForHash().entries(key))
      .filter(ObjectUtils::isNotEmpty)
      .map(entries -> entries.keySet().stream().map(keys -> (String) keys).toList())
      .orElse(Collections.emptyList());
  }


  public List<String> getTaskSolCdList() {
    return getHashKeys(taskModeKey);
  }


  public List<String> getSchedulerSolCdList() {
    return Optional.of(getTaskSolCdList())
      .filter(ObjectUtils::isNotEmpty)
      .map(solCdList -> solCdList.stream().filter(this::isSchedulerEnabled).toList())
      .orElse(Collections.emptyList())
      ;
  }


  public List<String> getStreamSolCdList() {
    return Optional.of(getTaskSolCdList())
      .filter(ObjectUtils::isNotEmpty)
      .map(solCdList -> solCdList.stream().filter(this::isStreamEnabled).toList())
      .orElse(Collections.emptyList())
      ;
  }

}
