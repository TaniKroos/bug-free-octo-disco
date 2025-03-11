package com.example.brokerportal.quoteservice.entities;

import com.example.brokerportal.quoteservice.dto.QuoteDTO;
import com.example.brokerportal.quoteservice.enums.InsuranceType;
import jakarta.persistence.*;
import lombok.*;
import com.example.brokerportal.authservice.entities.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "quotes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Quote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String status;
    private BigDecimal estimatedPremium;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

//    @Enumerated(EnumType.STRING)
//    @Column(name ="insurance_type")
//    private InsuranceType insuranceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "broker_id")
    private User broker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuoteInsurance> insurances;

    @Column(name = "is_deleted")
    private boolean deleted = false;




}
