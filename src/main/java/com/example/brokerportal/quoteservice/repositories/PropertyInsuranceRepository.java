package com.example.brokerportal.quoteservice.repositories;

import com.example.brokerportal.quoteservice.entities.PropertyInsurance;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface PropertyInsuranceRepository extends JpaRepository<PropertyInsurance, Long> {
    Optional<PropertyInsurance> findByQuoteInsuranceIdAndDeletedTrue(Long id);
}
