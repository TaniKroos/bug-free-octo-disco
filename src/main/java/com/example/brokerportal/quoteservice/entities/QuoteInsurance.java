package com.example.brokerportal.quoteservice.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quote_insurances")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QuoteInsurance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String insuranceType; // GENERAL, CYBER, PROPERTY, EMPLOYEE, etc.
    private boolean isSelected;

    @OneToMany(mappedBy = "quoteInsurance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Coverage> coverages = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id")
    private Quote quote;

    @OneToOne(mappedBy = "quoteInsurance", cascade = CascadeType.ALL, orphanRemoval = true)
    private PropertyInsurance propertyInsurance;

    @OneToOne(mappedBy = "quoteInsurance", cascade = CascadeType.ALL, orphanRemoval = true)
    private CyberInsurance cyberInsurance;

    @OneToOne(mappedBy = "quoteInsurance", cascade = CascadeType.ALL, orphanRemoval = true)
    private GeneralLiabilityInsurance generalInsurance;
}
