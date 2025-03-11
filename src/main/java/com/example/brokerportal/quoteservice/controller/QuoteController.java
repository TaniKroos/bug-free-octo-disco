package com.example.brokerportal.quoteservice.controller;


import com.example.brokerportal.quoteservice.dto.QuoteDTO;
import com.example.brokerportal.quoteservice.service.QuoteService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quotes")
public class QuoteController {
    private final QuoteService quoteService;
    @PostConstruct
    public void init() {
        System.out.println("✅ QuoteController loaded and registered");
    }
    @GetMapping("/test")
    public String test() {
        return "Quote controller is alive!";
    }
    // Create Quote
    @PostMapping
    public ResponseEntity<QuoteDTO> createQuote(@RequestBody QuoteDTO quoteDTO) {
        QuoteDTO createdQuote = quoteService.createQuote(quoteDTO);
        return ResponseEntity.ok(createdQuote);
    }
//
//    // Update Quote
//    @PutMapping("/{id}")
//    public ResponseEntity<QuoteDTO> updateQuote(@PathVariable Long id, @RequestBody QuoteDTO quoteDTO) {
//        QuoteDTO updatedQuote = quoteService.updateQuote(id, quoteDTO);
//        return ResponseEntity.ok(updatedQuote);
//    }
//
    // Get Quote by ID
    @GetMapping("/{id}")
    public ResponseEntity<QuoteDTO> getQuoteById(@PathVariable Long id) {
        QuoteDTO quoteDTO = quoteService.getQuoteById(id);
        return ResponseEntity.ok(quoteDTO);
    }
//    @GetMapping("/test")
//    public ResponseEntity<String> quoteTest() {
//        return ResponseEntity.ok("✅ QuoteController Test Hit");
//    }
//
//
//
//    // Soft Delete Quote
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteQuote(@PathVariable Long id) {
//        quoteService.softDeleteQuote(id);
//        return ResponseEntity.noContent().build();
//    }
}
