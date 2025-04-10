package com.example.brokerportal.quoteservice.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuoteSummaryDTO {
    private Long quoteId;
    private String clientName;
    private String status;
    private LocalDateTime createdAt;
    private List<String> insuranceTypes;  // e.g., ["CYBER", "PROPERTY"]
    private Double totalPremium;
}
