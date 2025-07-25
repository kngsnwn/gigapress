package etners.common.config.security;

import static org.springframework.security.config.Customizer.withDefaults;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.security.CustomAccessDeniedHandler;
import common.security.CustomAuthenticationEntryPoint;
import common.util.filter.NotFoundUrlFilter;
import etners.common.domains.jwt.TokenProvider;
import etners.common.util.filter.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig implements WebSecurityCustomizer {

  private final TokenProvider tokenProvider;
  private final RequestMappingHandlerMapping requestMappingHandlerMapping;

  @Value("${not.verify.url}")
  String[] notVerifyUrls;
  private final String[] onlyEtnersUrls = {};
  private final String[] onlyClientUrls = {};

  @Override
  public void customize(WebSecurity web) {
    web.ignoring().requestMatchers(notVerifyUrls);
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
      .formLogin(FormLoginConfigurer::disable)
      .httpBasic(HttpBasicConfigurer::disable)
      .csrf(CsrfConfigurer::disable)
      .cors(withDefaults())
      .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
      .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(
        requests ->
          requests
            .requestMatchers(notVerifyUrls).permitAll()
            .requestMatchers(onlyEtnersUrls).hasRole("ETNERS")
            .requestMatchers(onlyClientUrls).hasRole("CLIENT")
            .anyRequest().authenticated()
      )
      .addFilterBefore(new NotFoundUrlFilter(requestMappingHandlerMapping), ChannelProcessingFilter.class)
      .addFilterBefore(new JwtFilter(tokenProvider, new ObjectMapper()), ExceptionTranslationFilter.class)
      .exceptionHandling(httpSecurityExceptionHandlingConfigurer ->
        httpSecurityExceptionHandlingConfigurer
          .accessDeniedHandler(new CustomAccessDeniedHandler(new ObjectMapper()))
          .authenticationEntryPoint(new CustomAuthenticationEntryPoint(new ObjectMapper()))
      )
      .build();
  }

}

