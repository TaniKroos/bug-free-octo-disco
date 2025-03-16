package com.example.brokerportal.quoteservice.controller;

import com.example.brokerportal.quoteservice.service.PremiumCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/premium")
@RequiredArgsConstructor
public class PremiumCalculationController {

    private final PremiumCalculationService premiumCalculationService;

    @PostMapping("/calculate/{quoteId}")
    public ResponseEntity<String> calculatePremiumForQuote(@PathVariable Long quoteId) {
        premiumCalculationService.calculatePremiumForQuote(quoteId);
        return ResponseEntity.ok("Premium calculation triggered for Quote ID: " + quoteId);
    }
    @GetMapping("/calculate/{quoteId}")
    public Double getFinalPremium(@PathVariable Long quoteId){
         return  premiumCalculationService.getPremium(quoteId);
    }
}
