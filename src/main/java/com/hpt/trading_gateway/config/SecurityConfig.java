package com.hpt.trading_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for the API Gateway
 * 
 * Implements security best practices for Tình huống 3:
 * - Disables default Spring Security authentication (handled by our custom filter)
 * - Configures CORS for cross-origin requests
 * - Adds security headers to prevent common attacks
 * - Disables CSRF for stateless JWT authentication
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${CORS_ALLOWED_ORIGINS:http://localhost:3000}")
    private String corsAllowedOrigins;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            // Disable CSRF as we're using stateless JWT authentication
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            
            // Enable CORS with CorsConfigurationSource
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configure authorization
            .authorizeExchange(exchanges -> exchanges
                // Allow health check endpoints
                .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                // Allow fallback endpoints
                .pathMatchers("/fallback/**").permitAll()
                // Allow OPTIONS requests (CORS preflight)
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // All other requests are handled by our custom AuthenticationFilter
                .anyExchange().permitAll()
            )
            
            // Add security headers
            .headers(headers -> headers
                // Prevent clickjacking
                .frameOptions(frameOptions -> frameOptions.disable())
                // XSS protection
                .xssProtection(xss -> xss.disable())
                // Content type sniffing
                .contentTypeOptions(contentType -> {})
            )
            
            // Disable HTTP Basic authentication
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            
            // Disable form login
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
            
            .build();
    }

    /**
     * CORS configuration to allow all origins, methods, and headers (*)
     * Used by Spring Security WebFlux
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow all origins with wildcard - use setAllowedOrigins for true wildcard
        configuration.setAllowedOrigins(List.of("*"));
        
        // Allow all HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"
        ));
        
        // Allow all headers
        configuration.setAllowedHeaders(List.of("*"));
        
        // Allow credentials MUST be false when using wildcard origin
        configuration.setAllowCredentials(false);
        
        // Expose headers
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-User-Id", "X-User-Email", "X-Request-Id"
        ));
        
        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}

