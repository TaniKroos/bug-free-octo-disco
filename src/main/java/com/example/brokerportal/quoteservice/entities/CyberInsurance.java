package com.example.brokerportal.quoteservice.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "cyber_insurance")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CyberInsurance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer numberOfComputers;
    private String securityLevel;
    private String dataSensitivity;
    @OneToMany(mappedBy = "propertyInsurance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Coverage> coverages;

    @OneToMany(mappedBy = "propertyInsurance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Premium> premiums;
    @OneToOne
    @JoinColumn(name = "quote_insurance_id")
    private QuoteInsurance quoteInsurance;
}
