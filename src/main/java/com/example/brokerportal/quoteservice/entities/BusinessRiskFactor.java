package com.example.brokerportal.quoteservice.entities;

import com.example.brokerportal.quoteservice.enums.BusinessType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
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

    // ===== Common Risk Factors =====
    private Double baseTaxRate;               // e.g., 0.18 or 0.20, depending on region or regulation
    private Double locationRiskFactor;        // Location-specific risk multiplier
    private Double floorRiskFactor;           // Used for property: lower floors might have flood risk

    // ===== Cyber Insurance Specific =====
    private Double businessRisk;              // Cyber-related business risk (e.g., exposure to attacks)
    private Double dataExposure;              // Risk from handling sensitive data
    private Double cyberComplianceFactor;     // A multiplier based on compliance with cyber standards

    // ===== Property Insurance Specific =====
    private Double constructionTypeFactor;    // Multiplier based on material used
    private Double theftProtectionFactor;     // How well the property is protected against theft
    private Double occupancyRiskFactor;       // Residential vs. commercial etc.

    // ===== General Liability Insurance Specific =====
    private Double liabilityExposureFactor;   // Risk based on liability type (e.g. public, product)
    private Double employeeRiskFactor;        // Based on number/type of employees
    private Double clientInteractionFactor;   // High if frequent public/client interaction

}
