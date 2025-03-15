package com.example.brokerportal.quoteservice.entities;



import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "general_liability_insurance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneralLiabilityInsurance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_insurance_id", nullable = false)
    private QuoteInsurance quoteInsurance;

    @Column(name = "coverage_limit", precision = 15, scale = 2)
    private BigDecimal coverageLimit;

    @Column(name = "deductible", precision = 15, scale = 2)
    private BigDecimal deductible;

    @Column(name = "has_prior_claims")
    private Boolean hasPriorClaims;

    @Column(name = "number_of_claims")
    private Integer numberOfClaims;

    @Column(name = "description_of_operations", columnDefinition = "TEXT")
    private String descriptionOfOperations;

    @Column(name = "annual_payroll", precision = 15, scale = 2)
    private BigDecimal annualPayroll;

    @Column(name = "business_area_sqft")
    private Integer businessAreaSqft;

    @Column(name = "client_interaction_level", length = 50)
    private String clientInteractionLevel;

    @Column(name = "risk_classification", length = 100)
    private String riskClassification;

    @Column(name = "additional_insured_required")
    private Boolean additionalInsuredRequired;

    @OneToOne(mappedBy = "generalInsurance", cascade = CascadeType.ALL, orphanRemoval = true)
    private Premium premium;

    private Boolean deleted = false; // Soft delete
}
