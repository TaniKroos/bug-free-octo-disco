package com.example.brokerportal.quoteservice.mapper;

import com.example.brokerportal.quoteservice.dto.CoverageDTO;
import com.example.brokerportal.quoteservice.dto.CyberInsuranceDTO;
import com.example.brokerportal.quoteservice.dto.PremiumDTO;
import com.example.brokerportal.quoteservice.entities.Coverage;
import com.example.brokerportal.quoteservice.entities.CyberInsurance;
import com.example.brokerportal.quoteservice.entities.Premium;
import com.example.brokerportal.quoteservice.entities.QuoteInsurance;

import java.util.*;
import java.util.stream.Collectors;

public class CyberInsuranceMapper {

    public static CyberInsuranceDTO toDTO(CyberInsurance entity, List<Coverage> quoteInsuranceCoverages) {
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
                .coverages(quoteInsuranceCoverages != null
                        ? quoteInsuranceCoverages.stream()
                        .map(CoverageMapper::toDTO)
                        .collect(Collectors.toList())
                        : null)
                .premium(PremiumMapper.toDTO(entity.getPremium()))
                .build();
    }


    public static CyberInsurance toEntity(CyberInsuranceDTO dto, QuoteInsurance quoteInsurance) {
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

        cyberInsurance.setQuoteInsurance(quoteInsurance); // ✅ establish link

        // Premium Mapping
        if (dto.getPremium() != null) {
            Premium premium = PremiumMapper.toEntity(dto.getPremium());
            premium.setCyberInsurance(cyberInsurance);
            cyberInsurance.setPremium(premium);
        }

        //  Coverages will be created and added directly to quoteInsurance externally
        return cyberInsurance;
    }

//    private static List<CoverageDTO> toCoverageDtoList(List<Coverage> coverages) {
//        if (coverages == null) return null;
//        return coverages.stream()
//                .map(CoverageMapper::toDTO)
//                .collect(Collectors.toList());
//    }

    public static void updateEntityFromDTO(CyberInsurance entity, CyberInsuranceDTO dto, QuoteInsurance quoteInsurance) {
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

        // ✅ Update Coverages in QuoteInsurance
        CoverageMapper.updateCoveragesInQuoteInsurance(dto.getCoverages(), quoteInsurance);


        // ✅ Premium update
        if (dto.getPremium() != null) {
            Premium updatedOrNew = PremiumMapper.updateOrCreatePremium(dto.getPremium(), entity.getPremium());
            if (entity.getPremium() == null) {
                updatedOrNew.setCyberInsurance(entity); // link only here!
                entity.setPremium(updatedOrNew);
            }
        }
    }
}
