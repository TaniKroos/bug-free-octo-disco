package com.example.brokerportal.quoteservice.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "property_insurance")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PropertyInsurance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String propertyType;
    private String location;
    private Double area;
    private Double valuation;

    @OneToOne
    @JoinColumn(name = "quote_insurance_id")
    private QuoteInsurance quoteInsurance;


    @OneToMany(mappedBy = "propertyInsurance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Coverage> coverages;

    @OneToMany(mappedBy = "propertyInsurance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Premium> premiums;


}
