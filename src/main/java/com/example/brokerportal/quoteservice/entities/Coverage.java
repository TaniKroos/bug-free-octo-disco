package com.example.brokerportal.quoteservice.entities;



import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Objects;

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
    private BigDecimal coverageAmount;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_insurance_id")
    private QuoteInsurance quoteInsurance;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coverage coverage = (Coverage) o;
        return id != null && id.equals(coverage.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


}

