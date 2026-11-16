package com.n11bootcamp.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.POST, "/api/v1/users/register").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/reviews/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/ai/**").permitAll()
                        .pathMatchers("/api/v1/internal/**").denyAll()
                        .pathMatchers("/actuator/**").permitAll()
                        // Swagger
                        .pathMatchers("/swagger-ui.html").permitAll()
                        .pathMatchers("/swagger-ui/**").permitAll()
                        .pathMatchers("/*/api-docs/**").permitAll()
                        .pathMatchers("/api-docs/**").permitAll()
                        .pathMatchers("/webjars/**").permitAll()
                        .pathMatchers("/actuator/gateway/routes").permitAll()
                        .pathMatchers("/*/actuator/health").permitAll()
                        .pathMatchers("/actuator/**").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                )
                .build();
    }
}
