package com.example.brokerportal.quoteservice.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PremiumDTO {
    private Long id;
    private BigDecimal baseAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String premiumType;

    private Long propertyInsuranceId;
    private Long cyberInsuranceId;
    private Long employeeInsuranceId;
    private Long quoteId;
}


