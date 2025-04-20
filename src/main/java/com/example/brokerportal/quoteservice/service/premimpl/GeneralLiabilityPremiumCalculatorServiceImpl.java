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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneralLiabilityPremiumCalculatorServiceImpl implements GeneralLiabilityPremiumCalculatorService {

    private final GeneralLiabilityInsuranceRepository generalInsuranceRepository;
    private final QuoteInsuranceRepository quoteInsuranceRepository;
    private final PremiumRepository premiumRepository;
    private final BusinessRiskFactorRepository businessRiskFactorRepository;

    // Adjusted base rates to target ~$8000 for $1M coverage
    private static final BigDecimal BASE_RATE_PER_MILLION = new BigDecimal("400");
    private static final BigDecimal PAYROLL_FACTOR = new BigDecimal("0.002");
    private static final BigDecimal DEDUCTIBLE_CREDIT = new BigDecimal("0.0005");
    private static final BigDecimal MINIMUM_PREMIUM = new BigDecimal("500");

    // Moderate risk factors
    private static final BigDecimal HIGH_RISK_CLASS_FACTOR = new BigDecimal("1.2");
    private static final BigDecimal CONSTRUCTION_RISK_FACTOR = new BigDecimal("1.3");
    private static final BigDecimal LOW_RISK_CLASS_FACTOR = new BigDecimal("0.9");
    private static final BigDecimal PRIOR_CLAIM_FACTOR = new BigDecimal("0.1");
    private static final BigDecimal LARGE_PREMISES_FACTOR = new BigDecimal("1.1");
    private static final BigDecimal HIGH_INTERACTION_FACTOR = new BigDecimal("1.15");
    private static final BigDecimal ADDITIONAL_INSURED_FACTOR = new BigDecimal("1.05");

    @Override
    @Transactional
    public void calculatePremium(Long quoteInsuranceId) {
        QuoteInsurance quoteInsurance = getQuoteInsurance(quoteInsuranceId);
        GeneralLiabilityInsurance insurance = getInsurance(quoteInsuranceId);
        BusinessRiskFactor riskFactor = getBusinessRiskFactor(quoteInsurance);
        Client client = quoteInsurance.getQuote().getClient();

        // Calculate base premium
        BigDecimal basePremium = calculateBasePremium(insurance);

        // Apply risk factors
        BigDecimal riskAdjustedPremium = applyRiskFactors(basePremium, insurance, riskFactor);

        // Calculate taxes based on state
        BigDecimal stateTaxRate = calculateTaxRateBasedOnState(client.getAddress());
        BigDecimal taxes = riskAdjustedPremium.multiply(stateTaxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPremium = riskAdjustedPremium.add(taxes);

        // Save premium
        savePremium(quoteInsurance, riskAdjustedPremium, taxes, totalPremium);

        log.info("General Liability Premium calculated - Base: {}, Taxes: {}, Total: {}",
                riskAdjustedPremium, taxes, totalPremium);
    }

    private BigDecimal calculateBasePremium(GeneralLiabilityInsurance insurance) {
        BigDecimal premium = BigDecimal.ZERO;

        // Coverage limit component
        if (insurance.getCoverageLimit() != null) {
            BigDecimal coverageInMillions = insurance.getCoverageLimit()
                    .divide(new BigDecimal("1000000"), 6, RoundingMode.HALF_UP);
            premium = premium.add(BASE_RATE_PER_MILLION.multiply(coverageInMillions));
        }

        // Payroll component
        if (insurance.getAnnualPayroll() != null) {
            premium = premium.add(insurance.getAnnualPayroll().multiply(PAYROLL_FACTOR));
        }

        // Deductible credit
        if (insurance.getDeductible() != null) {
            premium = premium.subtract(insurance.getDeductible().multiply(DEDUCTIBLE_CREDIT));
        }

        return premium.max(MINIMUM_PREMIUM);
    }

    private BigDecimal applyRiskFactors(BigDecimal premium, GeneralLiabilityInsurance insurance, BusinessRiskFactor riskFactor) {
        // Apply business risk factors from DB
        BigDecimal adjustedPremium = premium
                .multiply(BigDecimal.valueOf(riskFactor.getLiabilityExposureFactor()))
                .multiply(BigDecimal.valueOf(riskFactor.getConstructionTypeFactor()))
                .multiply(BigDecimal.valueOf(riskFactor.getOccupancyRiskFactor()));

        // Apply policy-specific factors
        BigDecimal policyFactor = BigDecimal.ONE;

        if ("CONSTRUCTION".equalsIgnoreCase(insurance.getRiskClassification())) {
            policyFactor = policyFactor.multiply(CONSTRUCTION_RISK_FACTOR);
        } else if ("HIGH".equalsIgnoreCase(insurance.getRiskClassification())) {
            policyFactor = policyFactor.multiply(HIGH_RISK_CLASS_FACTOR);
        } else if ("LOW".equalsIgnoreCase(insurance.getRiskClassification())) {
            policyFactor = policyFactor.multiply(LOW_RISK_CLASS_FACTOR);
        }

        if (Boolean.TRUE.equals(insurance.getHasPriorClaims())) {
            int claims = insurance.getNumberOfClaims() != null ? insurance.getNumberOfClaims() : 1;
            policyFactor = policyFactor.multiply(BigDecimal.ONE.add(
                    PRIOR_CLAIM_FACTOR.multiply(new BigDecimal(claims))));
        }

        if (insurance.getBusinessAreaSqft() != null && insurance.getBusinessAreaSqft() > 5000) {
            policyFactor = policyFactor.multiply(LARGE_PREMISES_FACTOR);
        }

        if (Boolean.TRUE.equals(insurance.getAdditionalInsuredRequired())) {
            policyFactor = policyFactor.multiply(ADDITIONAL_INSURED_FACTOR);
        }

        if ("HIGH".equalsIgnoreCase(insurance.getClientInteractionLevel())) {
            policyFactor = policyFactor.multiply(HIGH_INTERACTION_FACTOR)
                    .multiply(BigDecimal.valueOf(riskFactor.getClientInteractionFactor()));
        }

        return adjustedPremium.multiply(policyFactor).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTaxRateBasedOnState(String address) {
        String state = extractStateFromAddress(address);
        BigDecimal taxRate = BigDecimal.valueOf(0.18); // Default 18%

        Map<String, BigDecimal> stateTaxRates = new HashMap<>();
        stateTaxRates.put("CALIFORNIA", BigDecimal.valueOf(0.075));
        stateTaxRates.put("TEXAS", BigDecimal.valueOf(0.0625));
        stateTaxRates.put("NEW_YORK", BigDecimal.valueOf(0.088));
        stateTaxRates.put("FLORIDA", BigDecimal.valueOf(0.06));
        stateTaxRates.put("ILLINOIS", BigDecimal.valueOf(0.062));
        stateTaxRates.put("OHIO", BigDecimal.valueOf(0.05));
        stateTaxRates.put("MASSACHUSETTS", BigDecimal.valueOf(0.0625));
        stateTaxRates.put("PENNSYLVANIA", BigDecimal.valueOf(0.06));

        if (state != null && stateTaxRates.containsKey(state.toUpperCase())) {
            taxRate = stateTaxRates.get(state.toUpperCase());
        }

        return taxRate;
    }

    private String extractStateFromAddress(String address) {
        if (address == null || address.isBlank()) return null;
        String[] parts = address.split(",");
        return parts.length >= 3 ? parts[2].trim().toUpperCase() : null;
    }

    private void savePremium(QuoteInsurance quoteInsurance,
                             BigDecimal basePremium,
                             BigDecimal taxes,
                             BigDecimal totalPremium) {
        Premium premium = Optional.ofNullable(quoteInsurance.getPremium()).orElseGet(Premium::new);
        premium.setQuoteInsurance(quoteInsurance);
        premium.setBasePremium(basePremium.doubleValue());
        premium.setTaxes(taxes.doubleValue());
        premium.setTotalPremium(totalPremium.doubleValue());

        premiumRepository.save(premium);
        quoteInsurance.setPremium(premium);
        quoteInsuranceRepository.save(quoteInsurance);
    }
    private QuoteInsurance getQuoteInsurance(Long quoteInsuranceId) {
        return quoteInsuranceRepository.findById(quoteInsuranceId)
                .orElseThrow(() -> new ResourceNotFoundException("QuoteInsurance not found"));
    }

    private GeneralLiabilityInsurance getInsurance(Long quoteInsuranceId) {
        GeneralLiabilityInsurance insurance = generalInsuranceRepository
                .findByQuoteInsuranceIdAndDeletedFalse(quoteInsuranceId)
                .orElseThrow(() -> new ResourceNotFoundException("Insurance not found"));
        if (Boolean.TRUE.equals(insurance.getDeleted())) {
            throw new IllegalStateException("Cannot calculate premium for deleted insurance");
        }
        return insurance;
    }

    private BusinessRiskFactor getBusinessRiskFactor(QuoteInsurance quoteInsurance) {
        Client client = quoteInsurance.getQuote().getClient();
        BusinessType type = BusinessType.valueOf(client.getBusinessType().toUpperCase());
        return businessRiskFactorRepository.findByBusinessType(type)
                .orElseThrow(() -> new ResourceNotFoundException("Risk factor not found"));
    }

    private void logPremiumCalculation(GeneralLiabilityInsurance insurance,
                                       BigDecimal basePremium,
                                       BigDecimal afterCoverage,
                                       BigDecimal afterBusiness,
                                       BigDecimal afterPolicy,
                                       BigDecimal finalPremium,
                                       BigDecimal taxes,
                                       BigDecimal totalPremium,
                                       BusinessRiskFactor riskFactor) {
        log.info("""
                General Liability Premium Calculation:
                --------------------------------------------------
                Inputs:
                - Coverage Limit: {}
                - Annual Payroll: {}
                - Deductible: {}
                - Business Area Sqft: {}
                - Risk Classification: {}
                - Has Prior Claims: {}
                - Number of Claims: {}
                - Additional Insured: {}
                - Client Interaction: {}
                - Operations: {}
                - Business Type: {}
                
                Calculation:
                - Base Premium: ${}
                - After Coverage Factors: ${}
                - After Business Risk Factors: ${}
                - After Policy Risk Factors: ${}
                - After Cap: ${}
                - Taxes ({}%): ${}
                - Total Premium: ${}
                --------------------------------------------------
                """,
                insurance.getCoverageLimit(),
                insurance.getAnnualPayroll(),
                insurance.getDeductible(),
                insurance.getBusinessAreaSqft(),
                insurance.getRiskClassification(),
                insurance.getHasPriorClaims(),
                insurance.getNumberOfClaims(),
                insurance.getAdditionalInsuredRequired(),
                insurance.getClientInteractionLevel(),
                insurance.getDescriptionOfOperations(),
                insurance.getQuoteInsurance().getQuote().getClient().getBusinessType(),
                basePremium,
                afterCoverage,
                afterBusiness,
                afterPolicy,
                finalPremium,
                riskFactor.getBaseTaxRate() * 100,
                taxes,
                totalPremium
        );
    }
}
