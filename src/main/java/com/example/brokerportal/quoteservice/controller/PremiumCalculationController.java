package com.example.brokerportal.quoteservice.controller;

import com.example.brokerportal.quoteservice.service.PremiumCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/premium")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class PremiumCalculationController {

    private final PremiumCalculationService premiumCalculationService;

    @PostMapping("/calculate/{quoteId}")
    public ResponseEntity<Map<String, String>>  calculatePremiumForQuote(@PathVariable Long quoteId) {
        premiumCalculationService.calculatePremiumForQuote(quoteId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Premium calculation triggered for Quote ID: " + quoteId);
        response.put("status", "IN_PROGRESS");
        return ResponseEntity.ok(response);
//        return ResponseEntity.ok("Premium calculation triggered for Quote ID: " + quoteId);
    }
    @GetMapping("/calculate/{quoteId}")
    public Double getFinalPremium(@PathVariable Long quoteId){
         return  premiumCalculationService.getPremium(quoteId);
    }
}
