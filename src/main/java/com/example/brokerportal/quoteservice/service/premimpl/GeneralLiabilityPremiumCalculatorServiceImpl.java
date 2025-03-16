package com.example.brokerportal.quoteservice.service.premimpl;

import com.example.brokerportal.quoteservice.entities.GeneralLiabilityInsurance;
import com.example.brokerportal.quoteservice.entities.Premium;
import com.example.brokerportal.quoteservice.entities.QuoteInsurance;
import com.example.brokerportal.quoteservice.exceptions.ResourceNotFoundException;
import com.example.brokerportal.quoteservice.repositories.GeneralLiabilityInsuranceRepository;
import com.example.brokerportal.quoteservice.repositories.PremiumRepository;
import com.example.brokerportal.quoteservice.repositories.QuoteInsuranceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.Optional;
@Service
@RequiredArgsConstructor
@Slf4j
public class GeneralLiabilityPremiumCalculatorServiceImpl implements GeneralLiabilityPremiumCalculatorService {

    private final GeneralLiabilityInsuranceRepository generalInsuranceRepository;
    private final QuoteInsuranceRepository quoteInsuranceRepository;
    private final PremiumRepository premiumRepository;

    @Override
    @Transactional
    public void calculatePremium(Long quoteInsuranceId) {
        QuoteInsurance quoteInsurance = quoteInsuranceRepository.findById(quoteInsuranceId)
                .orElseThrow(() -> new ResourceNotFoundException("QuoteInsurance not found for ID: " + quoteInsuranceId));

        GeneralLiabilityInsurance insurance = generalInsuranceRepository.findByQuoteInsuranceIdAndDeletedFalse(quoteInsuranceId)
                .orElseThrow(() -> new ResourceNotFoundException("General Liability Insurance not found for QuoteInsurance ID: " + quoteInsuranceId));

        if (Boolean.TRUE.equals(insurance.getDeleted())) {
            throw new IllegalStateException("Cannot calculate premium for soft-deleted insurance");
        }

        log.info("Calculating General Liability Premium for QuoteInsurance ID: {}", quoteInsuranceId);


        BigDecimal basePremium = BigDecimal.ZERO;

        if (insurance.getCoverageLimit() != null) {
            basePremium = basePremium.add(insurance.getCoverageLimit().multiply(new BigDecimal("0.02"))); // 2% of coverage limit
        }

        if (insurance.getAnnualPayroll() != null) {
            basePremium = basePremium.add(insurance.getAnnualPayroll().multiply(new BigDecimal("0.01"))); // 1% of payroll
        }

        if (insurance.getDeductible() != null) {
            basePremium = basePremium.subtract(insurance.getDeductible().multiply(new BigDecimal("0.005"))); // 0.5% of deductible
        }

        if ("HIGH".equalsIgnoreCase(insurance.getRiskClassification())) {
            basePremium = basePremium.multiply(new BigDecimal("1.2"));
        } else if ("LOW".equalsIgnoreCase(insurance.getRiskClassification())) {
            basePremium = basePremium.multiply(new BigDecimal("0.9"));
        }

        if (Boolean.TRUE.equals(insurance.getHasPriorClaims()) && insurance.getNumberOfClaims() != null) {
            basePremium = basePremium.add(BigDecimal.valueOf(insurance.getNumberOfClaims()).multiply(new BigDecimal("100")));
        }

        if (insurance.getBusinessAreaSqft() != null) {
            basePremium = basePremium.add(BigDecimal.valueOf(insurance.getBusinessAreaSqft()).multiply(new BigDecimal("0.1")));
        }

        if ("HIGH".equalsIgnoreCase(insurance.getClientInteractionLevel())) {
            basePremium = basePremium.multiply(new BigDecimal("1.1"));
        }


        if (Boolean.TRUE.equals(insurance.getAdditionalInsuredRequired())) {
            basePremium = basePremium.add(new BigDecimal("200"));
        }


        basePremium = basePremium.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        BigDecimal taxes = basePremium.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPremium = basePremium.add(taxes).setScale(2, RoundingMode.HALF_UP);

        Premium premium = quoteInsurance.getPremium();

        if (premium == null) {
            premium = new Premium();
            premium.setQuoteInsurance(quoteInsurance);
        }
        premium.setBasePremium(basePremium.doubleValue());
        premium.setTaxes(taxes.doubleValue());
        premium.setTotalPremium(totalPremium.doubleValue());


        premiumRepository.save(premium);

        log.info("General Liability Premium calculated: base={}, taxes={}, total={}",
                basePremium, taxes, totalPremium);
    }
}
