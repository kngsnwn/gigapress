package etners.common.util.mail;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


@Service
public class MailContentBuilder {

  private TemplateEngine templateEngine;

  @Autowired
  public MailContentBuilder(TemplateEngine templateEngine) {
    this.templateEngine = templateEngine;
  }


  public String build(Map<String, Object> message, String templateName) {
    Context context = new Context();
    context.setVariables(message);
    return templateEngine.process("/mail/" + templateName, context);
  }

}
