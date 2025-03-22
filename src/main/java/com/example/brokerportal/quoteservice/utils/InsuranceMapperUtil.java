package com.example.brokerportal.quoteservice.utils;

import com.example.brokerportal.quoteservice.dto.CoverageDTO;
import com.example.brokerportal.quoteservice.dto.PremiumDTO;
import com.example.brokerportal.quoteservice.entities.Coverage;
import com.example.brokerportal.quoteservice.entities.Premium;
import com.example.brokerportal.quoteservice.entities.QuoteInsurance;
import com.example.brokerportal.quoteservice.mapper.CoverageMapper;
import com.example.brokerportal.quoteservice.mapper.PremiumMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InsuranceMapperUtil {

    public static void mapPremiumAndCoveragesToQuoteInsurance(
            PremiumDTO premiumDTO,
            List<CoverageDTO> coverageDTOs,
            QuoteInsurance quoteInsurance
    ) {

        Premium premium = (premiumDTO != null)
                ? PremiumMapper.toEntity(premiumDTO)
                : new Premium(); // ← Blank Premium

        premium.setQuoteInsurance(quoteInsurance);
        quoteInsurance.setPremium(premium);


        if (coverageDTOs != null) {
            quoteInsurance.getCoverages().clear();
            List<Coverage> coverages = coverageDTOs.stream()
                    .map(dto -> CoverageMapper.toEntity(dto, quoteInsurance))
                    .collect(Collectors.toList());
            quoteInsurance.getCoverages().addAll(coverages);
        }
    }

    public static void updatePremiumAndCoveragesOnQuoteInsurance(
            PremiumDTO premiumDTO,
            List<CoverageDTO> coverageDTOs,
            QuoteInsurance quoteInsurance
    ) {
        // Update or create Premium
        Premium existingPremium = quoteInsurance.getPremium();
        if (existingPremium == null) {
            existingPremium = new Premium();
            existingPremium.setQuoteInsurance(quoteInsurance);
            quoteInsurance.setPremium(existingPremium);
        }

        if (premiumDTO != null) {
            existingPremium.setBasePremium(premiumDTO.getBasePremium());
            existingPremium.setTaxes(premiumDTO.getTaxes());
            existingPremium.setTotalPremium(premiumDTO.getTotalPremium());
        }

        // Update Coverages
        if (coverageDTOs != null) {
            List<Coverage> updatedCoverages = new ArrayList<>();

            for (CoverageDTO dto : coverageDTOs) {
                if (dto.getId() != null) {
                    // Try to find matching coverage in existing list
                    Coverage existing = quoteInsurance.getCoverages().stream()
                            .filter(c -> c.getId().equals(dto.getId()))
                            .findFirst()
                            .orElse(null);

                    if (existing != null) {
                        existing.setCoverageType(dto.getCoverageType());
                        existing.setCoverageAmount(dto.getCoverageAmount());
                        existing.setDescription(dto.getDescription());
                        updatedCoverages.add(existing);
                    } else {
                        // ID exists in DTO but not in DB — might be stale ID, create new
                        Coverage newCoverage = CoverageMapper.toEntity(dto, quoteInsurance);
                        updatedCoverages.add(newCoverage);
                    }
                } else {
                    // New coverage
                    Coverage newCoverage = CoverageMapper.toEntity(dto, quoteInsurance);
                    updatedCoverages.add(newCoverage);
                }
            }

            // Replace the coverages list completely
            quoteInsurance.getCoverages().clear(); // this removes existing associations
            quoteInsurance.getCoverages().addAll(updatedCoverages);
        }
    }

}
