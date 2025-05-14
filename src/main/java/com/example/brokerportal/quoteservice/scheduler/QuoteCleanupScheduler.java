package com.example.brokerportal.quoteservice.scheduler;

import com.example.brokerportal.quoteservice.entities.Quote;
import com.example.brokerportal.quoteservice.enums.QuoteStatus;
import com.example.brokerportal.quoteservice.repositories.QuoteRepository;
import com.example.brokerportal.quoteservice.service.QuoteService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class QuoteCleanupScheduler {

    private final QuoteRepository quoteRepository;
    private final QuoteService quoteService;

    public QuoteCleanupScheduler(QuoteRepository quoteRepository, QuoteService quoteService) {
        this.quoteRepository = quoteRepository;
        this.quoteService = quoteService;
    }

    // Run every day at midnight
    @Scheduled(cron = "0 18 16 * * *", zone = "UTC")
    public void autoDeleteUnboundQuotes() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);


        List<Quote> quotesToDelete = quoteRepository.findAllByStatusNotAndStartDateLessThanAndDeletedFalse("BOUND", tomorrow);
        System.out.println("Quotes selected for deletion: " + quotesToDelete.size());
        for (Quote quote : quotesToDelete) {
          softDeleteQuoteInternal(quote.getId()); //
        }

        System.out.println("Auto-deleted unbound quotes for start date: " + tomorrow);
    }
    private void softDeleteQuoteInternal(Long quoteId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new EntityNotFoundException("Quote not found"));
        quote.setStatus(QuoteStatus.DELETED.name());
        quote.setDeleted(true);
        quote.setUpdatedAt(LocalDateTime.now());
        quoteRepository.save(quote);
    }
}

