package com.gigapress.mcp.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
public class RequestResponseLoggingFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Instant start = Instant.now();
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();
        String requestId = exchange.getRequest().getHeaders().getFirst("X-Request-ID");

        if (requestId == null) {
            requestId = exchange.getRequest().getId();
        }

        // final 변수로 만들어서 람다에서 사용 가능하게 함
        final String finalRequestId = requestId;

        log.info("Incoming request: {} {} [Request ID: {}]", method, path, finalRequestId);

        return chain.filter(exchange)
                .doOnSuccess(aVoid -> {
                    Duration duration = Duration.between(start, Instant.now());
                    int statusCode = exchange.getResponse().getStatusCode() != null ?
                            exchange.getResponse().getStatusCode().value() : 0;

                    log.info("Outgoing response: {} {} - Status: {} - Duration: {}ms [Request ID: {}]",
                            method, path, statusCode, duration.toMillis(), finalRequestId);
                })
                .doOnError(error -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.error("Request failed: {} {} - Duration: {}ms - Error: {} [Request ID: {}]",
                            method, path, duration.toMillis(), error.getMessage(), finalRequestId);
                });
    }
}