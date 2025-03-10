package com.example.brokerportal.quoteservice.repositories;

import com.example.brokerportal.quoteservice.entities.CyberInsurance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CyberInsuranceRepository extends JpaRepository<CyberInsurance, Long> {
}