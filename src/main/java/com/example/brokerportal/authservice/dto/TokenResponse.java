package com.example.brokerportal.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {
    private Long id;
    private String token;
    private boolean isExpired;
    private boolean isRevoked;
    private Long userId;
}
