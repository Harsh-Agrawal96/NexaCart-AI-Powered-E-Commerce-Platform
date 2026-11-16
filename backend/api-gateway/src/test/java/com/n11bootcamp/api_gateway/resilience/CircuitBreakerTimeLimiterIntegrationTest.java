package com.n11bootcamp.api_gateway.resilience;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

                // Enable and scope filter
                "gateway-resilience.enabled=true",
                "gateway-resilience.protected-path-patterns[0]=/api/v1/payment/**",
                "gateway-resilience.exempt-path-patterns[0]=/actuator/**",
                "gateway-resilience.circuit-breaker-instance=paymentGatewayTest",
                "gateway-resilience.time-limiter-instance=paymentGatewayTest",

                // Fast-open circuit for test
                "resilience4j.circuitbreaker.instances.paymentGatewayTest.slidingWindowType=COUNT_BASED",
                "resilience4j.circuitbreaker.instances.paymentGatewayTest.slidingWindowSize=5",
                "resilience4j.circuitbreaker.instances.paymentGatewayTest.minimumNumberOfCalls=3",
                "resilience4j.circuitbreaker.instances.paymentGatewayTest.failureRateThreshold=50",
                "resilience4j.circuitbreaker.instances.paymentGatewayTest.waitDurationInOpenState=10s",
                "resilience4j.circuitbreaker.instances.paymentGatewayTest.permittedNumberOfCallsInHalfOpenState=1",

                // TimeLimiter (kept, but we use 5xx failures in this test)
                "resilience4j.timelimiter.instances.paymentGatewayTest.timeoutDuration=2s",
                "resilience4j.timelimiter.instances.paymentGatewayTest.cancelRunningFuture=true"
        }
)
class CircuitBreakerTimeLimiterIntegrationTest {

    static final AtomicInteger upstreamHits = new AtomicInteger(0);
    static DisposableServer upstream;

    @LocalServerPort
    int port;

    @Autowired
    WebTestClient webTestClient;

    @BeforeAll
    static void startUpstream() {
        upstream = HttpServer.create()
                .port(0)
                .route(routes -> routes.get("/api/v1/payment/test", (req, res) -> {
                    int hit = upstreamHits.incrementAndGet();
                    // First 3 calls return 500 → circuit should open afterwards
                    if (hit <= 3) {
                        return res.status(500).sendString(Mono.just("boom"));
                    }
                    return res.status(200).sendString(Mono.just("ok"));
                }))
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
        registry.add("spring.cloud.gateway.routes[0].id", () -> "payment-test-upstream");
        registry.add("spring.cloud.gateway.routes[0].uri", () -> "http://localhost:" + upstream.port());
        registry.add("spring.cloud.gateway.routes[0].predicates[0]", () -> "Path=/api/v1/payment/**");
    }

    @Test
    void shouldOpenCircuitAndReturn503WithoutHittingDownstream() {
        WebTestClient client = webTestClient.mutate()
                .baseUrl("http://localhost:" + port)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();

        // Warm up with failures (downstream 500)
        client.get().uri("/api/v1/payment/test").exchange().expectStatus().is5xxServerError();
        client.get().uri("/api/v1/payment/test").exchange().expectStatus().is5xxServerError();
        client.get().uri("/api/v1/payment/test").exchange().expectStatus().is5xxServerError();

        int hitsBefore = upstreamHits.get();

        // Circuit should be OPEN now → gateway returns 503 without calling downstream
        client.get().uri("/api/v1/payment/test")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.code").isEqualTo("GATEWAY_CIRCUIT_OPEN");

        int hitsAfter = upstreamHits.get();
        org.junit.jupiter.api.Assertions.assertEquals(hitsBefore, hitsAfter, "Downstream should not be hit while OPEN");
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

