package com.example.brokerportal.authservice.filter;

import com.example.brokerportal.authservice.entities.Token;
import com.example.brokerportal.authservice.repository.TokenRepository;
import com.example.brokerportal.authservice.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;

    public JwtRequestFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService, TokenRepository tokenRepository) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.tokenRepository = tokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        System.out.println("🔐 JwtRequestFilter executed for path: " + request.getRequestURI());

        String path = request.getRequestURI();
        if (path.equals("/authenticate") || path.equals("/register") || path.equals("/login")) {
            chain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"No token provided. Please log in.\"}");
            return;
        }

        String token = authHeader.substring(7);
        String email;

        try {
            email = jwtUtil.extractEmail(token);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Invalid token.\"}");
            return;
        }

        try {
            String tokenType = jwtUtil.getTokenType(token);
            if (!"access".equals(tokenType)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Invalid token type. Expected access token.\"}");
                return;
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Token validation error.\"}");
            return;
        }

        boolean authenticated = false;

        if (!jwtUtil.isTokenExpired(token)) {
            if (jwtUtil.validateToken(token, email)) {
                setAuthentication(email, request);
                authenticated = true;
            }
        } else {
            Optional<Token> tokenOptional = tokenRepository.findByUserEmail(email);
            if (tokenOptional.isPresent()) {
                Token dbToken = tokenOptional.get();
                String refreshToken = dbToken.getRefreshToken();
                try {
                    String refreshTokenType = jwtUtil.getTokenType(refreshToken);
                    if ("refresh".equals(refreshTokenType) && jwtUtil.validateRefreshToken(refreshToken, email)) {
                        String newAccessToken = jwtUtil.generateAccessToken(email);
                        response.setHeader("X-New-Access-Token", newAccessToken);

                        setAuthentication(email, request);
                        authenticated = true;
                    } else {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("{\"error\": \"Invalid refresh token. Please log in again.\"}");
                        return;
                    }
                } catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\": \"Failed to refresh token. Please log in again.\"}");
                    return;
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"No refresh token found. Please log in again.\"}");
                return;
            }
        }

        // Proceed to next filter

            chain.doFilter(request, response);

    }
    private void setAuthentication(String email, HttpServletRequest request) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        System.out.println("✅ Authenticated user: " + email);
        System.out.println("Authorities: " + userDetails.getAuthorities());
        System.out.println("SecurityContext Authentication: " + SecurityContextHolder.getContext().getAuthentication());

    }

}
