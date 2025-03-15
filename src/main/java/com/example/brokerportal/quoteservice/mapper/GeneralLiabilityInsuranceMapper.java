package com.example.brokerportal.quoteservice.mapper;

import com.example.brokerportal.quoteservice.dto.GeneralLiabilityInsuranceDTO;
import com.example.brokerportal.quoteservice.dto.CoverageDTO;
import com.example.brokerportal.quoteservice.dto.PremiumDTO;
import com.example.brokerportal.quoteservice.entities.Coverage;
import com.example.brokerportal.quoteservice.entities.GeneralLiabilityInsurance;
import com.example.brokerportal.quoteservice.entities.Premium;
import com.example.brokerportal.quoteservice.entities.QuoteInsurance;

import java.util.List;
import java.util.stream.Collectors;

public class GeneralLiabilityInsuranceMapper {

    public static GeneralLiabilityInsuranceDTO toDTO(GeneralLiabilityInsurance entity, List<Coverage> quoteInsuranceCoverages) {
        if (entity == null) return null;

        return GeneralLiabilityInsuranceDTO.builder()
                .id(entity.getId())
                .coverageLimit(entity.getCoverageLimit())
                .deductible(entity.getDeductible())
                .hasPriorClaims(entity.getHasPriorClaims())
                .numberOfClaims(entity.getNumberOfClaims())
                .descriptionOfOperations(entity.getDescriptionOfOperations())
                .annualPayroll(entity.getAnnualPayroll())
                .businessAreaSqft(entity.getBusinessAreaSqft())
                .clientInteractionLevel(entity.getClientInteractionLevel())
                .riskClassification(entity.getRiskClassification())
                .additionalInsuredRequired(entity.getAdditionalInsuredRequired())
                .coverages(quoteInsuranceCoverages != null
                        ? quoteInsuranceCoverages.stream().map(CoverageMapper::toDTO).collect(Collectors.toList())
                        : null)
                .premium(PremiumMapper.toDTO(entity.getPremium()))
                .build();
    }

    public static GeneralLiabilityInsurance toEntity(GeneralLiabilityInsuranceDTO dto, QuoteInsurance quoteInsurance) {
        if (dto == null) return new GeneralLiabilityInsurance();

        GeneralLiabilityInsurance generalInsurance = GeneralLiabilityInsurance.builder()
                .id(dto.getId())
                .coverageLimit(dto.getCoverageLimit())
                .deductible(dto.getDeductible())
                .hasPriorClaims(dto.getHasPriorClaims())
                .numberOfClaims(dto.getNumberOfClaims())
                .descriptionOfOperations(dto.getDescriptionOfOperations())
                .annualPayroll(dto.getAnnualPayroll())
                .businessAreaSqft(dto.getBusinessAreaSqft())
                .clientInteractionLevel(dto.getClientInteractionLevel())
                .riskClassification(dto.getRiskClassification())
                .additionalInsuredRequired(dto.getAdditionalInsuredRequired())
                .deleted(false)
                .build();

        generalInsurance.setQuoteInsurance(quoteInsurance); // Link to QuoteInsurance

        // Premium mapping
        if (dto.getPremium() != null) {
            Premium premium = PremiumMapper.toEntity(dto.getPremium());
            premium.setGeneralInsurance(generalInsurance);
            generalInsurance.setPremium(premium);
        }

        // Coverages will be created externally in QuoteInsurance
        return generalInsurance;
    }

    public static void updateEntityFromDTO(GeneralLiabilityInsurance entity, GeneralLiabilityInsuranceDTO dto, QuoteInsurance quoteInsurance) {
        if (dto == null || entity == null) return;

        entity.setCoverageLimit(dto.getCoverageLimit());
        entity.setDeductible(dto.getDeductible());
        entity.setHasPriorClaims(dto.getHasPriorClaims());
        entity.setNumberOfClaims(dto.getNumberOfClaims());
        entity.setDescriptionOfOperations(dto.getDescriptionOfOperations());
        entity.setAnnualPayroll(dto.getAnnualPayroll());
        entity.setBusinessAreaSqft(dto.getBusinessAreaSqft());
        entity.setClientInteractionLevel(dto.getClientInteractionLevel());
        entity.setRiskClassification(dto.getRiskClassification());
        entity.setAdditionalInsuredRequired(dto.getAdditionalInsuredRequired());

        // ✅ Update coverages linked to QuoteInsurance
        CoverageMapper.updateCoveragesInQuoteInsurance(dto.getCoverages(), quoteInsurance);

        // ✅ Update or create premium
        if (dto.getPremium() != null) {
            Premium updatedOrNew = PremiumMapper.updateOrCreatePremium(dto.getPremium(), entity.getPremium());
            if (entity.getPremium() == null) {
                updatedOrNew.setGeneralInsurance(entity); // Link if premium was null
                entity.setPremium(updatedOrNew);
            }
        }
    }
}
