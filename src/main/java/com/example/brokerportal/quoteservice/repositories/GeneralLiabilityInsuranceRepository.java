package com.example.brokerportal.quoteservice.repositories;

import com.example.brokerportal.quoteservice.entities.GeneralLiabilityInsurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GeneralLiabilityInsuranceRepository extends JpaRepository<GeneralLiabilityInsurance, Long> {

    Optional<GeneralLiabilityInsurance> findByQuoteInsuranceIdAndDeletedFalse(Long quoteInsuranceId);
}
