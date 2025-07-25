package etners.common.config.redis;

import common.util.string.StringUtil;
import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

public abstract class AbstractRedisConfig {

  public RedisConnectionFactory redisConnectionFactory(String host, int port, String password, Integer database) {
    RedisStandaloneConfiguration redisConfiguration = new RedisStandaloneConfiguration();
    redisConfiguration.setHostName(host);
    redisConfiguration.setPort(port);
    if (StringUtil.isNotEmpty(password)) {
      redisConfiguration.setPassword(password);
    }
    if (ObjectUtils.isNotEmpty(database)) {
      redisConfiguration.setDatabase(database);
    }
    LettuceConnectionFactory factory = new LettuceConnectionFactory(redisConfiguration);
    factory.setValidateConnection(true);
    return factory;
  }

  public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory, boolean transactionSupport) {
    RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory);

    redisTemplate.setDefaultSerializer(new StringRedisSerializer(StandardCharsets.UTF_8));
    redisTemplate.setKeySerializer(new StringRedisSerializer(StandardCharsets.UTF_8));
    redisTemplate.setValueSerializer(new StringRedisSerializer(StandardCharsets.UTF_8));
    redisTemplate.setHashKeySerializer(new StringRedisSerializer(StandardCharsets.UTF_8));
    redisTemplate.setHashValueSerializer(new StringRedisSerializer(StandardCharsets.UTF_8));
    redisTemplate.setEnableTransactionSupport(transactionSupport);
    return redisTemplate;
  }

  public RedisTemplate<String, Object> streamRedisTemplate(RedisConnectionFactory redisConnectionFactory, boolean transactionSupport) {
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory);

    redisTemplate.setKeySerializer(new StringRedisSerializer(StandardCharsets.UTF_8));
    redisTemplate.setValueSerializer(new StringRedisSerializer(StandardCharsets.UTF_8));

    redisTemplate.setHashKeySerializer(new StringRedisSerializer(StandardCharsets.UTF_8));
    redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

    redisTemplate.setEnableTransactionSupport(transactionSupport);
    return redisTemplate;
  }

  public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory, boolean transactionSupport) {
    StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
    stringRedisTemplate.setConnectionFactory(redisConnectionFactory);
    stringRedisTemplate.setKeySerializer(new StringRedisSerializer(StandardCharsets.UTF_8));
    stringRedisTemplate.setValueSerializer(new StringRedisSerializer(StandardCharsets.UTF_8));
    stringRedisTemplate.setEnableTransactionSupport(transactionSupport);
    return stringRedisTemplate;
  }

}
