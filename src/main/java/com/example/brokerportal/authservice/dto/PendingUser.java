package com.example.brokerportal.authservice.dto;


import com.example.brokerportal.authservice.entities.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class PendingUser {
    private User user;
    private String verificationCode;
}
