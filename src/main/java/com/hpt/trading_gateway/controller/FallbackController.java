package com.hpt.trading_gateway.controller;

import com.hpt.trading_gateway.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Fallback controller for circuit breaker
 * Provides graceful degradation when downstream services are unavailable
 * 
 * Note: CORS headers are added manually to ensure they're present in fallback responses
 */
@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    /**
     * Create response with CORS headers to ensure browser can read the error
     */
    private ResponseEntity<ErrorResponse> createCorsResponse(String message, HttpStatus status) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "false");
        headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD");
        headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
        headers.add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        
        return ResponseEntity
            .status(status)
            .headers(headers)
            .body(new ErrorResponse(message));
    }

    @PostMapping("/auth")
    @GetMapping("/auth")
    public ResponseEntity<ErrorResponse> authServiceFallback() {
        log.error("Auth service is currently unavailable");
        return createCorsResponse(
            "Authentication service is temporarily unavailable. Please try again later.",
            HttpStatus.SERVICE_UNAVAILABLE
        );
    }

    @PostMapping("/service")
    @GetMapping("/service")
    public ResponseEntity<ErrorResponse> serviceFallback() {
        log.error("Downstream service is currently unavailable");
        return createCorsResponse(
            "Service is temporarily unavailable. Please try again later.",
            HttpStatus.SERVICE_UNAVAILABLE
        );
    }
}

