package com.example.brokerportal.quoteservice.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteDTO {
    private Long id;
    private String status;
    private BigDecimal estimatedPremium;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;

    private Long brokerId;
    private ClientDTO client;
    private List<QuoteInsuranceDTO> insurances;
}
