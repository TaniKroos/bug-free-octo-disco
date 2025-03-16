package com.example.brokerportal.quoteservice.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneralLiabilityInsuranceDTO {

    private Long id;

    private BigDecimal coverageLimit;

    private BigDecimal deductible;

    private Boolean hasPriorClaims;

    private Integer numberOfClaims;

    private String descriptionOfOperations;

    private BigDecimal annualPayroll;

    private Integer businessAreaSqft;

    private String clientInteractionLevel;

    private String riskClassification;

    private Boolean additionalInsuredRequired;

    // for frontend to show
    private List<CoverageDTO> coverages;

    private PremiumDTO premium;


}
