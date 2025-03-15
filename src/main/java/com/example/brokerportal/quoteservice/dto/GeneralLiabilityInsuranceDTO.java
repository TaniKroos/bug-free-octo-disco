package com.example.brokerportal.quoteservice.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneralLiabilityInsuranceDTO {

    private Long id; // Optional during creation; required during update

    private BigDecimal coverageLimit;

    private BigDecimal deductible;

    private Boolean hasPriorClaims;

    private Integer numberOfClaims;

    private String descriptionOfOperations;

    private BigDecimal annualPayroll;

    private Integer businessAreaSqft;

    private String clientInteractionLevel; // LOW, MEDIUM, HIGH

    private String riskClassification;

    private Boolean additionalInsuredRequired;

    // ✅ Even though coverages are linked to QuoteInsurance, include here for frontend display
    private List<CoverageDTO> coverages;

    private PremiumDTO premium;

    // Optionally expose quoteInsuranceId if needed in response
    // private Long quoteInsuranceId;
}
