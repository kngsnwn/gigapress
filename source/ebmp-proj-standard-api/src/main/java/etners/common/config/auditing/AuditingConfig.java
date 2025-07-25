package etners.common.config.auditing;


import common.util.string.StringUtil;
import etners.common.util.scope.CurrentUserData;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@Configuration
public class AuditingConfig {

  private final CurrentUserData currentUserData;

  public AuditingConfig(CurrentUserData currentUserData) {
    this.currentUserData = currentUserData;
  }

  @Bean
  public AuditorAware<String> auditorAware() {
    return () -> {
      try {
        String unqUserId = currentUserData.getUnqUserId();
        if (StringUtil.isEmpty(unqUserId)) {
          return Optional.of("system");
        } else {
          return Optional.of(unqUserId);
        }
      } catch (Exception e) {
        return Optional.of("system");
      }
    };
  }

}
