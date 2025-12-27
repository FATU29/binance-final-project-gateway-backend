package com.hpt.trading_gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hpt.trading_gateway.dto.ApiResponse;
import com.hpt.trading_gateway.dto.ErrorResponse;
import com.hpt.trading_gateway.dto.UserData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Authentication filter that validates JWT tokens by calling the auth service /me endpoint.
 * This implements zero-trust security - the gateway doesn't trust tokens directly,
 * but validates them with the auth service on every request.
 * 
 * Addresses Tình huống 3 security requirements:
 * - All requests must go through the gateway
 * - Tokens are validated on every request (prevents old token abuse)
 * - User context is forwarded to downstream services via headers
 * - Comprehensive logging for security audit trail
 */
@Slf4j
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${auth.service.url:http://localhost:8081}")
    private String authServiceUrl;

    @Value("${gateway.security.secret}")
    private String gatewaySecret;

    public AuthenticationFilter(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        super(Config.class);
        this.webClientBuilder = webClientBuilder;
        this.objectMapper = objectMapper;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // Log incoming request for security audit
            log.info("Authentication check for: {} {}", request.getMethod(), request.getURI());
            
            // Extract Authorization header
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header from IP: {}", 
                    request.getRemoteAddress());
                return onError(exchange, "Missing or invalid Authorization header", 
                    HttpStatus.UNAUTHORIZED);
            }
            
            // Validate token with auth service
            return validateToken(authHeader)
                .flatMap(userData -> {
                    // Token is valid, add user context headers for downstream services
                    ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Id", userData.getId())
                        .header("X-User-Email", userData.getEmail())
                        .header("X-User-FirstName", userData.getFirstName())
                        .header("X-User-LastName", userData.getLastName())
                        .header("X-Gateway-Validated", "true") // Proof that request went through gateway
                        .build();
                    
                    log.info("Authentication successful for user: {} ({})", 
                        userData.getEmail(), userData.getId());
                    
                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                })
                .onErrorResume(error -> {
                    log.error("Authentication failed: {}", error.getMessage());
                    return onError(exchange, "Authentication failed: " + error.getMessage(), 
                        HttpStatus.UNAUTHORIZED);
                });
        };
    }

    /**
     * Validates the token by calling the auth service /me endpoint.
     * This ensures:
     * 1. Token signature is valid
     * 2. Token is not expired
     * 3. Token is not blacklisted (logout)
     */
    private Mono<UserData> validateToken(String authHeader) {
        log.debug("Validating token with auth service: {}", authServiceUrl);

        return webClientBuilder.build()
            .get()
            .uri(authServiceUrl + "/api/v1/auth/me")
            .header(HttpHeaders.AUTHORIZATION, authHeader)
            .header("X-Gateway-Signature", gatewaySecret)  // Add gateway signature for internal service validation
            .retrieve()
            .onStatus(
                status -> status.is4xxClientError() || status.is5xxServerError(),
                response -> {
                    log.warn("Auth service returned error status: {}", response.statusCode());
                    return Mono.error(new RuntimeException("Invalid or expired token"));
                }
            )
            .bodyToMono(new ParameterizedTypeReference<ApiResponse<UserData>>() {})
            .map(response -> {
                if (response.isSuccess() && response.getData() != null) {
                    return response.getData();
                }
                throw new RuntimeException("Invalid response from auth service");
            })
            .doOnError(error -> log.error("Token validation error: {}", error.getMessage()));
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

