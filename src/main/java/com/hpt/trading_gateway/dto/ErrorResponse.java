package com.hpt.trading_gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Error response DTO for gateway errors
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    private boolean success = false;
    
    private String message;
    
    private Instant timestamp;
    
    public ErrorResponse(String message) {
        this.message = message;
        this.timestamp = Instant.now();
    }
}

