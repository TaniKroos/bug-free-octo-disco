package com.example.brokerportal.quoteservice.service.premimpl;

import com.example.brokerportal.quoteservice.entities.Quote;
import com.example.brokerportal.quoteservice.entities.QuoteInsurance;
import com.example.brokerportal.quoteservice.exceptions.ResourceNotFoundException;
import com.example.brokerportal.quoteservice.repositories.QuoteRepository;
import com.example.brokerportal.quoteservice.service.PremiumCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PremiumCalculationServiceImpl implements PremiumCalculationService {

    private final QuoteRepository quoteRepository;
    private final CyberPremiumCalculatorService cyberPremiumCalculatorService;
    private final PropertyPremiumCalculatorService propertyPremiumCalculatorService;
    private final GeneralLiabilityPremiumCalculatorService generalPremiumCalculatorService;

    @Override
    public void calculatePremiumForQuote(Long quoteId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote not found with ID: " + quoteId));
        if(quote.isDeleted()){
            throw new ResourceNotFoundException("QUote with this id has been marked soft deleted");
        }
        for (QuoteInsurance insurance : quote.getInsurances()) {
            if (!Boolean.TRUE.equals(insurance.isSelected())) continue;

            switch (insurance.getInsuranceType().toUpperCase()) {
                case "CYBER" -> cyberPremiumCalculatorService.calculatePremium(insurance.getId());
                case "PROPERTY" -> propertyPremiumCalculatorService.calculatePremium(insurance.getId());
                case "GENERAL" -> generalPremiumCalculatorService.calculatePremium(insurance.getId());
                default -> log.warn("Unknown insurance type: {}", insurance.getInsuranceType());
            }
        }
    }
    @Override
    public Double getPremium(Long quoteId){
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("No quote with this id"));
        if(quote.isDeleted()){
            throw new ResourceNotFoundException("QUote with this id has been marked soft deleted");
        }
        Double finalprem = 0.0;
        for(QuoteInsurance qi:quote.getInsurances()){
            if(!Boolean.TRUE.equals(qi.isSelected())) continue;
            finalprem = finalprem  +  qi.getPremium().getTotalPremium();
        }
        return finalprem;
    }
}

