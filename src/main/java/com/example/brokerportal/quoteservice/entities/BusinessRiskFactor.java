package com.example.brokerportal.quoteservice.entities;

import com.example.brokerportal.quoteservice.enums.BusinessType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "business_risk_factor")
public class BusinessRiskFactor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private BusinessType businessType;

    private Double businessRisk;
    private Double dataExposure;
}
