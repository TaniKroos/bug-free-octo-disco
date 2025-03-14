package com.example.brokerportal.quoteservice.mapper;

import com.example.brokerportal.quoteservice.dto.PropertyInsuranceDTO;
import com.example.brokerportal.quoteservice.dto.PremiumDTO;
import com.example.brokerportal.quoteservice.dto.CoverageDTO;
import com.example.brokerportal.quoteservice.entities.Coverage;
import com.example.brokerportal.quoteservice.entities.Premium;
import com.example.brokerportal.quoteservice.entities.PropertyInsurance;
import com.example.brokerportal.quoteservice.entities.QuoteInsurance;

import java.util.List;
import java.util.stream.Collectors;

public class PropertyInsuranceMapper {

    public static PropertyInsuranceDTO toDTO(PropertyInsurance entity, List<Coverage> quoteInsuranceCoverages) {
        if (entity == null) return null;

        return PropertyInsuranceDTO.builder()
                .id(entity.getId())
                .locationAddress(entity.getLocationAddress())
                .propertyType(entity.getPropertyType())
                .buildingAge(entity.getBuildingAge())
                .constructionType(entity.getConstructionType())
                .propertyValue(entity.getPropertyValue())
                .equipmentValue(entity.getEquipmentValue())
                .inventoryValue(entity.getInventoryValue())
                .hasFireAlarmSystem(entity.getHasFireAlarmSystem())
                .hasSecuritySystem(entity.getHasSecuritySystem())
                .hasSprinklerSystem(entity.getHasSprinklerSystem())
                .isCompliantWithLocalCodes(entity.getIsCompliantWithLocalCodes())
                .coverageLimit(entity.getCoverageLimit())
                .deductible(entity.getDeductible())
                .businessInterruptionCoverRequired(entity.getBusinessInterruptionCoverRequired())
                .businessInterruptionLimit(entity.getBusinessInterruptionLimit())
                .notes(entity.getNotes())
                .coverages(quoteInsuranceCoverages != null
                        ? quoteInsuranceCoverages.stream()
                        .map(CoverageMapper::toDTO)
                        .collect(Collectors.toList())
                        : null)
                .premium(PremiumMapper.toDTO(entity.getPremium()))
                .build();
    }

    public static PropertyInsurance toEntity(PropertyInsuranceDTO dto, QuoteInsurance quoteInsurance) {
        if (dto == null) return new PropertyInsurance();

        PropertyInsurance propertyInsurance = PropertyInsurance.builder()
                .id(dto.getId())
                .locationAddress(dto.getLocationAddress())
                .propertyType(dto.getPropertyType())
                .buildingAge(dto.getBuildingAge())
                .constructionType(dto.getConstructionType())
                .propertyValue(dto.getPropertyValue())
                .equipmentValue(dto.getEquipmentValue())
                .inventoryValue(dto.getInventoryValue())
                .hasFireAlarmSystem(dto.getHasFireAlarmSystem())
                .hasSecuritySystem(dto.getHasSecuritySystem())
                .hasSprinklerSystem(dto.getHasSprinklerSystem())
                .isCompliantWithLocalCodes(dto.getIsCompliantWithLocalCodes())
                .coverageLimit(dto.getCoverageLimit())
                .deductible(dto.getDeductible())
                .businessInterruptionCoverRequired(dto.getBusinessInterruptionCoverRequired())
                .businessInterruptionLimit(dto.getBusinessInterruptionLimit())
                .notes(dto.getNotes())
                .deleted(false)
                .build();

        propertyInsurance.setQuoteInsurance(quoteInsurance); // Link it properly

        // Premium
        if (dto.getPremium() != null) {
            Premium premium = PremiumMapper.toEntity(dto.getPremium());
            premium.setPropertyInsurance(propertyInsurance);
            propertyInsurance.setPremium(premium);
        }

        // Coverages handled externally through CoverageMapper
        return propertyInsurance;
    }

    public static void updateEntityFromDTO(PropertyInsurance entity, PropertyInsuranceDTO dto, QuoteInsurance quoteInsurance) {
        if (dto == null || entity == null) return;

        entity.setLocationAddress(dto.getLocationAddress());
        entity.setPropertyType(dto.getPropertyType());
        entity.setBuildingAge(dto.getBuildingAge());
        entity.setConstructionType(dto.getConstructionType());
        entity.setPropertyValue(dto.getPropertyValue());
        entity.setEquipmentValue(dto.getEquipmentValue());
        entity.setInventoryValue(dto.getInventoryValue());
        entity.setHasFireAlarmSystem(dto.getHasFireAlarmSystem());
        entity.setHasSecuritySystem(dto.getHasSecuritySystem());
        entity.setHasSprinklerSystem(dto.getHasSprinklerSystem());
        entity.setIsCompliantWithLocalCodes(dto.getIsCompliantWithLocalCodes());
        entity.setCoverageLimit(dto.getCoverageLimit());
        entity.setDeductible(dto.getDeductible());
        entity.setBusinessInterruptionCoverRequired(dto.getBusinessInterruptionCoverRequired());
        entity.setBusinessInterruptionLimit(dto.getBusinessInterruptionLimit());
        entity.setNotes(dto.getNotes());

        // ✅ Update Coverages in QuoteInsurance
        CoverageMapper.updateCoveragesInQuoteInsurance(dto.getCoverages(), quoteInsurance);

        // ✅ Update or Create Premium
        if (dto.getPremium() != null) {
            Premium updatedOrNew = PremiumMapper.updateOrCreatePremium(dto.getPremium(), entity.getPremium());
            if (entity.getPremium() == null) {
                updatedOrNew.setPropertyInsurance(entity); // Only set if new
                entity.setPremium(updatedOrNew);
            }
        }
    }
}
