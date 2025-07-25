package etners.common.config.async;

import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

  @Bean
  public TaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(500);
    executor.setKeepAliveSeconds(60); //기본값
    executor.setThreadNamePrefix("async-");
    executor.setRejectedExecutionHandler(new CallerRunsPolicy()); // max 스레드 초과 요청시에도 처리
    executor.setWaitForTasksToCompleteOnShutdown(true); //shutdown시 유실없이 처리 후 종료
    executor.setAwaitTerminationSeconds(20); //최대 종료 대기 시간 20초
    executor.initialize();

    return executor;
  }
}
