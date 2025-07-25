package etners.common.config.webmvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import etners.common.interceptor.CommonInterceptor;
import etners.common.interceptor.EnvironmentSettingInterceptor;
import etners.common.util.scope.CurrentUserData;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer, WebMvcRegistrations {

  @Value("${not.verify.url}")
  private String[] notVerifyUrls;

  private final CommonInterceptor commonInterceptor;
  private final EnvironmentSettingInterceptor environmentSettingInterceptor;
  private final ObjectMapper objectMapper;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(commonInterceptor)
      .addPathPatterns("/**");

    registry.addInterceptor(environmentSettingInterceptor)
      .addPathPatterns("/test/**")
      .addPathPatterns("/**")
      .excludePathPatterns(notVerifyUrls);
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/").setViewName("forward:/index.html");
  }

  @Bean(name = "multipartResolver")
  public StandardServletMultipartResolver multipartResolver() {
    StandardServletMultipartResolver multipartResolver = new StandardServletMultipartResolver();

    return multipartResolver;
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/**")
        .addResourceLocations("classpath:/static/", "classpath:/templates/");
  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {

    //Swagger view
    StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter();
    stringHttpMessageConverter.setDefaultCharset(StandardCharsets.UTF_8);
    converters.add(stringHttpMessageConverter);

    //Json
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
    converters.add(new ByteArrayHttpMessageConverter());
    converters.add(converter);
  }
}
