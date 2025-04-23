package com.example.brokerportal.quoteservice.service.premimpl;

import com.example.brokerportal.quoteservice.entities.*;
import com.example.brokerportal.quoteservice.enums.BusinessType;
import com.example.brokerportal.quoteservice.exceptions.ResourceNotFoundException;
import com.example.brokerportal.quoteservice.repositories.*;
import com.example.brokerportal.quoteservice.utils.PremiumUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneralLiabilityPremiumCalculatorServiceImpl implements GeneralLiabilityPremiumCalculatorService {

    private final GeneralLiabilityInsuranceRepository generalInsuranceRepository;
    private final QuoteInsuranceRepository quoteInsuranceRepository;
    private final PremiumRepository premiumRepository;
    private final BusinessRiskFactorRepository businessRiskFactorRepository;

    private static final BigDecimal BASE_RATE_PER_MILLION = new BigDecimal("400");
    private static final BigDecimal PAYROLL_FACTOR = new BigDecimal("0.002");
    private static final BigDecimal DEDUCTIBLE_CREDIT = new BigDecimal("0.0005");
    private static final BigDecimal MINIMUM_PREMIUM = new BigDecimal("500");

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
        // Retrieve data
        QuoteInsurance quoteInsurance = getQuoteInsurance(quoteInsuranceId);
        GeneralLiabilityInsurance insurance = getInsurance(quoteInsuranceId);
        BusinessRiskFactor riskFactor = getBusinessRiskFactor(quoteInsurance);
        Client client = quoteInsurance.getQuote().getClient();

        // Calculate base premium
        BigDecimal basePremium = calculateBasePremium(insurance);

        // Apply risk factors
        BigDecimal riskAdjustedPremium = applyRiskFactors(basePremium, insurance, riskFactor);

        // Calculate taxes based on state using PremiumUtils
        BigDecimal stateTaxRate = PremiumUtils.getTaxRate(client.getAddress());
        BigDecimal taxes = riskAdjustedPremium.multiply(stateTaxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPremium = riskAdjustedPremium.add(taxes);

        // Save premium
        savePremium(quoteInsurance, riskAdjustedPremium, taxes, totalPremium);

        log.info("General Liability Premium calculated - Base: {}, Taxes: {}, Total: {}",
                riskAdjustedPremium, taxes, totalPremium);
    }

    BigDecimal calculateBasePremium(GeneralLiabilityInsurance insurance) {
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

        // Return minimum premium if calculated value is lower
        return premium.max(MINIMUM_PREMIUM);
    }

    BigDecimal applyRiskFactors(BigDecimal basePremium, GeneralLiabilityInsurance insurance, BusinessRiskFactor riskFactor) {
        BigDecimal adjustedPremium = basePremium
                .multiply(BigDecimal.valueOf(riskFactor.getLiabilityExposureFactor()))
                .multiply(BigDecimal.valueOf(riskFactor.getConstructionTypeFactor()))
                .multiply(BigDecimal.valueOf(riskFactor.getOccupancyRiskFactor()));

        // Policy-specific adjustments
        adjustedPremium = adjustedPremium.multiply(getPolicyRiskFactor(insurance));

        return adjustedPremium.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal getPolicyRiskFactor(GeneralLiabilityInsurance insurance) {
        BigDecimal policyFactor = BigDecimal.ONE;

        // Risk classification
        policyFactor = policyFactor.multiply(getRiskClassificationFactor(insurance));

        // Prior claims
        if (Boolean.TRUE.equals(insurance.getHasPriorClaims())) {
            policyFactor = applyPriorClaimsFactor(policyFactor, insurance);
        }

        // Business area size
        if (insurance.getBusinessAreaSqft() != null && insurance.getBusinessAreaSqft() > 5000) {
            policyFactor = policyFactor.multiply(LARGE_PREMISES_FACTOR);
        }

        // Additional insured factor
        if (Boolean.TRUE.equals(insurance.getAdditionalInsuredRequired())) {
            policyFactor = policyFactor.multiply(ADDITIONAL_INSURED_FACTOR);
        }

        // High interaction factor
        if ("HIGH".equalsIgnoreCase(insurance.getClientInteractionLevel())) {
            policyFactor = policyFactor.multiply(HIGH_INTERACTION_FACTOR);
        }

        return policyFactor;
    }

    private BigDecimal applyPriorClaimsFactor(BigDecimal policyFactor, GeneralLiabilityInsurance insurance) {
        int claims = Optional.ofNullable(insurance.getNumberOfClaims()).orElse(1);
        return policyFactor.multiply(BigDecimal.ONE.add(PRIOR_CLAIM_FACTOR.multiply(new BigDecimal(claims))));
    }

    private BigDecimal getRiskClassificationFactor(GeneralLiabilityInsurance insurance) {
        switch (insurance.getRiskClassification().toUpperCase()) {
            case "CONSTRUCTION":
                return CONSTRUCTION_RISK_FACTOR;
            case "HIGH":
                return HIGH_RISK_CLASS_FACTOR;
            case "LOW":
                return LOW_RISK_CLASS_FACTOR;
            default:
                return BigDecimal.ONE;
        }
    }

    void savePremium(QuoteInsurance quoteInsurance, BigDecimal basePremium, BigDecimal taxes, BigDecimal totalPremium) {
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
        return generalInsuranceRepository
                .findByQuoteInsuranceIdAndDeletedFalse(quoteInsuranceId)
                .orElseThrow(() -> new ResourceNotFoundException("Insurance not found"));
    }

    private BusinessRiskFactor getBusinessRiskFactor(QuoteInsurance quoteInsurance) {
        Client client = quoteInsurance.getQuote().getClient();
        BusinessType type = PremiumUtils.parse(client.getBusinessType());
        return businessRiskFactorRepository.findByBusinessType(type)
                .orElseThrow(() -> new ResourceNotFoundException("Risk factor not found"));
    }
}