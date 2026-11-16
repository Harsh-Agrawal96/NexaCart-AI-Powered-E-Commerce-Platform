package com.n11bootcamp.api_gateway.filter;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;

import com.n11bootcamp.api_gateway.prop.GatewayResilienceProperties;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.timelimiter.TimeLimiterOperator;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 3)
public class GatewayCircuitBreakerFilter implements GlobalFilter {

    private static final byte[] FALLBACK_BODY = (
            "{\"success\":false,\"message\":\"Service temporarily unavailable\",\"code\":\"GATEWAY_CIRCUIT_OPEN\"}"
    ).getBytes(StandardCharsets.UTF_8);

    private final GatewayResilienceProperties properties;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final TimeLimiterRegistry timeLimiterRegistry;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public GatewayCircuitBreakerFilter(
            GatewayResilienceProperties properties,
            CircuitBreakerRegistry circuitBreakerRegistry,
            TimeLimiterRegistry timeLimiterRegistry
    ) {
        this.properties = Objects.requireNonNull(properties, "properties");
        this.circuitBreakerRegistry = Objects.requireNonNull(circuitBreakerRegistry, "circuitBreakerRegistry");
        this.timeLimiterRegistry = Objects.requireNonNull(timeLimiterRegistry, "timeLimiterRegistry");
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!properties.isEnabled()) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getURI().getPath();
        if (isMatch(path, properties.getExemptPathPatterns())) {
            return chain.filter(exchange);
        }
        if (!isMatch(path, properties.getProtectedPathPatterns())) {
            return chain.filter(exchange);
        }

        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(properties.getCircuitBreakerInstance());

        TimeLimiter tl = timeLimiterRegistry.timeLimiter(
                properties.getTimeLimiterInstance(),
                () -> TimeLimiterConfig.custom()
                        .timeoutDuration(nonNullOrDefault(properties.getFallbackTimeout(), Duration.ofSeconds(2)))
                        .cancelRunningFuture(true)
                        .build()
        );

        return chain.filter(exchange)
                .transformDeferred(TimeLimiterOperator.of(tl))
                .transformDeferred(CircuitBreakerOperator.of(cb))
                .doOnSuccess(v -> {
                    var status = exchange.getResponse().getStatusCode();
                    if (status != null && status.is5xxServerError()) {
                        cb.onError(0L, java.util.concurrent.TimeUnit.MILLISECONDS,
                                new Downstream5xxException(status.value()));
                    }
                })
                .onErrorResume(throwable -> {
                    if (throwable instanceof CallNotPermittedException) {
                        return serviceUnavailable(exchange);
                    }
                    if (isTimeout(throwable)) {
                        return serviceUnavailable(exchange);
                    }
                    return Mono.error(throwable);
                });
    }

    private boolean isTimeout(Throwable t) {
        String name = t.getClass().getName();
        return name.endsWith("TimeoutException") || name.contains("Timeout");
    }

    private Mono<Void> serviceUnavailable(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().setContentLength(FALLBACK_BODY.length);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(FALLBACK_BODY)));
    }

    private boolean isMatch(String path, List<String> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return false;
        }
        for (String p : patterns) {
            if (p != null && !p.isBlank() && pathMatcher.match(p.trim(), path)) {
                return true;
            }
        }
        return false;
    }

    private Duration nonNullOrDefault(Duration value, Duration fallback) {
        return value != null ? value : fallback;
    }

    static final class Downstream5xxException extends RuntimeException {
        Downstream5xxException(int statusCode) {
            super("Downstream 5xx status: " + statusCode);
        }
    }
}

