package com.example.brokerportal.authservice.controller;

import com.example.brokerportal.authservice.dto.UserDTO;
import com.example.brokerportal.authservice.entities.User;
import com.example.brokerportal.authservice.service.AuthService;
import com.example.brokerportal.authservice.service.VerificationService;
import com.example.brokerportal.authservice.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private VerificationService verificationService;
    // Now returns an access token.
    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody User user) {
        String accessToken = authService.authenticateUser(user.getEmail(), user.getPassword());
        return ResponseEntity.ok(accessToken);
    }

    // Returns access token on successful registration.
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user, BindingResult result) {
        if (result.hasErrors()) {
            // Collect error messages and return a bad request response with those messages
            List<String> errorMessages = result.getAllErrors().stream()
                    .map(ObjectError::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errorMessages);
        }
        authService.register(user);
        return ResponseEntity.ok("Verification code sent to your email");
    }


    // Endpoint to validate the provided access token and return the associated user ID.
    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String tokenHeader) {
         return authService.validateTokenAndGetUserId(tokenHeader);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyCode(@RequestParam String email, @RequestParam String code) {
        System.out.println("Hi from verify code");
        ResponseEntity<?> response = verificationService.verifyCode(email, code);
        return ResponseEntity.ok(response);
    }

}
