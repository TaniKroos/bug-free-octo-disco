package com.example.brokerportal.quoteservice.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "cyber_insurance")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CyberInsurance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "coverage_limit", precision = 15, scale = 2)
    private BigDecimal coverageLimit;

    @Column(name = "deductible", precision = 15, scale = 2)
    private BigDecimal deductible;

    @Column(name = "has_prior_cyber_incidents")
    private Boolean hasPriorCyberIncidents;

    @Column(name = "number_of_prior_incidents")
    private Integer numberOfPriorIncidents;

    @Column(name = "uses_firewall_antivirus")
    private Boolean usesFirewallAntivirus;

    @Column(name = "has_data_backup_policy")
    private Boolean hasDataBackupPolicy;

    @Column(name = "stores_customer_data")
    private Boolean storesCustomerData;

    @Column(name = "data_records_volume")
    private Integer dataRecordsVolume;

    @Column(name = "has_cybersecurity_training")
    private Boolean hasCybersecurityTraining;

    @Column(name = "payment_processing_methods", length = 100)
    private String paymentProcessingMethods;

    @Column(name = "cloud_services_used", length = 255)
    private String cloudServicesUsed;

    @Column(name = "industry_type", length = 100)
    private String industryType;


    @OneToOne(mappedBy = "cyberInsurance", cascade = CascadeType.ALL, orphanRemoval = true)
    private Premium premium;

    @OneToOne
    @JoinColumn(name = "quote_insurance_id", nullable = false)
    private QuoteInsurance quoteInsurance;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
}
