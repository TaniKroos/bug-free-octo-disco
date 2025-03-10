package com.example.brokerportal.authservice.controller;

import com.example.brokerportal.authservice.dto.UserDTO;
import com.example.brokerportal.authservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserDetails() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserDTOByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
