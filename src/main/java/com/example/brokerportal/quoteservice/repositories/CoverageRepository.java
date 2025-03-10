package com.example.brokerportal.quoteservice.repositories;


import com.example.brokerportal.quoteservice.entities.Coverage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoverageRepository extends JpaRepository<Coverage, Long> {
    List<Coverage> findByQuoteInsuranceId(Long quoteInsuranceId);
}
