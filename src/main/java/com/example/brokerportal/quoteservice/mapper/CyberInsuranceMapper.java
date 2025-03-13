package com.example.brokerportal.quoteservice.mapper;


import com.example.brokerportal.quoteservice.dto.CoverageDTO;
import com.example.brokerportal.quoteservice.dto.CyberInsuranceDTO;
import com.example.brokerportal.quoteservice.entities.Coverage;
import com.example.brokerportal.quoteservice.entities.CyberInsurance;
import com.example.brokerportal.quoteservice.entities.Premium;
import com.example.brokerportal.quoteservice.entities.QuoteInsurance;

import java.util.ArrayList;
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
        if (dto == null) return new CyberInsurance();

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
                .deleted(false)
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
    public static void updateEntityFromDTO(CyberInsurance entity, CyberInsuranceDTO dto) {
        if (dto == null || entity == null) return;

        entity.setCoverageLimit(dto.getCoverageLimit());
        entity.setDeductible(dto.getDeductible());
        entity.setHasPriorCyberIncidents(dto.getHasPriorCyberIncidents());
        entity.setNumberOfPriorIncidents(dto.getNumberOfPriorIncidents());
        entity.setUsesFirewallAntivirus(dto.getUsesFirewallAntivirus());
        entity.setHasDataBackupPolicy(dto.getHasDataBackupPolicy());
        entity.setStoresCustomerData(dto.getStoresCustomerData());
        entity.setDataRecordsVolume(dto.getDataRecordsVolume());
        entity.setHasCybersecurityTraining(dto.getHasCybersecurityTraining());
        entity.setPaymentProcessingMethods(dto.getPaymentProcessingMethods());
        entity.setCloudServicesUsed(dto.getCloudServicesUsed());
        entity.setIndustryType(dto.getIndustryType());

        // Update coverages
        if (dto.getCoverages() != null) {
            if (entity.getCoverages() == null) {
                entity.setCoverages(new ArrayList<>());
            } else {
                entity.getCoverages().clear();
            }
            List<Coverage> updatedCoverages = dto.getCoverages().stream()
                    .map(covDto -> {
                        Coverage coverage = CoverageMapper.toEntity(covDto, null, entity, null);
                        coverage.setCyberInsurance(entity);
                        return coverage;
                    })
                    .collect(Collectors.toList());
            // Don't replace collection — just addAll to existing one
            entity.getCoverages().addAll(updatedCoverages);
        }

        // Update premium
        if (dto.getPremium() != null) {
            if (entity.getPremium() != null) {
                // update existing premium
                Premium premium = entity.getPremium();
                premium.setBasePremium(dto.getPremium().getBasePremium());
                premium.setTaxes(dto.getPremium().getTaxes());
                premium.setTotalPremium(dto.getPremium().getTotalPremium());
            } else {
                // create new premium only if not already there
                Premium newPremium = PremiumMapper.toEntity(dto.getPremium());
                newPremium.setCyberInsurance(entity);
                entity.setPremium(newPremium);
            }
        }
    }
}
