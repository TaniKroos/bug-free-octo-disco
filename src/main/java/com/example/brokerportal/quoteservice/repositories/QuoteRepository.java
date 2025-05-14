package com.example.brokerportal.quoteservice.repositories;

import com.example.brokerportal.quoteservice.entities.Quote;
import com.example.brokerportal.quoteservice.enums.QuoteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface QuoteRepository extends JpaRepository<Quote, Long>, JpaSpecificationExecutor<Quote> {
    Page<Quote> findByBrokerIdAndDeletedFalse(Long brokerId, Pageable pageable);   // All quotes by broker
    List<Quote> findByClientId(Long clientId);   // All quotes for a client
    List<Quote> findByStatus(String status);     // Quotes by status
    List<Quote> findByDeletedFalse();
    Page<Quote> findAll(Pageable pageable);
    Optional<Quote> findByIdAndDeletedFalse(Long id);
    List<Quote> findAllByStatusNotAndStartDateLessThanAndDeletedFalse(String status, LocalDate startDate);


    List<Quote> findByBrokerIdAndDeletedTrue(Long brokerId);
   List<Quote> findByBrokerIdAndDeletedFalse(Long brokerId);
}
