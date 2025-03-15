package com.example.brokerportal.quoteservice.mapper;

import com.example.brokerportal.quoteservice.dto.PremiumDTO;
import com.example.brokerportal.quoteservice.entities.Premium;
import lombok.Data;


public class PremiumMapper {

    public static PremiumDTO toDTO(Premium premium) {
        if (premium == null) return null;

        return PremiumDTO.builder()
                .id(premium.getId())
                .basePremium(premium.getBasePremium())
                .totalPremium(premium.getTotalPremium())
                .taxes(premium.getTaxes())
                .quoteId(premium.getQuote() != null ? premium.getQuote().getId() : null)
                .quoteInsuranceId(premium.getQuoteInsurance() != null ? premium.getQuoteInsurance().getId() : null)
                .propertyInsuranceId(premium.getPropertyInsurance() != null ? premium.getPropertyInsurance().getId() : null)
                .cyberInsuranceId(premium.getCyberInsurance() != null ? premium.getCyberInsurance().getId() : null)
                .generalInsuranceId(premium.getGeneralInsurance() != null ? premium.getGeneralInsurance().getId() : null)
                .build();
    }

    public static Premium toEntity(PremiumDTO dto) {
        if (dto == null) return null;

        Premium premium = Premium.builder()
                .id(dto.getId())
                .basePremium(dto.getBasePremium())
                .totalPremium(dto.getTotalPremium())
                .taxes(dto.getTaxes())
                .build();

        // NOTE: Insurance and Quote references should be set from service layer or externally
        return premium;
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