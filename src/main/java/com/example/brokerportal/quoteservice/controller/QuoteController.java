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

    // Update Quote
    @PutMapping("/{id}")
    public ResponseEntity<QuoteDTO> updateQuote(@PathVariable Long id, @RequestBody QuoteDTO quoteDTO) {
        QuoteDTO updatedQuote = quoteService.updateQuote(id, quoteDTO);
        return ResponseEntity.ok(updatedQuote);
    }
    @GetMapping("/all-deleted")
    public ResponseEntity<List<QuoteDTO>> getDeletedQuotesByClientName( ) {
        List<QuoteDTO> quotes = quoteService.findByBrokerIdAndDeletedTrue();
        return ResponseEntity.ok(quotes);
    }
     // Get Quote by ID
    @GetMapping("/{id}")
    public ResponseEntity<QuoteDTO> getQuoteById(@PathVariable Long id) {
        QuoteDTO quoteDTO = quoteService.getQuoteById(id);
        return ResponseEntity.ok(quoteDTO);
    }

    // Soft Delete Quote
    @DeleteMapping("/{id}")
    public ResponseEntity<String> softDeleteQuote(@PathVariable Long id) {
        quoteService.softDeleteQuote(id);
        return ResponseEntity.ok("Quote with ID " + id + " has been soft deleted along with its insurances.");
    }
    @GetMapping("/by-broker")
    public ResponseEntity<List<QuoteDTO>> getQuoteByBrokerId(){
        List<QuoteDTO> ls = quoteService.getQuotesByBrokerId();
        return ResponseEntity.ok(ls);
    }
}
