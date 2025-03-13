package com.example.brokerportal.quoteservice.service;


import com.example.brokerportal.authservice.entities.User;
import com.example.brokerportal.authservice.service.UserService;
import com.example.brokerportal.quoteservice.dto.CyberInsuranceDTO;
import com.example.brokerportal.quoteservice.entities.*;
import com.example.brokerportal.quoteservice.exceptions.ResourceNotFoundException;
import com.example.brokerportal.quoteservice.mapper.CoverageMapper;
import com.example.brokerportal.quoteservice.mapper.CyberInsuranceMapper;
import com.example.brokerportal.quoteservice.mapper.PremiumMapper;
import com.example.brokerportal.quoteservice.repositories.CyberInsuranceRepository;
import com.example.brokerportal.quoteservice.repositories.QuoteInsuranceRepository;
import com.example.brokerportal.quoteservice.repositories.QuoteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CyberInsuranceServiceImpl implements CyberInsuranceService {
    private final CyberInsuranceRepository cyberInsuranceRepository;
    private final QuoteRepository quoteRepository;
    private final QuoteInsuranceRepository quoteInsuranceRepository;
    private final UserService userService;

    @Override
    @Transactional
    public CyberInsuranceDTO createCyberInsurance(Long quoteId, CyberInsuranceDTO dto) {
        if (dto == null) throw new IllegalArgumentException("CyberInsuranceDTO cannot be null");

        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote with this ID not found"));

        QuoteInsurance quoteInsurance = quote.getInsurances().stream()
                .filter(qi -> "CYBER".equalsIgnoreCase(qi.getInsuranceType()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("CYBER insurance not configured in this quote"));

        if (!Boolean.TRUE.equals(quoteInsurance.isSelected())) {
            throw new IllegalStateException("CYBER Insurance not selected in this quote");
        }

        authorizeBrokerAccess(quoteInsurance);

        if (quoteInsurance.getCyberInsurance() != null) {
            throw new IllegalStateException("Cyber Insurance already exists. Use update endpoint.");
        }

        CyberInsurance entity = CyberInsuranceMapper.toEntity(dto);
        entity.setQuoteInsurance(quoteInsurance);

        if (entity.getCoverages() != null) {
            entity.getCoverages().forEach(c -> c.setCyberInsurance(entity));
        }

        if (entity.getPremium() != null) {
            entity.getPremium().setCyberInsurance(entity);
        }

        quoteInsurance.setCyberInsurance(entity);

        cyberInsuranceRepository.save(entity);
        quoteInsuranceRepository.save(quoteInsurance);

        return CyberInsuranceMapper.toDTO(entity);
    }

    @Override
    @Transactional
    public CyberInsuranceDTO updateCyberInsurance(Long quoteId, CyberInsuranceDTO dto) {
        if (dto == null) throw new IllegalArgumentException("CyberInsuranceDTO cannot be null");

        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote with this ID not found"));

        QuoteInsurance quoteInsurance = quote.getInsurances().stream()
                .filter(qi -> "CYBER".equalsIgnoreCase(qi.getInsuranceType()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("CYBER insurance not configured in this quote"));

        if (!Boolean.TRUE.equals(quoteInsurance.isSelected())) {
            throw new IllegalStateException("CYBER Insurance not selected in this quote");
        }

        authorizeBrokerAccess(quoteInsurance);

        CyberInsurance entity = quoteInsurance.getCyberInsurance();
        if (entity == null) {
            throw new ResourceNotFoundException("Cyber Insurance not found for this quote.");
        }

        CyberInsuranceMapper.updateEntityFromDTO(entity, dto);
        if (entity.getPremium() != null) {
            entity.getPremium().setCyberInsurance(entity);
        }

        cyberInsuranceRepository.save(entity);
        return CyberInsuranceMapper.toDTO(entity);
    }

    @Override
    public CyberInsuranceDTO getCyberInsuranceByQuoteId(Long quoteId) {
        QuoteInsurance insurance = quoteInsuranceRepository.findByQuoteIdAndInsuranceType(quoteId, "CYBER")
                .orElseThrow(() -> new ResourceNotFoundException("Cyber Insurance not found for Quote ID: " + quoteId));

        authorizeBrokerAccess(insurance);

        CyberInsurance cyberInsurance = insurance.getCyberInsurance();
        if (cyberInsurance == null || Boolean.TRUE.equals(cyberInsurance.getDeleted())) {
            throw new ResourceNotFoundException("Cyber Insurance not available or has been soft deleted.");
        }

        return CyberInsuranceMapper.toDTO(cyberInsurance);
    }

    @Override
    @Transactional
    public void softDeleteCyberInsurance(Long quoteId) {
        log.info("Soft deleting Cyber Insurance for Quote ID: {}", quoteId); // ✅ Log before

        QuoteInsurance insurance = quoteInsuranceRepository.findByQuoteIdAndInsuranceType(quoteId, "CYBER")
                .orElseThrow(() -> new ResourceNotFoundException("Cyber Insurance not found for Quote ID: " + quoteId));

        authorizeBrokerAccess(insurance);

        CyberInsurance cyberInsurance = insurance.getCyberInsurance();
        if (cyberInsurance != null) {
            log.info("Before soft delete, CyberInsurance ID: {}, deleted status: {}",
                    cyberInsurance.getId(), cyberInsurance.getDeleted()); // ✅ Log current status

            cyberInsurance.setDeleted(true);
            cyberInsuranceRepository.save(cyberInsurance);

            log.info("CyberInsurance marked as deleted. ID: {}", cyberInsurance.getId()); // ✅ Log after update
        } else {
            log.warn("No Cyber Insurance found for Quote ID: {}. Nothing to delete.", quoteId); // ✅ Log warning if null
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
