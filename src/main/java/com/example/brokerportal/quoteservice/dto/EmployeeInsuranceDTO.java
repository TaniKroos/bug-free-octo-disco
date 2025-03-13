package com.example.brokerportal.quoteservice.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeInsuranceDTO {
    private Long id;
    private Integer numberOfEmployees;
    private String riskLevel;
    private String workerType;


    private PremiumDTO premium;
}
