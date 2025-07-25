package etners.common.config.redis;

import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;

public abstract class AbstractRedisStreamConsumer implements StreamListener<String, MapRecord<String, Object, Object>> {

  @Override
  public void onMessage(MapRecord<String, Object, Object> message) {

  }
}
