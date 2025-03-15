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
import com.example.brokerportal.quoteservice.utils.InsuranceMapperUtil;
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

        GeneralLiabilityInsurance generalEntity = GeneralLiabilityInsuranceMapper.toEntity(dto, quoteInsurance);

        InsuranceMapperUtil.mapPremiumAndCoverages(
                generalEntity,
                dto.getPremium(),
                dto.getCoverages(),
                quoteInsurance,
                (premium, entity) -> premium.setGeneralInsurance((GeneralLiabilityInsurance) entity),
                QuoteInsurance::setGeneralInsurance
        );

        generalLiabilityInsuranceRepository.save(generalEntity);
        quoteInsuranceRepository.save(quoteInsurance);

        return GeneralLiabilityInsuranceMapper.toDTO(generalEntity, quoteInsurance.getCoverages());
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

        GeneralLiabilityInsurance generalEntity = quoteInsurance.getGeneralInsurance();
        if (generalEntity == null) {
            throw new ResourceNotFoundException("General Liability Insurance not found for this quote.");
        }

        GeneralLiabilityInsuranceMapper.updateEntityFromDTO(generalEntity, dto, quoteInsurance);

        InsuranceMapperUtil.updatePremiumAndCoverages(
                generalEntity,
                dto.getPremium(),
                dto.getCoverages(),
                quoteInsurance,
                (premium, entity) -> premium.setGeneralInsurance((GeneralLiabilityInsurance) entity)
        );


        generalLiabilityInsuranceRepository.save(generalEntity);
        quoteInsuranceRepository.save(quoteInsurance);

        return GeneralLiabilityInsuranceMapper.toDTO(generalEntity, quoteInsurance.getCoverages());
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
