package com.example.brokerportal.quoteservice.dto;

import com.example.brokerportal.quoteservice.enums.ConstructionType;
import com.example.brokerportal.quoteservice.enums.PropertyType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyInsuranceDTO {

    private Long id;  // Optional on creation

    @NotNull(message = "Property type is required")
    private PropertyType propertyType;

    @NotNull(message = "Building age is required")
    @Min(value = 0, message = "Building age must be non-negative")
    private Integer buildingAge;

    @NotNull(message = "Construction type is required")
    private ConstructionType constructionType;

    @NotNull(message = "Property value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Property value must be greater than 0")
    private BigDecimal propertyValue;

    @NotNull(message = "Equipment value is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Equipment value must be non-negative")
    private BigDecimal equipmentValue;

    @NotNull(message = "Inventory value is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Inventory value must be non-negative")
    private BigDecimal inventoryValue;

    @NotNull(message = "Fire alarm system info is required")
    private Boolean hasFireAlarmSystem;

    @NotNull(message = "Security system info is required")
    private Boolean hasSecuritySystem;

    @NotNull(message = "Sprinkler system info is required")
    private Boolean hasSprinklerSystem;

    @NotNull(message = "Compliance status is required")
    private Boolean isCompliantWithLocalCodes;

    @NotNull(message = "Coverage limit is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Coverage limit must be greater than 0")
    private BigDecimal coverageLimit;

    @NotNull(message = "Deductible is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Deductible must be non-negative")
    private BigDecimal deductible;

    @NotNull(message = "Business interruption cover flag is required")
    private Boolean businessInterruptionCoverRequired;

    // Only required if cover is selected
    @DecimalMin(value = "0.0", inclusive = true, message = "Business interruption limit must be positive")
    private BigDecimal businessInterruptionLimit;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    @Valid
    private List<@Valid CoverageDTO> coverages;

    @Valid
    private PremiumDTO premium;
}
