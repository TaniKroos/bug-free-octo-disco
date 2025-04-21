package com.example.brokerportal.quoteservice.dto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
@Getter
@Setter
public class QuoteSummaryDTO {
    private Long quoteId;
    private String clientName;
    private String status;
    private LocalDateTime createdAt;
    private List<String> insuranceTypes;  // e.g., ["CYBER", "PROPERTY"]
    private Double totalPremium;
}
