package com.hpt.trading_gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * User data DTO matching the auth service /me endpoint response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserData {
    
    private String id;
    
    private String email;
    
    private String firstName;
    
    private String lastName;
    
    private boolean enabled;
    
    private Instant createdAt;
    
    private Instant updatedAt;
}

