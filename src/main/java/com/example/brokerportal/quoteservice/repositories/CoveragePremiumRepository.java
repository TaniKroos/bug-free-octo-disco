package com.example.brokerportal.quoteservice.repositories;

import com.example.brokerportal.quoteservice.entities.CoveragePremium;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoveragePremiumRepository extends JpaRepository<CoveragePremium, Long> {
    List<CoveragePremium> findByQuoteInsuranceIdAndDeletedFalse(Long quoteInsuranceId);
}
