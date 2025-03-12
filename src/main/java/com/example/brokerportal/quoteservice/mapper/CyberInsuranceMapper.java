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
                .coverageLimit(entity.getCoverageLimit())
                .deductible(entity.getDeductible())
                .hasPriorCyberIncidents(entity.getHasPriorCyberIncidents())
                .numberOfPriorIncidents(entity.getNumberOfPriorIncidents())
                .usesFirewallAntivirus(entity.getUsesFirewallAntivirus())
                .hasDataBackupPolicy(entity.getHasDataBackupPolicy())
                .storesCustomerData(entity.getStoresCustomerData())
                .dataRecordsVolume(entity.getDataRecordsVolume())
                .hasCybersecurityTraining(entity.getHasCybersecurityTraining())
                .paymentProcessingMethods(entity.getPaymentProcessingMethods())
                .cloudServicesUsed(entity.getCloudServicesUsed())
                .industryType(entity.getIndustryType())
                .coverages(toCoverageDtoList(entity.getCoverages()))              // ✅ map coverages
                .premium(PremiumMapper.toDTO(entity.getPremium()))
                .build();
    }

    public static CyberInsurance toEntity(CyberInsuranceDTO dto) {
        if (dto == null) return null;

        CyberInsurance cyberInsurance = CyberInsurance.builder()
                .id(dto.getId())
                .coverageLimit(dto.getCoverageLimit())
                .deductible(dto.getDeductible())
                .hasPriorCyberIncidents(dto.getHasPriorCyberIncidents())
                .numberOfPriorIncidents(dto.getNumberOfPriorIncidents())
                .usesFirewallAntivirus(dto.getUsesFirewallAntivirus())
                .hasDataBackupPolicy(dto.getHasDataBackupPolicy())
                .storesCustomerData(dto.getStoresCustomerData())
                .dataRecordsVolume(dto.getDataRecordsVolume())
                .hasCybersecurityTraining(dto.getHasCybersecurityTraining())
                .paymentProcessingMethods(dto.getPaymentProcessingMethods())
                .cloudServicesUsed(dto.getCloudServicesUsed())
                .industryType(dto.getIndustryType())
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
