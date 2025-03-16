package com.example.brokerportal.quoteservice.service;

public interface PremiumCalculationService {
    void calculatePremiumForQuote(Long quoteId);
    Double getPremium(Long quoteId);
}
