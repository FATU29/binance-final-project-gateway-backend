package com.hpt.trading_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Global filter that adds a unique gateway signature to all requests.
 * This helps downstream services verify that requests came through the gateway.
 *
 * Addresses Tình huống 3 requirement:
 * "Nếu attacker gửi yêu cầu trực tiếp vào IP nội bộ của service, bạn sẽ xử lý như thế nào?"
 *
 * Solution:
 * 1. Gateway adds X-Gateway-Signature header with a secret token
 * 2. Downstream services MUST validate this header
 * 3. Network policies should block direct access to internal services
 * 4. Only allow traffic from gateway IP/service mesh
 */
@Slf4j
@Component
public class GatewayValidationFilter implements GlobalFilter, Ordered {

    @Value("${gateway.security.secret}")
    private String gatewaySecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Generate a unique request ID for tracing
        String requestId = UUID.randomUUID().toString();

        // Add gateway validation headers
        ServerHttpRequest modifiedRequest = request.mutate()
            .header("X-Gateway-Signature", gatewaySecret)
            .header("X-Request-Id", requestId)
            .header("X-Gateway-Timestamp", String.valueOf(System.currentTimeMillis()))
            .build();

        log.debug("Added gateway signature to request: {}", requestId);

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    /**
     * Run after logging filter but before authentication filter
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}

