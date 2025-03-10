package com.example.brokerportal.authservice.repository;

import com.example.brokerportal.authservice.entities.Token;
import com.example.brokerportal.authservice.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    @Query("SELECT t FROM Token t WHERE t.user.email = :email")
    Optional<Token> findByUserEmail(@Param("email") String email);
//    Optional<Token> findByUser(User user);
//    void deleteByRefreshToken(String refreshToken);
//    Optional<Token> findByRefreshToken(String refreshToken);
}
