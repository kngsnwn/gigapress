package etners.common.config.container;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.ajp.AbstractAjpProtocol;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ContainerConfig {

  @Value("${tomcat.ajp.protocol}")
  String ajpProtocol;

  @Value("${tomcat.ajp.port}")
  int ajpPort;

  @Bean
  public ServletWebServerFactory servletContainer() throws UnknownHostException {

    TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
    tomcat.addContextCustomizers(context -> {
      SecurityConstraint constraint = new SecurityConstraint();
      SecurityCollection collection = new SecurityCollection();
      //http method
      collection.addMethod("CONNECT");
      collection.addMethod("HEAD");
      collection.addMethod("TRACE");
      //url matching expression
      collection.addPattern("/*");
      constraint.addCollection(collection);
      constraint.setAuthConstraint(true);
      context.addConstraint(constraint);

      //Set to use httpOnly
      context.setUseHttpOnly(true);

    });
    tomcat.addAdditionalTomcatConnectors(createAjpConnector());

    return tomcat;
  }

  private Connector createAjpConnector() throws UnknownHostException {
    Connector ajpConnector = new Connector(ajpProtocol);
    ajpConnector.setPort(ajpPort);
    ajpConnector.setSecure(false);
    ajpConnector.setAllowTrace(false);
    ajpConnector.setScheme("http");
    ((AbstractAjpProtocol<?>) ajpConnector.getProtocolHandler()).setSecretRequired(false);
    ((AbstractAjpProtocol<?>) ajpConnector.getProtocolHandler())
        .setAddress(InetAddress.getByName("0.0.0.0"));
    return ajpConnector;
  }
}


