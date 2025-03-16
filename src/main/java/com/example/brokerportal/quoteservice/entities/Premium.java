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
    @JoinColumn(name = "quote_insurance_id", nullable = false)
    private QuoteInsurance quoteInsurance;
    private boolean deleted = false;
}
