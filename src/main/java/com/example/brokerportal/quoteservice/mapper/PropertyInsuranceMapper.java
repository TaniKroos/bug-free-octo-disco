package com.example.brokerportal.quoteservice.mapper;

import com.example.brokerportal.quoteservice.dto.PropertyInsuranceDTO;
import com.example.brokerportal.quoteservice.entities.Coverage;
import com.example.brokerportal.quoteservice.entities.Premium;
import com.example.brokerportal.quoteservice.entities.PropertyInsurance;

import java.util.List;
import java.util.stream.Collectors;

public class PropertyInsuranceMapper {

    public static PropertyInsuranceDTO toDTO(PropertyInsurance entity) {
        if (entity == null) return null;

        return PropertyInsuranceDTO.builder()
                .id(entity.getId())
                .propertyType(entity.getPropertyType())
                .location(entity.getLocation())
                .area(entity.getArea())
                .valuation(entity.getValuation())
                .premium(entity.getPremium() != null
                        ? PremiumMapper.toDTO(entity.getPremium())
                        : null)
                .build();
    }

    public static PropertyInsurance toEntity(PropertyInsuranceDTO dto) {
        if (dto == null) return null;

        PropertyInsurance entity = PropertyInsurance.builder()
                .id(dto.getId())
                .propertyType(dto.getPropertyType())
                .location(dto.getLocation())
                .area(dto.getArea())
                .valuation(dto.getValuation())
                .build();

//        if (dto.getCoverages() != null) {
//            List<Coverage> coverages = dto.getCoverages().stream()
//                    .map(coverageDto -> CoverageMapper.toEntity(coverageDto, entity.getQuoteInsurance()))
//                    .collect(Collectors.toList());
//            entity.setCoverages(coverages);
//        }

        // ✅ Map Premium
        if (dto.getPremium() != null) {
            Premium premium = PremiumMapper.toEntity(dto.getPremium());
            premium.setPropertyInsurance(entity);
            entity.setPremium(premium);
        }

        return entity;
    }
}
