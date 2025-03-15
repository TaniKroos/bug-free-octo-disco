package com.example.brokerportal.quoteservice.utils;

import com.example.brokerportal.quoteservice.dto.CoverageDTO;
import com.example.brokerportal.quoteservice.dto.PremiumDTO;
import com.example.brokerportal.quoteservice.entities.Coverage;
import com.example.brokerportal.quoteservice.entities.Premium;
import com.example.brokerportal.quoteservice.entities.QuoteInsurance;
import com.example.brokerportal.quoteservice.mapper.CoverageMapper;
import com.example.brokerportal.quoteservice.mapper.PremiumMapper;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class InsuranceMapperUtil {

    public static <T> void mapPremiumAndCoverages(
            T insuranceEntity,
            PremiumDTO premiumDTO,
            List<CoverageDTO> coverageDTOs,
            QuoteInsurance quoteInsurance,
            BiConsumer<Premium, T> premiumSetter,
            BiConsumer<QuoteInsurance, T> quoteInsuranceSetter
    ) {
        if (premiumDTO != null) {
            Premium premium = PremiumMapper.toEntity(premiumDTO);
            premiumSetter.accept(premium, insuranceEntity);
            if (insuranceEntity instanceof InsurancePremiumHolder) {
                ((InsurancePremiumHolder) insuranceEntity).setPremium(premium);
            }
        }

        quoteInsuranceSetter.accept(quoteInsurance, insuranceEntity);

        if (coverageDTOs != null) {
            List<Coverage> coverages = coverageDTOs.stream()
                    .map(dto -> CoverageMapper.toEntity(dto, quoteInsurance))
                    .toList();
            quoteInsurance.getCoverages().addAll(coverages);
        }
    }
    public static <T> void updatePremiumAndCoverages(
            T insuranceEntity,
            PremiumDTO premiumDTO,
            List<CoverageDTO> coverageDTOs,
            QuoteInsurance quoteInsurance,
            BiConsumer<Premium, T> premiumSetter
    ) {
        // ✅ Update Premium
        if (premiumDTO != null) {
            Premium existingPremium = null;
            if (insuranceEntity instanceof InsurancePremiumHolder) {
                existingPremium = ((InsurancePremiumHolder) insuranceEntity).getPremium();
            }

            if (existingPremium != null) {
                existingPremium.setBasePremium(premiumDTO.getBasePremium());
                existingPremium.setTaxes(premiumDTO.getTaxes());
                existingPremium.setTotalPremium(premiumDTO.getTotalPremium());
            } else {
                Premium newPremium = PremiumMapper.toEntity(premiumDTO);
                premiumSetter.accept(newPremium, insuranceEntity);
                if (insuranceEntity instanceof InsurancePremiumHolder) {
                    ((InsurancePremiumHolder) insuranceEntity).setPremium(newPremium);
                }
            }
        }

        // ✅ Update Coverages
        if (coverageDTOs != null) {
            List<Coverage> existingCoverages = quoteInsurance.getCoverages();
            List<Long> incomingIds = coverageDTOs.stream()
                    .map(CoverageDTO::getId)
                    .collect(Collectors.toList());

            for (CoverageDTO covDto : coverageDTOs) {
                if (covDto.getId() != null) {
                    existingCoverages.stream()
                            .filter(c -> c.getId().equals(covDto.getId()))
                            .findFirst()
                            .ifPresent(c -> {
                                c.setCoverageType(covDto.getCoverageType());
                                c.setCoverageAmount(covDto.getCoverageAmount());
                                c.setDescription(covDto.getDescription());
                            });
                } else {
                    Coverage newCov = CoverageMapper.toEntity(covDto, quoteInsurance);
                    existingCoverages.add(newCov);
                }
            }

            existingCoverages.removeIf(cov -> cov.getId() != null && !incomingIds.contains(cov.getId()));
        }
    }
}
