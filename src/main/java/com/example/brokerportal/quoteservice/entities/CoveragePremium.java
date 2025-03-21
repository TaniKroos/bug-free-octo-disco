package com.example.brokerportal.quoteservice.entities;

import com.example.brokerportal.quoteservice.enums.CoverageType;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "coverage_premium")
public class CoveragePremium {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private CoverageType coverageType; // DATA_BREACH, RANSOMWARE, etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_insurance_id")
    private QuoteInsurance quoteInsurance;

    private BigDecimal coverageAmount;

    private BigDecimal premiumAmount;

    @Column(columnDefinition = "TEXT")
    private String calculationDetailsJson;

    private Boolean deleted = false;
}
