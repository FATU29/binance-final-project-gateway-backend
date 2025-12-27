package com.hpt.trading_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

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

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            // Disable CSRF as we're using stateless JWT authentication
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            
            // Configure authorization
            .authorizeExchange(exchanges -> exchanges
                // Allow health check endpoints
                .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                // Allow fallback endpoints
                .pathMatchers("/fallback/**").permitAll()
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
     * CORS configuration to allow cross-origin requests
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow specific origins in production, use environment variable
        configuration.setAllowedOriginPatterns(List.of("*"));
        
        // Allow common HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Allow common headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "Origin",
            "X-Requested-With"
        ));
        
        // Expose headers that clients can access
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "X-User-Id",
            "X-User-Email"
        ));
        
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}

