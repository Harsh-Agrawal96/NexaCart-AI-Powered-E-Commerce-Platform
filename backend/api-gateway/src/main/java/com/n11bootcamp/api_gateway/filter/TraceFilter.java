package com.n11bootcamp.api_gateway.filter;

import java.util.Optional;
import java.util.UUID;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.n11bootcamp.common.constant.HeaderConstants;

import lombok.extern.slf4j.Slf4j;

import org.springframework.core.Ordered;

import reactor.core.publisher.Mono;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String traceId = Optional
                .ofNullable(exchange.getRequest().getHeaders().getFirst(HeaderConstants.X_TRACE_ID))
                .filter(v -> !v.isBlank())
                .orElse(UUID.randomUUID().toString());

        log.info("[TraceFilter] {} {} → traceId={}",
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI().getPath(),
                traceId);

        ServerWebExchange mutated = exchange.mutate()
                .request(r -> r.header(HeaderConstants.X_TRACE_ID, traceId))
                .build();

        mutated.getResponse().beforeCommit(() -> {
            mutated.getResponse().getHeaders().set(HeaderConstants.X_TRACE_ID, traceId);
            return Mono.empty();
        });

        return chain.filter(mutated);
    }
}