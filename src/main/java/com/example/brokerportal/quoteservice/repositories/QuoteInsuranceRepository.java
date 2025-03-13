package com.example.brokerportal.quoteservice.repositories;

import com.example.brokerportal.quoteservice.entities.Quote;
import com.example.brokerportal.quoteservice.entities.QuoteInsurance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuoteInsuranceRepository extends JpaRepository<QuoteInsurance, Long> {
    List<QuoteInsurance> findByQuoteId(Long quoteId);
    void deleteAllByQuote(Quote quote);
    Optional<QuoteInsurance> findByQuoteIdAndInsuranceType(Long id, String CYBER);
}
