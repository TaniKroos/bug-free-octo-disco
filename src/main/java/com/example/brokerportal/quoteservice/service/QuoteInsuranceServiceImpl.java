package com.example.brokerportal.quoteservice.service;

import com.example.brokerportal.quoteservice.dto.QuoteInsuranceDTO;
import com.example.brokerportal.quoteservice.entities.CyberInsurance;
import com.example.brokerportal.quoteservice.entities.Quote;
import com.example.brokerportal.quoteservice.entities.QuoteInsurance;
import com.example.brokerportal.quoteservice.mapper.CyberInsuranceMapper;
import com.example.brokerportal.quoteservice.mapper.EmployeeInsuranceMapper;
import com.example.brokerportal.quoteservice.mapper.PropertyInsuranceMapper;
import com.example.brokerportal.quoteservice.mapper.QuoteInsuranceMapper;
import com.example.brokerportal.quoteservice.repositories.CyberInsuranceRepository;
import com.example.brokerportal.quoteservice.repositories.QuoteInsuranceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuoteInsuranceServiceImpl implements QuoteInsuranceService {

    private final QuoteInsuranceRepository quoteInsuranceRepository;
    private final CyberInsuranceRepository cyberInsuranceRepository;

    @Override
    @Transactional
    public void deleteInsurancesByQuote(Quote quote) {
        quoteInsuranceRepository.deleteAllByQuote(quote);
    }

    @Override
    @Transactional
    public List<QuoteInsuranceDTO> mapAndAttachInsurancesToQuote(Quote quote, List<QuoteInsuranceDTO> quoteInsuranceDTOS) {
        List<QuoteInsurance> insuranceEntities = new ArrayList<>();

        for (QuoteInsuranceDTO dto : quoteInsuranceDTOS) {
            QuoteInsurance insuranceEntity = QuoteInsuranceMapper.toEntity(dto);
            insuranceEntity.setQuote(quote);

            // 🛑 Do NOT populate insuranceEntity.setCyberInsurance() or others here
            // Let them stay null — they’ll be added later via respective service

            insuranceEntities.add(insuranceEntity);
        }

        if (quote.getInsurances() == null) {
            quote.setInsurances(new ArrayList<>());
        } else {
            quote.getInsurances().clear();
        }

        quote.getInsurances().addAll(insuranceEntities);
        List<QuoteInsurance> savedEntities = quoteInsuranceRepository.saveAll(insuranceEntities);

        return savedEntities.stream()
                .map(QuoteInsuranceMapper::toDTO)
                .toList();
    }

    // 🔥 CyberInsurance Logic Fix: Now works with proper QuoteInsurance ID
    private void handleCyberInsuranceLogic(QuoteInsuranceDTO dto, QuoteInsurance insuranceEntity) {
        if (!"CYBER".equalsIgnoreCase(dto.getInsuranceType())) return;

        if (!dto.isSelected()) {
            if (insuranceEntity.getCyberInsurance() != null) {
                insuranceEntity.getCyberInsurance().setDeleted(true);
                cyberInsuranceRepository.save(insuranceEntity.getCyberInsurance());
            }
            insuranceEntity.setCyberInsurance(null);
        } else {
            // Try restoring existing soft-deleted CyberInsurance linked to this QuoteInsurance
            CyberInsurance existing = cyberInsuranceRepository
                    .findByQuoteInsuranceIdAndDeletedTrue(insuranceEntity.getId())
                    .orElse(null);

            if (existing != null) {
                existing.setDeleted(false);
                existing.setQuoteInsurance(insuranceEntity);
                cyberInsuranceRepository.save(existing);
                insuranceEntity.setCyberInsurance(existing);
            } else {
                // 🔥 FIX: Updated to match new CyberInsuranceMapper.toEntity(dto, quoteInsurance)
                CyberInsurance newCyber = CyberInsuranceMapper.toEntity(dto.getCyberInsurance(), insuranceEntity);
                insuranceEntity.setCyberInsurance(newCyber);
            }
        }
    }

    private void handlePropertyInsuranceLogic(QuoteInsuranceDTO dto, QuoteInsurance insuranceEntity) {
        if (dto.getPropertyInsurance() != null) {
            insuranceEntity.setPropertyInsurance(
                    PropertyInsuranceMapper.toEntity(dto.getPropertyInsurance())
            );
        }
    }

    private void handleEmployeeInsuranceLogic(QuoteInsuranceDTO dto, QuoteInsurance insuranceEntity) {
        if (dto.getEmployeeInsurance() != null) {
            insuranceEntity.setEmployeeInsurance(
                    EmployeeInsuranceMapper.toEntity(dto.getEmployeeInsurance())
            );
        }
    }
}
