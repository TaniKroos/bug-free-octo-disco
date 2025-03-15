package com.example.brokerportal.quoteservice.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteInsuranceDTO {
    private Long id;
    private String insuranceType;
    private boolean isSelected;

    private PropertyInsuranceDTO propertyInsurance;
    private CyberInsuranceDTO cyberInsurance;
    private GeneralLiabilityInsuranceDTO generalInsurance;
}
