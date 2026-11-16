package com.n11bootcamp.api_gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.n11bootcamp.common.constant.HeaderConstants;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE+1)
public class AuthFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .cast(JwtAuthenticationToken.class)
                .map(auth -> {
                    String keycloakId = auth.getToken().getSubject();
                    String email = auth.getToken().getClaimAsString("email");

                    log.info("[AuthFilter] keycloakId={} email={}", keycloakId, email);

                    return exchange.mutate()
                            .request(r -> r
                                    .header(HeaderConstants.USER_ID, keycloakId)
                                    .header(HeaderConstants.USER_EMAIL, email)
                            )
                            .build();
                })
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter);
    }
}
