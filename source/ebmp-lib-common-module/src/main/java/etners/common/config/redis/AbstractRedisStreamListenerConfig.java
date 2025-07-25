package etners.common.config.redis;

import common.util.string.StringUtil;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.output.StatusOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandKeyword;
import io.lettuce.core.protocol.CommandType;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamInfo;
import org.springframework.data.redis.connection.stream.StreamInfo.XInfoGroup;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

@Slf4j
public abstract class AbstractRedisStreamListenerConfig implements SmartLifecycle {

  private final RedisTemplate<String, Object> redisTemplate;
  private final AbstractRedisStreamConsumer abstractRedisStreamConsumer;
  private final AtomicBoolean running = new AtomicBoolean(false);

  protected String streamKey;
  protected String consumerGroupName;
  protected String consumerName;
  private Subscription subscription;
  private StreamMessageListenerContainer listenerContainer;

  protected AbstractRedisStreamListenerConfig(String streamKey, String consumerGroupName, String consumerName, RedisTemplate<String, Object> redisTemplate, AbstractRedisStreamConsumer abstractRedisStreamConsumer) {
    this.streamKey = streamKey;
    this.consumerGroupName = consumerGroupName;
    this.consumerName = consumerName;
    this.redisTemplate = redisTemplate;
    this.abstractRedisStreamConsumer = abstractRedisStreamConsumer;
  }

  @Override
  public void start() {
    if (running.compareAndSet(false, true)) {
      log.info("Redis Stream Listener 등록 시작");
      log.info("stream 이름 : {}, consumer group 이름 : {}, consumer 이름 : {}", streamKey, consumerGroupName, consumerName);
      try {
        createStreamConsumerGroup(streamKey, consumerGroupName);
        listenerContainer = createStreamMessageListenerContainer();

        var streamOffset = StreamOffset.create(
          streamKey, ReadOffset.lastConsumed()
        );

        var streamReadRequest = StreamMessageListenerContainer.StreamReadRequest
          .builder(streamOffset)
          .consumer(Consumer.from(consumerGroupName, consumerName))
          .cancelOnError(e -> false) // skip errors
          .errorHandler(e -> {
            log.info("Is Subscription Active: {}", subscription != null && subscription.isActive());
            log.error(StringUtil.extractStackTrace(e));
            if (e instanceof RedisConnectionFailureException) {
              log.error("Connection 끊어짐. Listener 재시작");
              try {
                shutdown();
                restartStreamListener();
              } catch (Exception ex) {
                log.error("Listener 재시작 실패: {}", ex.getMessage());
              }
            }

          })
          .autoAcknowledge(false)
          .build();
        subscription = listenerContainer.register(streamReadRequest, abstractRedisStreamConsumer);

        listenerContainer.start();
        log.info("=============================== Started Redis Stream Listener ===============================");
      } catch (Exception e) {
        log.error("Redis Stream Listener 등록 실패: {}", e.getMessage());
        running.set(false);
      }
    }
  }

  @Override
  public boolean isRunning() {
    return running.get();
  }

  @Override
  public void stop() {
    if (running.compareAndSet(true, false)) {
      log.info("Redis Stream Listener 종료 시작");
      shutdown();
      log.debug("Is Subscription Active: {}", subscription.isActive());
      log.debug("Is StreamListenerContainer Running : {}", listenerContainer.isRunning());
      log.info("Redis Stream Listener 종료 완료");
      log.debug("Is Connection Closed : {}", ObjectUtils.isNotEmpty(redisTemplate.getConnectionFactory()) && ObjectUtils.isNotEmpty(redisTemplate.getConnectionFactory().getConnection()) &&
        redisTemplate.getConnectionFactory().getConnection().isClosed());
    }
  }

  public void shutdown() {
    log.info("Shutting down Subscription and ListenerContainer.");
    try {
      if (subscription != null && subscription.isActive()) {
        subscription.cancel();

        // 2. cancel() 후 상태 점검 반복 (최대 3회 시도)
        int retries = 3;
        while (subscription.isActive() && retries > 0) {
          log.info("Waiting for subscription to cancel. Retries left: {}", retries);
          Thread.sleep(500);  // 500ms 대기
          retries--;
        }
        log.debug("Is Subscription Active: {}", subscription.isActive());

      }
      log.debug("Is ListenerContainer Active: {}", listenerContainer != null && listenerContainer.isRunning());
      if (listenerContainer != null && listenerContainer.isRunning()) {
        listenerContainer.stop();
      }

      if (ObjectUtils.isNotEmpty(redisTemplate.getConnectionFactory())) {
        if (ObjectUtils.isNotEmpty(redisTemplate.getConnectionFactory().getConnection()) &&
          redisTemplate.getConnectionFactory().getConnection().isClosed()) {
          log.debug("Redis connection is already closed.");
        } else {
          redisTemplate.getConnectionFactory().getConnection().close();
          log.info("Redis Connection is closed.");
        }
      }
    } catch (Exception e) {
      log.error("Error while shutting down Redis Stream Listener: {}", e.getMessage());
    }
  }

  public void createStreamConsumerGroup(String streamKey, String consumerGroupName) {
    // if stream is not exist, create stream and consumer group of it
    if (!redisTemplate.hasKey(streamKey)) {
      RedisAsyncCommands commands = (RedisAsyncCommands) redisTemplate
        .getConnectionFactory()
        .getConnection()
        .getNativeConnection();

      CommandArgs<String, String> args = new CommandArgs<>(StringCodec.UTF8)
        .add(CommandKeyword.CREATE)
        .add(streamKey)
        .add(consumerGroupName)
        .add("0")
        .add("MKSTREAM");

      commands.dispatch(CommandType.XGROUP, new StatusOutput(StringCodec.UTF8), args);
    }
    // stream is exist, create consumerGroup if is not exist
    else {
      if (!isStreamConsumerGroupExist(streamKey, consumerGroupName)) {
        redisTemplate.opsForStream().createGroup(streamKey, ReadOffset.from("0"), consumerGroupName);
      }
    }
  }

  public boolean isStreamConsumerGroupExist(String streamKey, String consumerGroupName) {
    Iterator<XInfoGroup> iterator = redisTemplate
      .opsForStream().groups(streamKey).stream().iterator();

    while (iterator.hasNext()) {
      StreamInfo.XInfoGroup xInfoGroup = iterator.next();
      if (xInfoGroup.groupName().equals(consumerGroupName)) {
        return true;
      }
    }
    return false;
  }

  public StreamMessageListenerContainer createStreamMessageListenerContainer() {
    return StreamMessageListenerContainer.create(
      redisTemplate.getConnectionFactory(),
      StreamMessageListenerContainer
        .StreamMessageListenerContainerOptions.builder()
        .hashKeySerializer(new StringRedisSerializer(StandardCharsets.UTF_8))
        .hashValueSerializer(new GenericJackson2JsonRedisSerializer())
        .pollTimeout(Duration.ofMillis(1000))
        .build()
    );
  }

  public void restartStreamListener() {
    try {
      if (listenerContainer != null && listenerContainer.isRunning()) {
        listenerContainer.stop();
        log.info("ListenerContainer stopped.");
      }

      createStreamConsumerGroup(streamKey, consumerGroupName);
      listenerContainer = createStreamMessageListenerContainer();
      // 연결을 끊고 재시작
      listenerContainer.start();
      log.info("ListenerContainer restarted. : {}", listenerContainer.isRunning());
    } catch (Exception e) {
      log.error("Error restarting StreamMessageListenerContainer: {}", e.getMessage());
    }
  }
}
