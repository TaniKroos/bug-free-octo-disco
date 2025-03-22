package com.example.brokerportal.quoteservice.repositories;

import com.example.brokerportal.quoteservice.entities.CoveragePremium;
import com.example.brokerportal.quoteservice.entities.QuoteInsurance;
import com.example.brokerportal.quoteservice.enums.CoverageType;
import org.checkerframework.checker.units.qual.C;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CoveragePremiumRepository extends JpaRepository<CoveragePremium, Long> {
    List<CoveragePremium> findByQuoteInsuranceIdAndDeletedFalse(Long quoteInsuranceId);
    //void deleteByQuoteInsurance(QuoteInsurance quoteInsurance);
    Optional<CoveragePremium> findByQuoteInsuranceAndCoverageTypeAndDeletedFalse(QuoteInsurance qi, CoverageType cv);
    List<CoveragePremium> findByQuoteInsurance(QuoteInsurance quoteInsurance);
}
