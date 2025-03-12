package com.example.brokerportal.quoteservice.service;

import com.example.brokerportal.quoteservice.dto.QuoteInsuranceDTO;
import com.example.brokerportal.quoteservice.entities.Quote;
import com.example.brokerportal.quoteservice.entities.QuoteInsurance;
import com.example.brokerportal.quoteservice.mapper.QuoteInsuranceMapper;
import com.example.brokerportal.quoteservice.repositories.QuoteInsuranceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuoteInsuranceServiceImpl implements QuoteInsuranceService{

    private final QuoteInsuranceRepository  quoteInsuranceRepository;

    @Override
    @Transactional
    public void deleteInsurancesByQuote(Quote quote){
        quoteInsuranceRepository.deleteAllByQuote(quote);
    }

    @Override
    @Transactional
    public List<QuoteInsuranceDTO> mapAndAttachInsurancesToQuote(Quote quote, List<QuoteInsuranceDTO> quoteInsuranceDTOS){
        List<QuoteInsurance> insurancesEntities = quoteInsuranceDTOS.stream()
                .map(QuoteInsuranceMapper::toEntity)
                .peek(insurance -> insurance.setQuote(quote))
                .toList();

        quote.getInsurances().clear();                         // safely removes old (orphanRemoval triggers)
        quote.getInsurances().addAll(insurancesEntities);         quoteInsuranceRepository.saveAll(insurancesEntities);
        return insurancesEntities.stream()
                .map(QuoteInsuranceMapper::toDTO)
                .toList();
    }
}
