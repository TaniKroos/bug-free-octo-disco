package com.example.brokerportal.quoteservice.service.premimpl;

import com.example.brokerportal.quoteservice.entities.*;
import com.example.brokerportal.quoteservice.enums.BusinessType;
import com.example.brokerportal.quoteservice.exceptions.ResourceNotFoundException;
import com.example.brokerportal.quoteservice.repositories.BusinessRiskFactorRepository;
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
    private final BusinessRiskFactorRepository businessRiskFactorRepository;
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
            basePremium = basePremium.add(insurance.getCoverageLimit().multiply(BigDecimal.valueOf(0.0008))); // reduced
        }

        if (insurance.getAnnualPayroll() != null) {
            basePremium = basePremium.add(insurance.getAnnualPayroll().multiply(BigDecimal.valueOf(0.007))); // reduced
        }

        if (insurance.getDeductible() != null) {
            basePremium = basePremium.subtract(insurance.getDeductible().multiply(BigDecimal.valueOf(1)));
        }

        basePremium = basePremium.max(BigDecimal.ZERO);

        BigDecimal riskFactor = getRiskFactor(insurance);
        BigDecimal businessRiskFactor = getBusinessRiskFactorFromDB(insurance);

        BigDecimal finalRiskFactor = riskFactor.multiply(businessRiskFactor).setScale(2, RoundingMode.HALF_UP);
        BigDecimal adjustedBase = basePremium.multiply(finalRiskFactor).setScale(2, RoundingMode.HALF_UP);

        BigDecimal taxes = adjustedBase.multiply(BigDecimal.valueOf(0.18)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPremium = adjustedBase.add(taxes).setScale(2, RoundingMode.HALF_UP);

        Premium premium = quoteInsurance.getPremium();
        if (premium == null) {
            premium = new Premium();
            premium.setQuoteInsurance(quoteInsurance);
        }

        premium.setBasePremium(adjustedBase.doubleValue());
        premium.setTaxes(taxes.doubleValue());
        premium.setTotalPremium(totalPremium.doubleValue());

        premiumRepository.save(premium);

        log.info("General Liability Premium calculated: base={}, taxes={}, total={}",
                adjustedBase, taxes, totalPremium);
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

    private BigDecimal getBusinessRiskFactorFromDB(GeneralLiabilityInsurance insurance) {
        QuoteInsurance quoteInsurance = insurance.getQuoteInsurance();
        Client client = quoteInsurance.getQuote().getClient();
        String businessTypeStr = client.getBusinessType();

        BusinessType businessType = parseBusinessType(businessTypeStr);
        BusinessRiskFactor riskBusinessFactor = businessRiskFactorRepository.findByBusinessType(businessType)
                .orElseThrow(() -> new ResourceNotFoundException("Risk factor not found for BusinessType: " + businessType));

        return riskBusinessFactor.getBusinessRisk() != null
                ? BigDecimal.valueOf(riskBusinessFactor.getBusinessRisk()).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ONE;

    }
    private BusinessType parseBusinessType(String businessTypeStr) {
        try {
            return BusinessType.valueOf(businessTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid business type: " + businessTypeStr);
        }
    }
}
