package com.hpt.trading_gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Generic API response wrapper matching the auth service response format
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    private boolean success;
    
    private String message;
    
    private T data;
    
    private Instant timestamp;
    
    @JsonProperty("success")
    public boolean isSuccess() {
        return success;
    }
}

