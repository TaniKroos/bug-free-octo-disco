package com.example.brokerportal.quoteservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogDTO {
    private Long id;
    private String action;
    private String changedDetails;
    private LocalDateTime timestamp;
    private Long quoteId;
}
