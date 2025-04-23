package com.example.brokerportal.quoteservice.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class QuoteSearchFilterDTO {

    @Size(max = 100, message = "Client name must be at most 100 characters")
    private String clientName;

    @Size(max = 50, message = "Status must be at most 50 characters")
    private String status;

    private Long brokerId;

    @Size(max = 50, message = "Insurance type must be at most 50 characters")
    private String insuranceType;
}
