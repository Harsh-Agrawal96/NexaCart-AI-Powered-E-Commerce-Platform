package com.n11bootcamp.api_gateway.filter;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;

import com.n11bootcamp.api_gateway.prop.RateLimitingProperties;
import com.n11bootcamp.common.constant.HeaderConstants;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class RateLimitingFilter implements GlobalFilter {

    private static final String LIMITER_NAME_PREFIX = "gateway-";

    private final RateLimiterRegistry rateLimiterRegistry;
    private final RateLimitingProperties properties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final ConcurrentMap<String, RateLimiter> perKeyLimiters = new ConcurrentHashMap<>();

    public RateLimitingFilter(RateLimiterRegistry rateLimiterRegistry, RateLimitingProperties properties) {
        this.rateLimiterRegistry = Objects.requireNonNull(rateLimiterRegistry, "rateLimiterRegistry");
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!properties.isEnabled()) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getURI().getPath();
        if (isExemptPath(path, properties.getExemptPathPatterns())) {
            return chain.filter(exchange);
        }

        return resolveKey(exchange)
                .map(key -> perKeyLimiters.computeIfAbsent(key, k -> {
                    String limiterName = LIMITER_NAME_PREFIX + k;
                    return rateLimiterRegistry.rateLimiter(limiterName);
                }))
                .flatMap(limiter -> Mono.just(1)
                        .transformDeferred(RateLimiterOperator.of(limiter))
                        .then(chain.filter(exchange))
                        .onErrorResume(RequestNotPermitted.class, ex -> tooManyRequests(exchange)));
    }

    private boolean isExemptPath(String path, List<String> patterns) {
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

    private Mono<Void> tooManyRequests(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        return exchange.getResponse().setComplete();
    }

    private Mono<String> resolveKey(ServerWebExchange exchange) {
        String userId = exchange.getRequest().getHeaders().getFirst(HeaderConstants.USER_ID);
        if (userId != null && !userId.isBlank()) {
            return Mono.just("uid:" + userId.trim());
        }

        return exchange.getPrincipal()
                .ofType(JwtAuthenticationToken.class)
                .map(JwtAuthenticationToken::getToken)
                .map(Jwt::getSubject)
                .filter(s -> s != null && !s.isBlank())
                .map(s -> "sub:" + s.trim())
                .switchIfEmpty(Mono.fromSupplier(() -> "ip:" + resolveIp(exchange)));
    }

    private String resolveIp(ServerWebExchange exchange) {
        InetSocketAddress remote = exchange.getRequest().getRemoteAddress();
        if (remote == null || remote.getAddress() == null) {
            return "unknown";
        }
        return remote.getAddress().getHostAddress();
    }
}

