package com.example.brokerportal.quoteservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CyberInsuranceDTO {

    private Long id;

    @NotNull(message = "Coverage limit is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Coverage limit must be greater than 0")
    private BigDecimal coverageLimit;

    @NotNull(message = "Deductible is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Deductible must be non-negative")
    private BigDecimal deductible;

    @NotNull(message = "Prior cyber incidents info is required")
    private Boolean hasPriorCyberIncidents;

    // Only required if hasPriorCyberIncidents is true
    @Min(value = 0, message = "Number of prior incidents must be non-negative")
    private Integer numberOfPriorIncidents;

    @NotNull(message = "Firewall and antivirus usage is required")
    private Boolean usesFirewallAntivirus;

    @NotNull(message = "Data backup policy info is required")
    private Boolean hasDataBackupPolicy;

    @NotNull(message = "Customer data storage flag is required")
    private Boolean storesCustomerData;

    @Min(value = 0, message = "Data records volume must be non-negative")
    private Integer dataRecordsVolume;

    @NotNull(message = "Cybersecurity training status is required")
    private Boolean hasCybersecurityTraining;

    @Size(max = 255, message = "Payment processing methods description must not exceed 255 characters")
    private String paymentProcessingMethods;

    @Size(max = 255, message = "Cloud services description must not exceed 255 characters")
    private String cloudServicesUsed;



    private List<CoverageDTO> coverages; // Removed @Valid as it's for response only

    private PremiumDTO premium;
}
