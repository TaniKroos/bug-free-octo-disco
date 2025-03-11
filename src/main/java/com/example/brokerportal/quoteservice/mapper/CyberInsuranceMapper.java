package com.example.brokerportal.quoteservice.mapper;


import com.example.brokerportal.quoteservice.dto.CoverageDTO;
import com.example.brokerportal.quoteservice.dto.CyberInsuranceDTO;
import com.example.brokerportal.quoteservice.entities.Coverage;
import com.example.brokerportal.quoteservice.entities.CyberInsurance;
import com.example.brokerportal.quoteservice.entities.Premium;

import java.util.List;
import java.util.stream.Collectors;

public class CyberInsuranceMapper {

    public static CyberInsuranceDTO toDTO(CyberInsurance entity) {
        if (entity == null) return null;

        return CyberInsuranceDTO.builder()
                .id(entity.getId())
                .numberOfComputers(entity.getNumberOfComputers())
                .securityLevel(entity.getSecurityLevel())
                .dataSensitivity(entity.getDataSensitivity())
                .coverages(toCoverageDtoList(entity.getCoverages()))
                .premium(PremiumMapper.toDTO(entity.getPremium()))
                .build();
    }

    public static CyberInsurance toEntity(CyberInsuranceDTO dto) {
        if (dto == null) return null;

        CyberInsurance cyberInsurance = CyberInsurance.builder()
                .id(dto.getId())
                .numberOfComputers(dto.getNumberOfComputers())
                .securityLevel(dto.getSecurityLevel())
                .dataSensitivity(dto.getDataSensitivity())
                .build();

        if (dto.getCoverages() != null) {
            List<Coverage> coverages = dto.getCoverages().stream()
                    .map(coverageDto -> {
                        Coverage coverage = CoverageMapper.toEntity(coverageDto, null, cyberInsurance, null);
                        return coverage;
                    })
                    .collect(Collectors.toList());
            cyberInsurance.setCoverages(coverages);
        }

        if (dto.getPremium() != null) {
            Premium premium = PremiumMapper.toEntity(dto.getPremium());
            premium.setCyberInsurance(cyberInsurance);
            cyberInsurance.setPremium(premium);
        }

        return cyberInsurance;
    }

    private static List<CoverageDTO> toCoverageDtoList(List<Coverage> coverages) {
        if (coverages == null) return null;
        return coverages.stream()
                .map(CoverageMapper::toDTO)
                .collect(Collectors.toList());
    }
}
