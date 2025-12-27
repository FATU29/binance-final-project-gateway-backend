package com.hpt.trading_gateway.controller;

import com.hpt.trading_gateway.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Fallback controller for circuit breaker
 * Provides graceful degradation when downstream services are unavailable
 */
@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @PostMapping("/auth")
    @GetMapping("/auth")
    public ResponseEntity<ErrorResponse> authServiceFallback() {
        log.error("Auth service is currently unavailable");
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(new ErrorResponse("Authentication service is temporarily unavailable. Please try again later."));
    }

    @PostMapping("/service")
    @GetMapping("/service")
    public ResponseEntity<ErrorResponse> serviceFallback() {
        log.error("Downstream service is currently unavailable");
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(new ErrorResponse("Service is temporarily unavailable. Please try again later."));
    }
}

