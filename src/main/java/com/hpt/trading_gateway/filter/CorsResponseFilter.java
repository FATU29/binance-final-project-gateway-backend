package com.hpt.trading_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global CORS filter - DISABLED
 * CORS is now handled by Spring Security's CorsConfigurationSource in SecurityConfig
 * This filter is kept for reference but not used (@Component annotation removed)
 */
@Slf4j
//@Component  // Disabled - Spring Security handles CORS
public class CorsResponseFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // Always add CORS headers to all responses (including error responses)
        // This ensures CORS works even when services are unavailable (503, etc.)
        response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "false");
        response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, 
            "GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD");
        response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");
        response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
        response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");

        // Handle preflight OPTIONS request
        if (request.getMethod() == HttpMethod.OPTIONS) {
            response.setStatusCode(HttpStatus.OK);
            return response.setComplete();
        }

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            // Ensure CORS headers are still present after the chain completes
            // This is important for error responses (503, 500, etc.)
            if (!response.getHeaders().containsKey(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)) {
                response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
                response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "false");
                response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, 
                    "GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD");
                response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");
                response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
            }
        }));
    }

    /**
     * Run BEFORE all other filters to handle CORS preflight (OPTIONS) requests
     * and add CORS headers before any security checks
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
