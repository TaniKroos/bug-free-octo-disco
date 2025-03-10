package com.example.brokerportal.quoteservice.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "premiums")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Premium {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double basePremium;
    private Double totalPremium;
    private Double taxes;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id")
    private Quote quote;

    // Premium mapped to PropertyInsurance (optional)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_insurance_id")
    private PropertyInsurance propertyInsurance;

    // Premium mapped to CyberInsurance (optional)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cyber_insurance_id")
    private CyberInsurance cyberInsurance;

    // Premium mapped to EmployeeInsurance (optional)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_insurance_id")
    private EmployeeInsurance employeeInsurance;

    // Optional: Link back to QuoteInsurance (if needed globally)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_insurance_id")
    private QuoteInsurance quoteInsurance;

}
