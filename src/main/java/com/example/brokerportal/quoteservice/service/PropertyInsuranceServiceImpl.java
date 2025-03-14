package com.example.brokerportal.quoteservice.service;


import com.example.brokerportal.authservice.entities.User;
import com.example.brokerportal.authservice.service.UserService;
import com.example.brokerportal.quoteservice.dto.CoverageDTO;
import com.example.brokerportal.quoteservice.dto.PropertyInsuranceDTO;
import com.example.brokerportal.quoteservice.entities.*;
import com.example.brokerportal.quoteservice.exceptions.ResourceNotFoundException;
import com.example.brokerportal.quoteservice.mapper.CoverageMapper;
import com.example.brokerportal.quoteservice.mapper.PremiumMapper;
import com.example.brokerportal.quoteservice.mapper.PropertyInsuranceMapper;
import com.example.brokerportal.quoteservice.repositories.PropertyInsuranceRepository;
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
public class PropertyInsuranceServiceImpl implements PropertyInsuranceService{
    private final PropertyInsuranceRepository propertyInsuranceRepository;
    private final QuoteRepository quoteRepository;
    private final QuoteInsuranceRepository quoteInsuranceRepository;
    private final UserService userService;
    @Override
    @Transactional
    public PropertyInsuranceDTO createPropertyInsurance(Long quoteId, PropertyInsuranceDTO dto){
        if(dto == null){
            throw new IllegalArgumentException("PropertyInsurance cannnot  be  null");
        }

        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote with this id does not exist"));
        QuoteInsurance quoteInsurance = quote.getInsurances().stream()
                .filter(qi -> "PROPERTY".equalsIgnoreCase(qi.getInsuranceType()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("PropertyInsurance not configured with this quote"));
        if(!Boolean.TRUE.equals(quoteInsurance.isSelected())){
            throw new IllegalStateException("Property Insurancce is not selected in this quote");
        }
        authorizeBrokerAccess(quoteInsurance);
        if(quoteInsurance.getPropertyInsurance() != null){
            throw new IllegalStateException("Property Insurance with this quote already exist, Try to hit the update endpoint");

        }
        PropertyInsurance entity = PropertyInsuranceMapper.toEntity(dto,quoteInsurance);

        // Map premium
        if(dto.getPremium() != null){
            Premium premium = PremiumMapper.toEntity(dto.getPremium());
            premium.setPropertyInsurance(entity);
            entity.setPremium(premium);
        }
        quoteInsurance.setPropertyInsurance(entity);

        // Map coverage
        if(dto.getCoverages()!=null){
            List<Coverage> coverages = dto.getCoverages().stream()
                    .map(coverageDTO -> CoverageMapper.toEntity(coverageDTO,quoteInsurance))
                    .collect(Collectors.toList());
            quoteInsurance.getCoverages().addAll(coverages);
        }
        propertyInsuranceRepository.save(entity);
        quoteInsuranceRepository.save(quoteInsurance);
        return  PropertyInsuranceMapper.toDTO(entity,quoteInsurance.getCoverages());

    }
    @Override
    @Transactional
    public PropertyInsuranceDTO updatePropertyInsurance(Long quoteId, PropertyInsuranceDTO dto) {
        if (dto == null) throw new IllegalArgumentException("PropertyInsuranceDTO cannot be null");

        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote not found with ID: " + quoteId));

        QuoteInsurance quoteInsurance = quote.getInsurances().stream()
                .filter(qi -> "PROPERTY".equalsIgnoreCase(qi.getInsuranceType()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("PROPERTY insurance not configured in this quote"));

        if (!Boolean.TRUE.equals(quoteInsurance.isSelected())) {
            throw new IllegalStateException("PROPERTY Insurance is not selected in this quote");
        }

        authorizeBrokerAccess(quoteInsurance);

        PropertyInsurance entity = quoteInsurance.getPropertyInsurance();
        if (entity == null) {
            throw new ResourceNotFoundException("Property Insurance not found for this quote.");
        }

        PropertyInsuranceMapper.updateEntityFromDTO(entity, dto, quoteInsurance);

        // Update Premium
        if (dto.getPremium() != null) {
            if (entity.getPremium() != null) {
                Premium premium = entity.getPremium();
                premium.setBasePremium(dto.getPremium().getBasePremium());
                premium.setTaxes(dto.getPremium().getTaxes());
                premium.setTotalPremium(dto.getPremium().getTotalPremium());
            } else {
                Premium newPremium = PremiumMapper.toEntity(dto.getPremium());
                newPremium.setPropertyInsurance(entity);
                entity.setPremium(newPremium);
            }
        }

        // Update Coverages
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

            // Remove deleted coverages
            existingCoverages.removeIf(cov -> cov.getId() != null && !incomingIds.contains(cov.getId()));
        }

        propertyInsuranceRepository.save(entity);
        quoteInsuranceRepository.save(quoteInsurance);

        return PropertyInsuranceMapper.toDTO(entity, quoteInsurance.getCoverages());
    }

    // get property insurance
    @Override
    public PropertyInsuranceDTO getPropertyInsuranceByQuoteId(Long quoteId) {
        QuoteInsurance insurance = quoteInsuranceRepository.findByQuoteIdAndInsuranceType(quoteId, "PROPERTY")
                .orElseThrow(() -> new ResourceNotFoundException("Property Insurance not found for Quote ID: " + quoteId));

        authorizeBrokerAccess(insurance);

        PropertyInsurance propertyInsurance = insurance.getPropertyInsurance();
        if (propertyInsurance == null || Boolean.TRUE.equals(propertyInsurance.getDeleted())) {
            throw new ResourceNotFoundException("Property Insurance not available or has been soft deleted.");
        }

        return PropertyInsuranceMapper.toDTO(propertyInsurance, insurance.getCoverages());
    }

    @Override
    @Transactional
    public void softDeletePropertyInsurance(Long quoteId) {
        log.info("Soft deleting Property Insurance for Quote ID: {}", quoteId);

        QuoteInsurance insurance = quoteInsuranceRepository.findByQuoteIdAndInsuranceType(quoteId, "PROPERTY")
                .orElseThrow(() -> new ResourceNotFoundException("Property Insurance not found for Quote ID: " + quoteId));

        authorizeBrokerAccess(insurance);

        PropertyInsurance propertyInsurance = insurance.getPropertyInsurance();
        if (propertyInsurance != null) {
            propertyInsurance.setDeleted(true);
            propertyInsuranceRepository.save(propertyInsurance);
            log.info("PropertyInsurance marked as deleted. ID: {}", propertyInsurance.getId());
        }

        insurance.setSelected(false);
        quoteInsuranceRepository.save(insurance);
    }



    //  To authorize the broker
    private void authorizeBrokerAccess(QuoteInsurance quoteInsurance) {
        User user = userService.getCurrentUser();
        if (!quoteInsurance.getQuote().getBroker().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to access or modify this quote");
        }
    }

}
