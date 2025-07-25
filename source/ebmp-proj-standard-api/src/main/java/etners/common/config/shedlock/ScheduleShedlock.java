package etners.common.config.shedlock;

import javax.sql.DataSource;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by chode8703 on 2021-05-21.
 *
 * @author: chode8703
 * @Package: etners.common.config.shedlock
 * @Description:
 */

@Configuration
public class ScheduleShedlock {

  @Bean
  public LockProvider lockProvider(DataSource dataSource) {
    return new JdbcTemplateLockProvider(dataSource);
  }

}
