package com.example.brokerportal.quoteservice.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CyberInsuranceDTO {
    private Long id;
    private Integer numberOfComputers;
    private String securityLevel;
    private String dataSensitivity;

    private List<CoverageDTO> coverages;
    private PremiumDTO premium;
}
