package com.example.brokerportal.quoteservice.repositories;

import com.example.brokerportal.quoteservice.entities.BusinessRiskFactor;
import com.example.brokerportal.quoteservice.enums.BusinessType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessRiskFactorRepository extends JpaRepository<BusinessRiskFactor, Long> {
    Optional<BusinessRiskFactor> findByBusinessType(BusinessType businessType);
}

