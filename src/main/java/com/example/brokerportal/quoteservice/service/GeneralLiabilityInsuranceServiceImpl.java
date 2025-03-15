package com.example.brokerportal.quoteservice.service;

import com.example.brokerportal.authservice.entities.User;
import com.example.brokerportal.authservice.service.UserService;
import com.example.brokerportal.quoteservice.dto.CoverageDTO;
import com.example.brokerportal.quoteservice.dto.GeneralLiabilityInsuranceDTO;
import com.example.brokerportal.quoteservice.entities.*;
import com.example.brokerportal.quoteservice.exceptions.ResourceNotFoundException;
import com.example.brokerportal.quoteservice.mapper.CoverageMapper;
import com.example.brokerportal.quoteservice.mapper.GeneralLiabilityInsuranceMapper;
import com.example.brokerportal.quoteservice.mapper.PremiumMapper;
import com.example.brokerportal.quoteservice.repositories.GeneralLiabilityInsuranceRepository;
import com.example.brokerportal.quoteservice.repositories.QuoteInsuranceRepository;
import com.example.brokerportal.quoteservice.repositories.QuoteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneralLiabilityInsuranceServiceImpl implements GeneralLiabilityInsuranceService {

    private final GeneralLiabilityInsuranceRepository generalLiabilityInsuranceRepository;
    private final QuoteRepository quoteRepository;
    private final QuoteInsuranceRepository quoteInsuranceRepository;
    private final UserService userService;

    @Override
    @Transactional
    public GeneralLiabilityInsuranceDTO createGeneralLiabilityInsurance(Long quoteId, GeneralLiabilityInsuranceDTO dto) {
        if (dto == null) throw new IllegalArgumentException("GeneralLiabilityInsuranceDTO cannot be null");

        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote with this ID not found"));

        QuoteInsurance quoteInsurance = quote.getInsurances().stream()
                .filter(qi -> "GENERAL".equalsIgnoreCase(qi.getInsuranceType()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("GENERAL_LIABILITY insurance not configured in this quote"));

        if (!Boolean.TRUE.equals(quoteInsurance.isSelected())) {
            throw new IllegalStateException("GENERAL_LIABILITY Insurance not selected in this quote");
        }

        authorizeBrokerAccess(quoteInsurance);

        if (quoteInsurance.getGeneralInsurance() != null && !Boolean.TRUE.equals(quoteInsurance.getGeneralInsurance().getDeleted())) {
            throw new IllegalStateException("General Liability Insurance already exists. Use update endpoint.");
        }

        GeneralLiabilityInsurance entity = GeneralLiabilityInsuranceMapper.toEntity(dto, quoteInsurance);
        entity.setQuoteInsurance(quoteInsurance);

        if (dto.getPremium() != null) {
            Premium premium = PremiumMapper.toEntity(dto.getPremium());
            premium.setGeneralInsurance(entity);
            entity.setPremium(premium);
        }

        quoteInsurance.setGeneralInsurance(entity);

        if (dto.getCoverages() != null) {
            List<Coverage> coverages = dto.getCoverages().stream()
                    .map(covDto -> CoverageMapper.toEntity(covDto, quoteInsurance))
                    .collect(Collectors.toList());
            quoteInsurance.getCoverages().addAll(coverages);
        }

        generalLiabilityInsuranceRepository.save(entity);
        quoteInsuranceRepository.save(quoteInsurance);

        return GeneralLiabilityInsuranceMapper.toDTO(entity, quoteInsurance.getCoverages());
    }

    @Override
    @Transactional
    public GeneralLiabilityInsuranceDTO updateGeneralLiabilityInsurance(Long quoteId, GeneralLiabilityInsuranceDTO dto) {
        if (dto == null) throw new IllegalArgumentException("GeneralLiabilityInsuranceDTO cannot be null");

        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote with this ID not found"));

        QuoteInsurance quoteInsurance = quote.getInsurances().stream()
                .filter(qi -> "GENERAL".equalsIgnoreCase(qi.getInsuranceType()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("GENERAL_LIABILITY insurance not configured in this quote"));

        if (!Boolean.TRUE.equals(quoteInsurance.isSelected())) {
            throw new IllegalStateException("GENERAL_LIABILITY Insurance not selected in this quote");
        }

        authorizeBrokerAccess(quoteInsurance);

        GeneralLiabilityInsurance entity = quoteInsurance.getGeneralInsurance();
        if (entity == null) {
            throw new ResourceNotFoundException("General Liability Insurance not found for this quote.");
        }

        GeneralLiabilityInsuranceMapper.updateEntityFromDTO(entity, dto, quoteInsurance);

        if (dto.getPremium() != null) {
            if (entity.getPremium() != null) {
                Premium premium = entity.getPremium();
                premium.setBasePremium(dto.getPremium().getBasePremium());
                premium.setTaxes(dto.getPremium().getTaxes());
                premium.setTotalPremium(dto.getPremium().getTotalPremium());
            } else {
                Premium newPremium = PremiumMapper.toEntity(dto.getPremium());
                newPremium.setGeneralInsurance(entity);
                entity.setPremium(newPremium);
            }
        }

        if (dto.getCoverages() != null) {
            List<Coverage> existingCoverages = quoteInsurance.getCoverages();
            List<Long> incomingIds = dto.getCoverages().stream()
                    .map(CoverageDTO::getId)
                    .collect(Collectors.toList());

            for (CoverageDTO covDto : dto.getCoverages()) {
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

        generalLiabilityInsuranceRepository.save(entity);
        quoteInsuranceRepository.save(quoteInsurance);

        return GeneralLiabilityInsuranceMapper.toDTO(entity, quoteInsurance.getCoverages());
    }

    @Override
    public GeneralLiabilityInsuranceDTO getGeneralLiabilityInsuranceByQuoteId(Long quoteId) {
        QuoteInsurance insurance = quoteInsuranceRepository.findByQuoteIdAndInsuranceType(quoteId, "GENERAL")
                .orElseThrow(() -> new ResourceNotFoundException("General Liability Insurance not found for Quote ID: " + quoteId));

        authorizeBrokerAccess(insurance);

        GeneralLiabilityInsurance entity = insurance.getGeneralInsurance();
        if (entity == null || Boolean.TRUE.equals(entity.getDeleted())) {
            throw new ResourceNotFoundException("General Liability Insurance not available or has been soft deleted.");
        }

        return GeneralLiabilityInsuranceMapper.toDTO(entity, insurance.getCoverages());
    }

    @Override
    @Transactional
    public void softDeleteGeneralLiabilityInsurance(Long quoteId) {
        log.info("Soft deleting General Liability Insurance for Quote ID: {}", quoteId);

        QuoteInsurance insurance = quoteInsuranceRepository.findByQuoteIdAndInsuranceType(quoteId, "GENERAL")
                .orElseThrow(() -> new ResourceNotFoundException("General Liability Insurance not found for Quote ID: " + quoteId));

        authorizeBrokerAccess(insurance);

        GeneralLiabilityInsurance entity = insurance.getGeneralInsurance();
        if (entity != null) {
            entity.setDeleted(true);
            generalLiabilityInsuranceRepository.save(entity);
            log.info("GeneralLiabilityInsurance marked as deleted. ID: {}", entity.getId());
        }

        insurance.setSelected(false);
        quoteInsuranceRepository.save(insurance);
    }

    private void authorizeBrokerAccess(QuoteInsurance quoteInsurance) {
        User user = userService.getCurrentUser();
        if (!quoteInsurance.getQuote().getBroker().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to access or modify this quote");
        }
    }
}
