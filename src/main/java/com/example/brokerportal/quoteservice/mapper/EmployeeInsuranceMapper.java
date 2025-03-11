package com.example.brokerportal.quoteservice.mapper;

import com.example.brokerportal.quoteservice.dto.CoverageDTO;
import com.example.brokerportal.quoteservice.dto.EmployeeInsuranceDTO;
import com.example.brokerportal.quoteservice.entities.Coverage;
import com.example.brokerportal.quoteservice.entities.EmployeeInsurance;
import com.example.brokerportal.quoteservice.entities.Premium;

import java.util.List;
import java.util.stream.Collectors;

public class EmployeeInsuranceMapper {

    public static EmployeeInsuranceDTO toDTO(EmployeeInsurance entity) {
        if (entity == null) return null;

        return EmployeeInsuranceDTO.builder()
                .id(entity.getId())
                .numberOfEmployees(entity.getNumberOfEmployees())
                .coverages(entity.getCoverages() != null
                        ? entity.getCoverages().stream()
                        .map(CoverageMapper::toDTO)
                        .collect(Collectors.toList())
                        : null)
                .premium(entity.getPremium() != null
                        ? PremiumMapper.toDTO(entity.getPremium())
                        : null)
                .build();
    }

    public static EmployeeInsurance toEntity(EmployeeInsuranceDTO dto) {
        if (dto == null) return null;

        EmployeeInsurance entity = EmployeeInsurance.builder()
                .id(dto.getId())
                .numberOfEmployees(dto.getNumberOfEmployees())
                .build();

        // Map and set coverages
        if (dto.getCoverages() != null) {
            List<Coverage> coverages = dto.getCoverages().stream()
                    .map(coverageDto -> CoverageMapper.toEntity(coverageDto, null, null, entity))
                    .collect(Collectors.toList());
            entity.setCoverages(coverages);
        }

        // Map and set premium
        if (dto.getPremium() != null) {
            Premium premium = PremiumMapper.toEntity(dto.getPremium());
            premium.setEmployeeInsurance(entity);
            entity.setPremium(premium);
        }

        return entity;
    }
}
