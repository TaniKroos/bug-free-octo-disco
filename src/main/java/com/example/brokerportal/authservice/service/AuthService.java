package com.example.brokerportal.authservice.service;

import com.example.brokerportal.authservice.entities.Token;
import com.example.brokerportal.authservice.entities.User;
import com.example.brokerportal.authservice.repository.TokenRepository;
import com.example.brokerportal.authservice.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private UserService userService;

    public String authenticateUser(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        generateAndStoreRefreshToken(email);
        return jwtUtil.generateAccessToken(email);
    }

    public ResponseEntity<?> register(User user) {
        if (userService.isEmailExists(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email already registered");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.saveUser(user);  

        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = generateAndStoreRefreshToken(user.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully");
        response.put("accessToken", accessToken);
        // response.put("refreshToken", refreshToken); // Add if needed

        return ResponseEntity.ok(response);
    }

    private String generateAndStoreRefreshToken(String email) {
        User user = userService.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        String refreshToken = jwtUtil.generateRefreshToken(email);
        Token token = tokenRepository.findByUserEmail(email).orElse(new Token());

        token.setUser(user);
        token.setRefreshToken(refreshToken);
        token.setExpirationDate(jwtUtil.extractExpiration(refreshToken));
        tokenRepository.save(token);

        return refreshToken;
    }

    public ResponseEntity<?> validateTokenAndGetUserId(String token) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.findByEmail(email)
                .map(user -> ResponseEntity.ok(user.getId().toString()))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found"));
    }
}
