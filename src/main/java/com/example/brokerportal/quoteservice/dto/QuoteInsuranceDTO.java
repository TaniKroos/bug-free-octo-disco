package com.example.brokerportal.quoteservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteInsuranceDTO {

    // ID is backend-generated, no need to validate in request
    private Long id;

    @NotBlank(message = "Insurance type is required")
    private String insuranceType;

    @NotNull(message = "Selection status must be specified")
    private boolean isSelected;

    @Valid // Only validated if propertyInsurance is populated
    private PropertyInsuranceDTO propertyInsurance;

    @Valid
    private CyberInsuranceDTO cyberInsurance;

    @Valid
    private GeneralLiabilityInsuranceDTO generalInsurance;

    // Optional on input, but validate structure if provided
    @Valid
    private PremiumDTO premium;
}
