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
            basePremium = basePremium.add(insurance.getCoverageLimit().multiply(BigDecimal.valueOf(0.02)));
        }

        if (insurance.getAnnualPayroll() != null) {
            basePremium = basePremium.add(insurance.getAnnualPayroll().multiply(BigDecimal.valueOf(0.01)));
        }

        if (insurance.getDeductible() != null) {
            basePremium = basePremium.subtract(insurance.getDeductible().multiply(BigDecimal.valueOf(0.005)));
        }

        basePremium = basePremium.max(BigDecimal.ZERO);

        BigDecimal riskFactor = getRiskFactor(insurance);
        basePremium = basePremium.multiply(riskFactor).setScale(2, RoundingMode.HALF_UP);

        BigDecimal taxes = basePremium.multiply(BigDecimal.valueOf(0.18)).setScale(2, RoundingMode.HALF_UP);
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

    private static BigDecimal getRiskFactor(GeneralLiabilityInsurance insurance) {
        int factor = 1;

        if ("HIGH".equalsIgnoreCase(insurance.getRiskClassification())) {
            factor += 1;
        } else if ("LOW".equalsIgnoreCase(insurance.getRiskClassification())) {
            factor -= 1;
        }

        if (Boolean.TRUE.equals(insurance.getHasPriorClaims()) && insurance.getNumberOfClaims() != null) {
            factor += insurance.getNumberOfClaims();
        }

        if (insurance.getBusinessAreaSqft() != null && insurance.getBusinessAreaSqft() > 5000) {
            factor += 1;
        }

        if ("HIGH".equalsIgnoreCase(insurance.getClientInteractionLevel())) {
            factor += 1;
        }

        if (Boolean.TRUE.equals(insurance.getAdditionalInsuredRequired())) {
            factor += 1;
        }

        return BigDecimal.valueOf(Math.max(factor, 1)).setScale(2, RoundingMode.HALF_UP);
    }
}
