package etners.common.config.mail;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

  private static final String MAIL_TRANSPORT_PROTOCOL = "mail.transport.protocol";
  private static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
  private static final String MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";
  private static final String MAIL_DEBUG = "mail.debug";
  private static final String MAIL_MIME_CHARSET = "mail.mime.charset";
  private static final String MAIL_SMTP_ALLOW8BITMIMEL = "mail.smtp.allow8bitmime";
  private static final String MAIL_SMTPS_ALLOW8BITMIME = "mail.smtps.allow8bitmime";

  @Value("${spring.mail.protocol}")
  private String protocol;

  @Value("${spring.mail.host}")
  private String host;

  @Value("${spring.mail.port}")
  private int port;

  @Value("${spring.mail.username}")
  private String userName;

  @Value("${spring.mail.password}")
  private String password;

  private String mailMimeCharset = "UTF-8";

  @Bean
  public JavaMailSender mailSender() {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost(host);
    mailSender.setPort(port);
    mailSender.setUsername(userName);
    mailSender.setPassword(password);
    Properties properties = mailSender.getJavaMailProperties();
    properties.put(MAIL_TRANSPORT_PROTOCOL, protocol);
    properties.put(MAIL_SMTP_AUTH, true);
    properties.put(MAIL_SMTP_STARTTLS_ENABLE, true);
    properties.put(MAIL_DEBUG, true);
    properties.put(MAIL_MIME_CHARSET, mailMimeCharset);
    properties.put(MAIL_SMTP_ALLOW8BITMIMEL, true);
    properties.put(MAIL_SMTPS_ALLOW8BITMIME, true);

    mailSender.setJavaMailProperties(properties);
    return mailSender;
  }
}
