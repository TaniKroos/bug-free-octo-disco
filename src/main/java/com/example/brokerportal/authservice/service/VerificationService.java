package com.example.brokerportal.authservice.service;

import com.example.brokerportal.authservice.dto.PendingUser;
import com.example.brokerportal.authservice.dto.UserDTO;
import com.example.brokerportal.authservice.entities.Token;
import com.example.brokerportal.authservice.entities.User;
import com.example.brokerportal.authservice.repository.TokenRepository;
import com.example.brokerportal.authservice.repository.UserRepository;
import com.example.brokerportal.authservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final CacheManager cacheManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenRepository tokenRepository;
    private final UserService userService;
    public ResponseEntity<?> verifyCode(String email, String code) {

        Cache cache = cacheManager.getCache("userVerificationCache");
        if (cache == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Verification service is currently unavailable.");
        }

        PendingUser pendingUser = cache.get(email, PendingUser.class);
        if (pendingUser == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Verification code expired or invalid.");
        }

        if (!pendingUser.getVerificationCode().trim().equalsIgnoreCase(code.trim())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid verification code.");
        }

        User req = pendingUser.getUser();

        // Optional: Check for duplicate email
        if (userRepository.existsByEmail(req.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already registered.");
        }
        System.out.println(req.getEmail());
        User user = new User();
        user.setEmail(req.getEmail());
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setPassword(passwordEncoder.encode(req.getPassword()));

        userRepository.save(user);

        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = generateAndStoreRefreshToken(user.getEmail());

        cache.evict(email);

        Map<String, Object> response = new HashMap<>();

        response.put("accessToken", accessToken);
        // response.put("refreshToken", refreshToken); // Uncomment if needed

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

}
