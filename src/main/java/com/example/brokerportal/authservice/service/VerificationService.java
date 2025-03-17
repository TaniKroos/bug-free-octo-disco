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
        PendingUser pendingUser = cache.get(email, PendingUser.class);

        if (pendingUser == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Verification code expired or invalid.");
        }

        if (!pendingUser.getVerificationCode().equals(code)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid verification code.");
        }

        User req = pendingUser.getUser();

        User user = new User();
        user.setEmail(req.getEmail());
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setPassword(passwordEncoder.encode(req.getPassword()));

        userRepository.save(user); // ✅ Now DB save

        // ✅ JWT + Refresh Token after verification
        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = generateAndStoreRefreshToken(user.getEmail()); // your existing logic

        cache.evict(email); // remove from cache

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User verified and registered successfully!");
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);

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
