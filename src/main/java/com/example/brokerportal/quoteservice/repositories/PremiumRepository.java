package com.example.brokerportal.quoteservice.repositories;

import com.example.brokerportal.quoteservice.entities.Premium;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PremiumRepository extends JpaRepository<Premium, Long> {
    List<Premium> findByQuoteInsurance_Quote_Id(Long quoteId);
}