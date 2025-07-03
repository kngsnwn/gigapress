package com.gigapress.mcp.config;

import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {
    
    @Value("${dynamic-update-engine.base-url}")
    private String dynamicUpdateEngineBaseUrl;
    
    @Value("${dynamic-update-engine.connect-timeout}")
    private int connectTimeout;
    
    @Value("${dynamic-update-engine.read-timeout}")
    private int readTimeout;
    
    @Bean
    public WebClient dynamicUpdateEngineWebClient() {
        HttpClient httpClient = HttpClient.create()
            .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
            .responseTimeout(Duration.ofMillis(readTimeout))
            .doOnConnected(conn -> 
                conn.addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
            );
        
        return WebClient.builder()
            .baseUrl(dynamicUpdateEngineBaseUrl)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }
}
