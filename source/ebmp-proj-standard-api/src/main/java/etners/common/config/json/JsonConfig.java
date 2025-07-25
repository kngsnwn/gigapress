package etners.common.config.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import etners.common.util.filter.JsonFilter;
import etners.common.util.scope.CurrentUserData;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@RequiredArgsConstructor
@Configuration
public class JsonConfig {

  private final CurrentUserData currentUserData;

  @Bean
  public ObjectMapper objectMapper() {
    return Jackson2ObjectMapperBuilder
        .json()
        .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .modules(customJsonDeserializeModule(), new JavaTimeModule())
        .filters(new SimpleFilterProvider(
            Collections.singletonMap("jsonFilter", new JsonFilter(currentUserData))))
        .build();
  }

  private SimpleModule customJsonDeserializeModule() {
    SimpleModule module = new SimpleModule();
    module.addDeserializer(String.class, new StringStripJsonDeserializer());

    return module;
  }
}
