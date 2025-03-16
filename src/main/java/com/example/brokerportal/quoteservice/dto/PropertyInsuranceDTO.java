package com.example.brokerportal.quoteservice.dto;

import com.example.brokerportal.quoteservice.enums.ConstructionType;
import com.example.brokerportal.quoteservice.enums.PropertyType;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyInsuranceDTO {
    private Long id;  // Optional during creation; required during update

    private String locationAddress;

    private PropertyType propertyType;

    private Integer buildingAge;

    private ConstructionType constructionType;

    private BigDecimal propertyValue;

    private BigDecimal equipmentValue;

    private BigDecimal inventoryValue;

    private Boolean hasFireAlarmSystem;

    private Boolean hasSecuritySystem;

    private Boolean hasSprinklerSystem;

    private Boolean isCompliantWithLocalCodes;

    private BigDecimal coverageLimit;

    private BigDecimal deductible;

    private Boolean businessInterruptionCoverRequired;

    private BigDecimal businessInterruptionLimit;

    private String notes;


    private List<CoverageDTO> coverages;

    private PremiumDTO premium;

}
