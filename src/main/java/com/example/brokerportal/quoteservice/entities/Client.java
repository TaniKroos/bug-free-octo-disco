package com.example.brokerportal.quoteservice.entities;

import com.example.brokerportal.authservice.entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "clients")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String clientName;
    private String businessType;

    private String contactNumber;
    private String email;
    private String address;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "broker_id")
    private User broker;
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private List<Quote> quotes;


}
