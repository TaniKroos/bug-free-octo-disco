package com.example.brokerportal.quoteservice.mapper;

import com.example.brokerportal.quoteservice.dto.QuoteInsuranceDTO;
import com.example.brokerportal.quoteservice.entities.QuoteInsurance;

public class QuoteInsuranceMapper {

    public static QuoteInsuranceDTO toDTO(QuoteInsurance entity) {
        if (entity == null) return null;

        return QuoteInsuranceDTO.builder()
                .id(entity.getId())
                .insuranceType(entity.getInsuranceType())
                .isSelected(entity.isSelected())
                .propertyInsurance(PropertyInsuranceMapper.toDTO(entity.getPropertyInsurance(),entity.getCoverages()))
                .cyberInsurance(CyberInsuranceMapper.toDTO(entity.getCyberInsurance(), entity.getCoverages()))
                .generalInsurance(GeneralLiabilityInsuranceMapper.toDTO(entity.getGeneralInsurance(),entity.getCoverages()))
                .build();
    }

    public static QuoteInsurance toEntity(QuoteInsuranceDTO dto) {
        if (dto == null) return null;

        return QuoteInsurance.builder()
                .id(dto.getId())
                .insuranceType(dto.getInsuranceType())
                .isSelected(dto.isSelected())
                .build();
    }
}
