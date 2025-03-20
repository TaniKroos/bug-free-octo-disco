package com.example.brokerportal.quoteservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
@AllArgsConstructor
@RequiredArgsConstructor
@Data
@Builder
public class QuoteSearchFilterDTO {

    private String clientName;
    private String status;
    private Long brokerId;
    private String insuranceType;


}
