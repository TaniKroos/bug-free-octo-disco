package com.example.brokerportal.quoteservice.mapper;

import com.example.brokerportal.quoteservice.dto.PremiumDTO;
import com.example.brokerportal.quoteservice.entities.Premium;

public class PremiumMapper {

    public static PremiumDTO toDTO(Premium premium) {
        if (premium == null) return null;

        return PremiumDTO.builder()
                .id(premium.getId())
                .basePremium(premium.getBasePremium())
                .totalPremium(premium.getTotalPremium())
                .taxes(premium.getTaxes())
                .quoteInsuranceId(premium.getQuoteInsurance() != null ? premium.getQuoteInsurance().getId() : null)
                .build();
    }

    public static Premium toEntity(PremiumDTO dto) {
        if (dto == null) return null;

        return Premium.builder()
                .id(dto.getId())
                .basePremium(dto.getBasePremium())
                .totalPremium(dto.getTotalPremium())
                .taxes(dto.getTaxes())
                .build(); // QuoteInsurance will be set  in service layer
    }

    public static Premium updateOrCreatePremium(PremiumDTO dto, Premium existingPremium) {
        if (dto == null) return null;

        if (existingPremium != null) {
            existingPremium.setBasePremium(dto.getBasePremium());
            existingPremium.setTaxes(dto.getTaxes());
            existingPremium.setTotalPremium(dto.getTotalPremium());
            return existingPremium;
        } else {
            return toEntity(dto);
        }
    }
}
