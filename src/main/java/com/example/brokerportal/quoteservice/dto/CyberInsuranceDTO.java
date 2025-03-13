package com.example.brokerportal.quoteservice.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CyberInsuranceDTO {
    private Long id;

    private BigDecimal coverageLimit;
    private BigDecimal deductible;

    private Boolean hasPriorCyberIncidents;
    private Integer numberOfPriorIncidents;

    private Boolean usesFirewallAntivirus;
    private Boolean hasDataBackupPolicy;
    private Boolean storesCustomerData;
    private Integer dataRecordsVolume;

    private Boolean hasCybersecurityTraining;

    private String paymentProcessingMethods;
    private String cloudServicesUsed;
    private String industryType;

    // ✅ Even though coverages are linked to QuoteInsurance, you show them here for frontend display
    private List<CoverageDTO> coverages;

    private PremiumDTO premium;
}
