package com.example.brokerportal.quoteservice.mapper;
import com.example.brokerportal.quoteservice.dto.QuoteDTO;
import com.example.brokerportal.quoteservice.dto.QuoteInsuranceDTO;
import com.example.brokerportal.quoteservice.dto.QuoteSummaryDTO;
import com.example.brokerportal.quoteservice.entities.Quote;
import com.example.brokerportal.quoteservice.entities.QuoteInsurance;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QuoteMapper {
    public static QuoteDTO toDTO(Quote quote) {
        List<QuoteInsuranceDTO> insuranceDTOs = new ArrayList<>();
        double totalPremium = 0.0;
        boolean premiumGenerated = false;

        if (quote.getInsurances() != null) {
            insuranceDTOs = quote.getInsurances().stream()
                    .filter(QuoteInsurance::isSelected)
                    .map(qi -> {
                        QuoteInsuranceDTO dto = QuoteInsuranceMapper.toDTO(qi);

                        if ("CYBER".equalsIgnoreCase(qi.getInsuranceType()) &&
                                (qi.getCyberInsurance() == null || Boolean.TRUE.equals(qi.getCyberInsurance().getDeleted()))) {
                            dto.setCyberInsurance(null);
                        }

                        // Add similar checks for PROPERTY or GENERAL_LIABILITY here if needed

                        return dto;
                    })
                    .collect(Collectors.toList());

            // Check if all selected QuoteInsurance have non-deleted Premiums
            premiumGenerated = quote.getInsurances().stream()
                    .filter(QuoteInsurance::isSelected)
                    .allMatch(qi -> qi.getPremium() != null && !qi.getPremium().isDeleted());

            if (premiumGenerated) {
                totalPremium = quote.getInsurances().stream()
                        .filter(qi -> qi.isSelected() && qi.getPremium() != null && !qi.getPremium().isDeleted())
                        .mapToDouble(qi -> qi.getPremium().getTotalPremium() != null ? qi.getPremium().getTotalPremium() : 0.0)
                        .sum();
            }
        }

        return QuoteDTO.builder()
                .id(quote.getId())
                .status(quote.getStatus())
                .estimatedPremium(BigDecimal.valueOf(premiumGenerated ? totalPremium : 0.0))
                .createdAt(quote.getCreatedAt())
                .updatedAt(quote.getUpdatedAt())
                .startDate(quote.getStartDate())   // Add startDate
                .endDate(quote.getEndDate())       // Add endDate
                .isDeleted(quote.isDeleted())
                .client(ClientMapper.toDTO(quote.getClient()))
                .insurances(insuranceDTOs)
                .build();
    }

    public static Quote toEntity(QuoteDTO dto) {
        return Quote.builder()
                .id(dto.getId())
                .status(dto.getStatus())
                .estimatedPremium(dto.getEstimatedPremium())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .startDate(dto.getStartDate())  // Add startDate
                .endDate(dto.getEndDate())      // Add endDate
                .deleted(dto.isDeleted())
                .build();
    }


    public static QuoteSummaryDTO toSummaryDTO(Quote quote) {
        if (quote == null) {
            return null;
        }
        QuoteSummaryDTO dto = new QuoteSummaryDTO();
        dto.setQuoteId(quote.getId());
        dto.setClientName(quote.getClient() != null ? quote.getClient().getClientName() : null);
        dto.setStatus(quote.getStatus());
        dto.setCreatedAt(quote.getCreatedAt());

        // Add startDate and endDate
        dto.setStartDate(quote.getStartDate());
        dto.setEndDate(quote.getEndDate());

        // Extract selected insurance types
        List<String> insuranceTypes = quote.getInsurances() != null ?
                quote.getInsurances().stream()
                        .filter(QuoteInsurance::isSelected)
                        .map(QuoteInsurance::getInsuranceType)
                        .collect(Collectors.toList()) : new ArrayList<>();
        dto.setInsuranceTypes(insuranceTypes);

        // Sum of all premiums
        Double totalPremium = quote.getInsurances() != null ?
                quote.getInsurances().stream()
                        .filter(qi -> qi.isSelected() && qi.getPremium() != null && !qi.getPremium().isDeleted())
                        .mapToDouble(qi -> qi.getPremium().getTotalPremium() != null ? qi.getPremium().getTotalPremium() : 0.0)
                        .sum() : 0.0;
        dto.setTotalPremium(totalPremium);

        return dto;
    }
}
