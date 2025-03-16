package com.example.brokerportal.quoteservice.repositories;

import com.example.brokerportal.quoteservice.entities.Quote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface QuoteRepository extends JpaRepository<Quote, Long> {
    Page<Quote> findByBrokerIdAndDeletedFalse(Long brokerId, Pageable pageable);   // All quotes by broker
    List<Quote> findByClientId(Long clientId);   // All quotes for a client
    List<Quote> findByStatus(String status);     // Quotes by status
    List<Quote> findByDeletedFalse();
    Page<Quote> findAll(Pageable pageable);
    Optional<Quote> findByIdAndDeletedFalse(Long id);
    List<Quote> findByBrokerIdAndDeletedTrue(Long brokerId);
   // List<Quote> findByBrokerIdAndDeletedFalse(Long brokerId);
}
