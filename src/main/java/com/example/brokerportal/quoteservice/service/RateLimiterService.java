package com.example.brokerportal.quoteservice.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimiterService {

    private Cache<String, AtomicInteger> requestCounts;

    @PostConstruct
    public void init() {
        requestCounts = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build();
    }

    public boolean allowRequest(String userKey, int maxRequestsPerMinute) {
        AtomicInteger currentCount = requestCounts.get(userKey, k -> new AtomicInteger(0));

        if (currentCount.incrementAndGet() > maxRequestsPerMinute) {
            return false; // Rate limit exceeded
        }

        return true; // Allowed
    }
}
