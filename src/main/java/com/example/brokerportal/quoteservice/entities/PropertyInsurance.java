package com.example.brokerportal.quoteservice.entities;

import com.example.brokerportal.quoteservice.enums.ConstructionType;
import com.example.brokerportal.quoteservice.enums.PropertyType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "property_insurance")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PropertyInsurance {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;  // You can switch to Long if your project uses Long

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_insurance_id", nullable = false)
    private QuoteInsurance quoteInsurance;

    private String locationAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "property_type", length = 50)
    private PropertyType propertyType;

    private Integer buildingAge;

    @Enumerated(EnumType.STRING)
    @Column(name = "construction_type", length = 50)
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

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToOne(mappedBy = "propertyInsurance", cascade = CascadeType.ALL, orphanRemoval = true)
    private Premium premium;

    private Boolean deleted = false; // Soft delete


}
