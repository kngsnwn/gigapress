package etners;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.cloud.openfeign.EnableFeignClients;

//@EnableScheduling
@EnableFeignClients
@SpringBootApplication
public class EbmpWebStandardApplication {

  public static void main(String[] args) {
    SpringApplication application = new SpringApplication(EbmpWebStandardApplication.class);
    application.addListeners(new ApplicationPidFileWriter());
    application.run(args);
  }
}
