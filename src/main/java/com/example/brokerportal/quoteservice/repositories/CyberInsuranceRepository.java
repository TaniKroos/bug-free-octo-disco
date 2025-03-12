package com.example.brokerportal.quoteservice.repositories;

import com.example.brokerportal.quoteservice.entities.CyberInsurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CyberInsuranceRepository extends JpaRepository<CyberInsurance, Long> {
    Optional<CyberInsurance> findByQuoteInsuranceId(Long quoteInsuranceId);
}