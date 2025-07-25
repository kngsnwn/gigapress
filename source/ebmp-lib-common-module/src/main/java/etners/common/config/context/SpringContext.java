package etners.common.config.context;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SpringContext implements ApplicationContextAware {

  private static ApplicationContext context;

  public static <T> T getBean(Class<T> type) {
    if (context == null) {
      throw new IllegalStateException("ApplicationContext가 초기화되지 않았습니다.");
    }
    return context.getBean(type);
  }

  @Override
  public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
    context = applicationContext;
  }

  @PostConstruct
  public void init() {
    // ApplicationContext가 정상적으로 주입되었을 때만 초기화
    if (context == null) {
      throw new IllegalStateException("ApplicationContext가 초기화되지 않았습니다.");
    }
    log.info("SpringContext 초기화 완료");
  }
}
