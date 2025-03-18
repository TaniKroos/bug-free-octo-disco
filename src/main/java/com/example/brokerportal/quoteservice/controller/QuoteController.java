package com.example.brokerportal.quoteservice.controller;


import com.example.brokerportal.quoteservice.dto.PagedResponseDTO;
import com.example.brokerportal.quoteservice.dto.QuoteDTO;
import com.example.brokerportal.quoteservice.service.QuoteService;
import com.example.brokerportal.quoteservice.service.RateLimiterService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quotes")
public class QuoteController {
    private final QuoteService quoteService;
    private final RateLimiterService rateLimiterService;
    @GetMapping("/test")
    public String test() {
        return "Quote controller is alive!";
    }
    // Create Quote
    @PostMapping
    public ResponseEntity<?> createQuote(@RequestBody QuoteDTO quoteDTO) {
        String userKey = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!rateLimiterService.allowRequest(userKey, 5)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Rate limit exceeded. Try again later.");
        }
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
    public ResponseEntity<PagedResponseDTO<QuoteDTO>> getQuoteByBrokerId(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        PagedResponseDTO<QuoteDTO> ls = quoteService.getQuotesByBrokerId(page,size);
        return ResponseEntity.ok(ls);
    }

    @GetMapping("/restore/{id}")
    public ResponseEntity<String> restoreQuote(@PathVariable Long id) {
        quoteService.restoreQuote(id);
        return ResponseEntity.ok("Quote with ID " + id + " has been restored along with its insurances.");
    }

    @PostMapping("/{id}/bind")
    public ResponseEntity<String> bindQuote(@PathVariable Long id) {
        quoteService.bindQuote(id);
        return ResponseEntity.ok("Quote bound successfully!");
    }
}
