package com.example.brokerportal.quoteservice.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PremiumDTO {
    private Long id;
    private Double basePremium;
    private Double totalPremium;
    private Double taxes;

    private Long quoteId;
    private Long quoteInsuranceId;

    private Long propertyInsuranceId;
    private Long cyberInsuranceId;
    private Long generalInsuranceId;
}


