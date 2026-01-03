package com.hpt.trading_gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hpt.trading_gateway.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * VIP Authorization filter that checks if the user has VIP account type.
 * This filter should be applied after AuthenticationFilter.
 * 
 * VIP accounts can access AI model-based analyses.
 * Standard accounts are restricted from these endpoints.
 */
@Slf4j
@Component
public class VipAuthorizationFilter extends AbstractGatewayFilterFactory<VipAuthorizationFilter.Config> {

    private static final String VIP_ACCOUNT_TYPE = "VIP";
    private static final String ACCOUNT_TYPE_HEADER = "X-User-AccountType";

    private final ObjectMapper objectMapper;

    public VipAuthorizationFilter(ObjectMapper objectMapper) {
        super(Config.class);
        this.objectMapper = objectMapper;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // Get account type from header (set by AuthenticationFilter)
            String accountType = request.getHeaders().getFirst(ACCOUNT_TYPE_HEADER);
            
            log.debug("VIP authorization check for: {} {} - Account type: {}", 
                request.getMethod(), request.getURI(), accountType);
            
            // Check if user has VIP account
            if (accountType == null || !VIP_ACCOUNT_TYPE.equalsIgnoreCase(accountType)) {
                log.warn("VIP access denied for user with account type: {} - Path: {}", 
                    accountType, request.getURI());
                return onError(exchange, 
                    "Access denied. VIP account required to access AI model-based analyses.", 
                    HttpStatus.FORBIDDEN);
            }
            
            log.info("VIP access granted for path: {}", request.getURI());
            return chain.filter(exchange);
        };
    }

    /**
     * Returns an error response to the client
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        ErrorResponse errorResponse = new ErrorResponse(message);
        
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        } catch (JsonProcessingException e) {
            log.error("Error serializing error response", e);
            return response.setComplete();
        }
    }

    public static class Config {
        // Configuration properties can be added here if needed
    }
}

