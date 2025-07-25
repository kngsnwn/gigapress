package etners.common.util.mail;


import java.util.Map;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class CommonMailSender {

  private static final Logger LOGGER = LoggerFactory.getLogger(CommonMailSender.class);

  @Autowired
  private MailContentBuilder mailContentBuilder;

  @Autowired
  JavaMailSender mailSender;

  /**
   * <pre>
   * 메소드명    : send
   * 작성자    : hrkwon
   * 작성일    : 2019-03-06
   * 설명    : 메일전송
   * </pre> ex : mailSender.send(new String[]{toEmailAddress}, mailTemplateFileName, subject, fromEmailAddress, map);
   *
   * @param toEmailAddress : 상위 코드
   */
  public void send(String[] toEmailAddress, String mailTemplateFileName, String subject,
      String fromEmailAddress, Map<String, Object> map) throws Exception {

    MimeMessage mimeMessage = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
    String htmlMailContent = mailContentBuilder.build(map, mailTemplateFileName);

    helper.setSubject(subject);
    helper.setText(htmlMailContent, true);
    helper.setFrom(fromEmailAddress);
    helper.setTo(toEmailAddress);
    mailSender.send(mimeMessage);


  }
}
