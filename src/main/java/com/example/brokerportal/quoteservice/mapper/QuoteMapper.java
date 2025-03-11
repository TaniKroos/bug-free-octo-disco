package com.example.brokerportal.quoteservice.mapper;
import com.example.brokerportal.quoteservice.dto.QuoteDTO;
import com.example.brokerportal.quoteservice.entities.Quote;

import java.util.stream.Collectors;

public class QuoteMapper {
    public static QuoteDTO toDTO(Quote quote) {
        return QuoteDTO.builder()
                .id(quote.getId())
                .status(quote.getStatus())
                .estimatedPremium(quote.getEstimatedPremium())
                .createdAt(quote.getCreatedAt())
                .updatedAt(quote.getUpdatedAt())
                .isDeleted(quote.isDeleted())
                .brokerId(quote.getBroker() != null ? quote.getBroker().getId() : null)
                .client(ClientMapper.toDTO(quote.getClient()))
                .insurances(quote.getInsurances() != null
                        ? quote.getInsurances().stream().map(QuoteInsuranceMapper::toDTO).collect(Collectors.toList())
                        : null)
                .build();
    }

    public static Quote toEntity(QuoteDTO dto) {
        return Quote.builder()
                .id(dto.getId())
                .status(dto.getStatus())
                .estimatedPremium(dto.getEstimatedPremium())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .deleted(dto.isDeleted())
                .build();
    }
}
