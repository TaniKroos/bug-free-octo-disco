package com.example.brokerportal.quoteservice.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoverageDTO {

    private Long id;
    private String coverageType;
    private String description;
    private BigDecimal coverageAmount;

    private Long propertyInsuranceId;
    private Long cyberInsuranceId;
    private Long employeeInsuranceId;
}
