package com.example.brokerportal.quoteservice.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyInsuranceDTO {
    private Long id;
    private String propertyType;
    private String location;
    private Double area;
    private Double valuation;


    private PremiumDTO premium;
}
