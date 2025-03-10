package com.example.brokerportal.authservice.controller;

import com.example.brokerportal.authservice.entities.User;
import com.example.brokerportal.authservice.service.AuthService;
import com.example.brokerportal.authservice.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    // Now returns an access token.
    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody User user) {
        String accessToken = authService.authenticateUser(user.getEmail(), user.getPassword());
        return ResponseEntity.ok(accessToken);
    }

    // Returns access token on successful registration.
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        return authService.register(user);
    }

    // Endpoint to validate the provided access token and return the associated user ID.
    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String tokenHeader) {
         return authService.validateTokenAndGetUserId(tokenHeader);
    }

}
