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
        PropertyInsurance propertyEntity = PropertyInsuranceMapper.toEntity(dto,quoteInsurance);

        // Map premium
        InsuranceMapperUtil.mapPremiumAndCoverages(
                propertyEntity,
                dto.getPremium(),
                dto.getCoverages(),
                quoteInsurance,
                (premium, entity) -> premium.setPropertyInsurance((PropertyInsurance) entity),
                QuoteInsurance::setPropertyInsurance
        );
        propertyInsuranceRepository.save(propertyEntity);
        quoteInsuranceRepository.save(quoteInsurance);
        return  PropertyInsuranceMapper.toDTO(propertyEntity,quoteInsurance.getCoverages());

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

        PropertyInsurance propertyEntity = quoteInsurance.getPropertyInsurance();
        if (propertyEntity == null) {
            throw new ResourceNotFoundException("Property Insurance not found for this quote.");
        }

        PropertyInsuranceMapper.updateEntityFromDTO(propertyEntity, dto, quoteInsurance);

        InsuranceMapperUtil.updatePremiumAndCoverages(
                propertyEntity,
                dto.getPremium(),
                dto.getCoverages(),
                quoteInsurance,
                (premium, entity) -> premium.setPropertyInsurance((PropertyInsurance) propertyEntity)
        );

        propertyInsuranceRepository.save(propertyEntity);
        quoteInsuranceRepository.save(quoteInsurance);

        return PropertyInsuranceMapper.toDTO(propertyEntity, quoteInsurance.getCoverages());
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
