package com.example.brokerportal.quoteservice.repositories;

import com.example.brokerportal.quoteservice.entities.Quote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuoteRepository extends JpaRepository<Quote, Long> {
    List<Quote> findByBrokerId(Long brokerId);   // All quotes by broker
    List<Quote> findByClientId(Long clientId);   // All quotes for a client
    List<Quote> findByStatus(String status);     // Quotes by status
    List<Quote> findByDeletedFalse();

    Optional<Quote> findByIdAndDeletedFalse(Long id);

    List<Quote> findByBrokerIdAndDeletedFalse(Long brokerId);
}
