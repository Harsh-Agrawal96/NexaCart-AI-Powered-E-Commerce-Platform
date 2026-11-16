package com.n11bootcamp.api_gateway.ratelimit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.cloud.config.enabled=false",
                "eureka.client.enabled=false",
                "spring.main.allow-bean-definition-overriding=true",

                // Enable rate limiting for test
                "rate-limiting.enabled=true",
                "rate-limiting.exempt-path-patterns[0]=/actuator/**",

                // Low limits so the test is fast and deterministic
                "rate-limiting.policy.limit-for-period=2",
                "rate-limiting.policy.limit-refresh-period=10s",
                "rate-limiting.policy.timeout-duration=0"
        }
)
class RateLimitingIntegrationTest {

    static DisposableServer upstream;

    @LocalServerPort
    int port;

    @Autowired
    WebTestClient webTestClient;

    @BeforeAll
    static void startUpstream() {
        upstream = HttpServer.create()
                .port(0)
                .route(routes -> routes.get("/__test/limited",
                        (req, res) -> res.status(200).sendString(Mono.just("ok"))))
                .bindNow();
    }

    @AfterAll
    static void stopUpstream() {
        if (upstream != null) {
            upstream.disposeNow();
        }
    }

    @DynamicPropertySource
    static void gatewayTestRoute(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.gateway.routes[0].id", () -> "test-upstream");
        registry.add("spring.cloud.gateway.routes[0].uri", () -> "http://localhost:" + upstream.port());
        registry.add("spring.cloud.gateway.routes[0].predicates[0]", () -> "Path=/__test/limited");
    }

    @Test
    void shouldReturn429WhenLimitExceededForSameUser() {
        WebTestClient client = webTestClient.mutate()
                .baseUrl("http://localhost:" + port)
                .defaultHeader("X-User-Id", "test-user-1")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();

        client.get().uri("/__test/limited")
                .exchange()
                .expectStatus().isOk();

        client.get().uri("/__test/limited")
                .exchange()
                .expectStatus().isOk();

        client.get().uri("/__test/limited")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @TestConfiguration
    @EnableWebFluxSecurity
    static class PermitAllSecurityConfig {
        @Bean
        SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
            return http
                    .csrf(ServerHttpSecurity.CsrfSpec::disable)
                    .authorizeExchange(ex -> ex.anyExchange().permitAll())
                    .build();
        }
    }
}

