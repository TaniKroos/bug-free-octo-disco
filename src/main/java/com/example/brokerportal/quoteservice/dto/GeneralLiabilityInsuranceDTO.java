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
public class GeneralLiabilityInsuranceDTO {

    private Long id;

    @NotNull(message = "Coverage limit is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Coverage limit must be greater than 0")
    private BigDecimal coverageLimit;

    @NotNull(message = "Deductible is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Deductible must be non-negative")
    private BigDecimal deductible;

    @NotNull(message = "Prior claims info is required")
    private Boolean hasPriorClaims;

    // Only required if hasPriorClaims is true (can use custom validator if needed)
    @Min(value = 0, message = "Number of claims must be non-negative")
    private Integer numberOfClaims;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String descriptionOfOperations;

    @NotNull(message = "Annual payroll is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Payroll must be non-negative")
    private BigDecimal annualPayroll;

    @NotNull(message = "Business area is required")
    @Min(value = 0, message = "Business area must be non-negative")
    private Integer businessAreaSqft;

    @NotBlank(message = "Client interaction level is required")
    private String clientInteractionLevel;

    @NotBlank(message = "Risk classification is required")
    private String riskClassification;

    @NotNull(message = "Additional insured flag is required")
    private Boolean additionalInsuredRequired;

    @Valid
    private List<@Valid CoverageDTO> coverages;

    @Valid
    private PremiumDTO premium;
}
