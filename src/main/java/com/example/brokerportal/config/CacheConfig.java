package com.example.brokerportal.config;


import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean // for ratelimiting
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(1000);
    }

    @Bean // for user verification
    public CacheManager cacheManager() {
        Caffeine<Object, Object> caffeineBuilder = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(1000);

        CaffeineCacheManager cacheManager = new CaffeineCacheManager("userVerificationCache");
        cacheManager.setCaffeine(caffeineBuilder);
        return cacheManager;
    }
}