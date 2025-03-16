package com.example.brokerportal.quoteservice.utils;

import com.example.brokerportal.quoteservice.dto.CoverageDTO;
import com.example.brokerportal.quoteservice.dto.PremiumDTO;
import com.example.brokerportal.quoteservice.entities.Coverage;
import com.example.brokerportal.quoteservice.entities.Premium;
import com.example.brokerportal.quoteservice.entities.QuoteInsurance;
import com.example.brokerportal.quoteservice.mapper.CoverageMapper;
import com.example.brokerportal.quoteservice.mapper.PremiumMapper;

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

        if (coverageDTOs != null) {
            List<Coverage> existingCoverages = quoteInsurance.getCoverages();
            List<Long> incomingIds = coverageDTOs.stream()
                    .map(CoverageDTO::getId)
                    .collect(Collectors.toList());

            for (CoverageDTO dto : coverageDTOs) {
                if (dto.getId() != null) {
                    existingCoverages.stream()
                            .filter(c -> c.getId().equals(dto.getId()))
                            .findFirst()
                            .ifPresent(c -> {
                                c.setCoverageType(dto.getCoverageType());
                                c.setCoverageAmount(dto.getCoverageAmount());
                                c.setDescription(dto.getDescription());
                            });
                } else {
                    Coverage newCoverage = CoverageMapper.toEntity(dto, quoteInsurance);
                    existingCoverages.add(newCoverage);
                }
            }


            existingCoverages.removeIf(c -> c.getId() != null && !incomingIds.contains(c.getId()));
        }
    }
}
