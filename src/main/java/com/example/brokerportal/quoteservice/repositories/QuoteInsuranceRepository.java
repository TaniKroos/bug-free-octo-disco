package com.example.brokerportal.quoteservice.repositories;

import com.example.brokerportal.quoteservice.entities.QuoteInsurance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteInsuranceRepository extends JpaRepository<QuoteInsurance, Long> {
    List<QuoteInsurance> findByQuoteId(Long quoteId);
}
