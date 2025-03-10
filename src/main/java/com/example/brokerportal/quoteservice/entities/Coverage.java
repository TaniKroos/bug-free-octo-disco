package com.example.brokerportal.quoteservice.entities;



import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "coverages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coverage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String coverageType;
    private Double coverageAmount;

    // Coverage mapped to PropertyInsurance (optional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_insurance_id")
    private PropertyInsurance propertyInsurance;

    // Coverage mapped to CyberInsurance (optional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cyber_insurance_id")
    private CyberInsurance cyberInsurance;

    // Coverage mapped to EmployeeInsurance (optional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_insurance_id")
    private EmployeeInsurance employeeInsurance;

}

