package com.example.brokerportal.quoteservice.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "employee_insurance")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmployeeInsurance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer numberOfEmployees;
    private String riskLevel;
    private String workerType;
    @OneToMany(mappedBy = "propertyInsurance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Coverage> coverages;

    @OneToMany(mappedBy = "propertyInsurance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Premium> premiums;
    @OneToOne
    @JoinColumn(name = "quote_insurance_id")
    private QuoteInsurance quoteInsurance;
}
