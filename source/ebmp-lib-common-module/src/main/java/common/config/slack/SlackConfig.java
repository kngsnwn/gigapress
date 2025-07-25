package common.config.slack;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SlackConfig {

  @Value("${slack.webhook.token}")
  private String token;

  @Bean
  public MethodsClient methodsClient() {
    Slack slackClient = Slack.getInstance();
    return slackClient.methods(token);
  }
}
