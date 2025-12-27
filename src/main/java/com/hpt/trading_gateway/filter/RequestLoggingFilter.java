package com.hpt.trading_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Global filter for logging all requests and responses
 * Provides security audit trail as required by Tình huống 3
 * 
 * Logs include:
 * - Request timestamp
 * - Client IP address
 * - HTTP method and path
 * - User agent
 * - Response status
 * - Processing time
 * - User ID (if authenticated)
 */
@Slf4j
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startTime = System.currentTimeMillis();
        
        // Extract request information
        String method = request.getMethod().toString();
        String path = request.getURI().getPath();
        String clientIp = getClientIp(request);
        String userAgent = request.getHeaders().getFirst(HttpHeaders.USER_AGENT);
        
        // Log incoming request
        log.info(">>> Incoming Request: {} {} from IP: {} at {}", 
            method, path, clientIp, Instant.now());
        
        if (userAgent != null) {
            log.debug("User-Agent: {}", userAgent);
        }
        
        // Continue with the filter chain and log response
        return chain.filter(exchange)
            .doOnSuccess(aVoid -> {
                ServerHttpResponse response = exchange.getResponse();
                long duration = System.currentTimeMillis() - startTime;
                String userId = request.getHeaders().getFirst("X-User-Id");
                
                // Log response
                log.info("<<< Response: {} {} - Status: {} - Duration: {}ms - User: {}", 
                    method, path, 
                    response.getStatusCode(), 
                    duration,
                    userId != null ? userId : "anonymous");
            })
            .doOnError(error -> {
                long duration = System.currentTimeMillis() - startTime;
                
                // Log error
                log.error("!!! Error: {} {} - Duration: {}ms - Error: {}", 
                    method, path, duration, error.getMessage());
            });
    }

    /**
     * Get the real client IP address, considering proxy headers
     */
    private String getClientIp(ServerHttpRequest request) {
        // Check X-Forwarded-For header (set by proxies/load balancers)
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xForwardedFor.split(",")[0].trim();
        }
        
        // Check X-Real-IP header
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        // Fall back to remote address
        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }
        
        return "unknown";
    }

    /**
     * Set high priority to ensure this filter runs first
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}

