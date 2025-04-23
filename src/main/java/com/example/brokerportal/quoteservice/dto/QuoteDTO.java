package com.example.brokerportal.quoteservice.dto;

import jakarta.persistence.Column;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteDTO {

    // ID is usually auto-generated; no need to validate on input
    private Long id;

    @NotBlank(message = "Status is required")
    private String status;


    private BigDecimal estimatedPremium;

    // usually set automatically — no need for input validation
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private boolean isDeleted;
    @NotNull
    @FutureOrPresent
    private LocalDate startDate;

    private LocalDate endDate;




    @NotNull(message = "Client is required")
    @Valid // Enable nested validation inside ClientDTO
    private ClientDTO client;

    @NotEmpty(message = "At least one insurance is required")
    @Valid // Validate each QuoteInsuranceDTO inside the list
    private List<QuoteInsuranceDTO> insurances;
}
