package com.example.brokerportal.quoteservice.service.premimpl;

import com.example.brokerportal.quoteservice.entities.*;
import com.example.brokerportal.quoteservice.enums.BusinessType;
import com.example.brokerportal.quoteservice.enums.CoverageType;
import com.example.brokerportal.quoteservice.exceptions.ResourceNotFoundException;
import com.example.brokerportal.quoteservice.repositories.*;
import com.example.brokerportal.quoteservice.utils.PremiumUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import static com.example.brokerportal.quoteservice.utils.PremiumUtils.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyPremiumCalculatorServiceImpl implements PropertyPremiumCalculatorService {

    private final PropertyInsuranceRepository propertyInsuranceRepository;
    private final QuoteInsuranceRepository quoteInsuranceRepository;
    private final PremiumRepository premiumRepository;
    private final BusinessRiskFactorRepository businessRiskFactorRepository;

    private static final BigDecimal PROPERTY_VALUE_RATE = new BigDecimal("0.0001");
    private static final BigDecimal COVERAGE_LIMIT_RATE = new BigDecimal("0.001");
    private static final BigDecimal BI_PREMIUM_RATE = new BigDecimal("0.008");
    private static final BigDecimal DEDUCTIBLE_DISCOUNT_RATE = new BigDecimal("0.001");

    @Override
    @Transactional
    public void calculatePremium(Long quoteInsuranceId) {
        QuoteInsurance quoteInsurance = getQuoteInsurance(quoteInsuranceId);
        PropertyInsurance insurance = getPropertyInsurance(quoteInsuranceId);
        BusinessRiskFactor riskFactor = getBusinessRiskFactor(quoteInsurance);
        Client client = quoteInsurance.getQuote().getClient();

        // Calculate core premium components
        BigDecimal basePremium = calculateCorePremiumComponents(insurance)
                .multiply(BigDecimal.valueOf(riskFactor.getConstructionTypeFactor()))
                .multiply(BigDecimal.valueOf(riskFactor.getLocationRiskFactor()))
                .setScale(2, RoundingMode.HALF_UP);

        // Apply structural risk factors
        BigDecimal structuralFactor = calculateStructuralFactor(insurance);
        basePremium = basePremium.multiply(structuralFactor);

        // Apply business-specific risk factors
        basePremium = applyBusinessRiskFactors(basePremium, riskFactor);

        // Calculate Business Interruption (BI) premium
        BigDecimal biPremium = calculateBusinessInterruptionPremium(insurance, quoteInsurance, riskFactor);

        // Calculate taxes based on state
        BigDecimal stateTaxRate = getTaxRate(client.getAddress());
        System.out.println(stateTaxRate);
        BigDecimal totalBeforeTax = basePremium.add(biPremium);
        BigDecimal taxes = totalBeforeTax.multiply(stateTaxRate).setScale(2, RoundingMode.HALF_UP);

        // Final total premium including taxes
        BigDecimal totalPremium = totalBeforeTax.add(taxes);

        // Save the premium details (excluding coverage-related details)
        savePremiumDetails(quoteInsurance, basePremium, biPremium, taxes, totalPremium);

        log.info("Premium calculated - TotalBeforeTax: {} , Base: {}, BI: {}, Taxes: {}, Total: {}",
                totalBeforeTax,  basePremium, biPremium, taxes, totalPremium);
    }

    // Get QuoteInsurance
    QuoteInsurance getQuoteInsurance(Long quoteInsuranceId) {
        return quoteInsuranceRepository.findById(quoteInsuranceId)
                .orElseThrow(() -> new ResourceNotFoundException("QuoteInsurance not found for ID: " + quoteInsuranceId));
    }

    // Get PropertyInsurance
    PropertyInsurance getPropertyInsurance(Long quoteInsuranceId) {
        return propertyInsuranceRepository.findByQuoteInsuranceIdAndDeletedFalse(quoteInsuranceId)
                .orElseThrow(() -> new ResourceNotFoundException("Property Insurance not found"));
    }

    // Core premium calculation (Property value, Coverage limit, Deductible)
    BigDecimal calculateCorePremiumComponents(PropertyInsurance insurance) {
        BigDecimal premium = BigDecimal.ZERO;

        if (insurance.getPropertyValue() != null) {
            premium = premium.add(insurance.getPropertyValue().multiply(PROPERTY_VALUE_RATE));
        }
        if (insurance.getCoverageLimit() != null) {
            premium = premium.add(insurance.getCoverageLimit().multiply(COVERAGE_LIMIT_RATE));
        }
        if (insurance.getDeductible() != null) {
            premium = premium.subtract(insurance.getDeductible().multiply(DEDUCTIBLE_DISCOUNT_RATE));
        }

        return premium.max(BigDecimal.ZERO);
    }

    // Apply structural risk factors
    BigDecimal calculateStructuralFactor(PropertyInsurance insurance) {
        BigDecimal factor = BigDecimal.ONE;

        if (insurance.getBuildingAge() != null && insurance.getBuildingAge() > 20) {
            factor = factor.add(BigDecimal.valueOf(0.09));
        }else if (insurance.getBuildingAge() != null && insurance.getBuildingAge() > 10) {
            factor = factor.add(BigDecimal.valueOf(0.05));
        }

        if (Boolean.FALSE.equals(insurance.getHasFireAlarmSystem())) {
            factor = factor.add(BigDecimal.valueOf(0.40));
        }
        if (Boolean.FALSE.equals(insurance.getHasSecuritySystem())) {
            factor = factor.add(BigDecimal.valueOf(0.09));
        }
        if (Boolean.FALSE.equals(insurance.getHasSprinklerSystem())) {
            factor = factor.add(BigDecimal.valueOf(0.09));
        }
        if (Boolean.FALSE.equals(insurance.getIsCompliantWithLocalCodes())) {
            factor = factor.add(BigDecimal.valueOf(0.14));
        }

        return factor;
    }

    // Apply business-specific risk factors (Theft protection, occupancy, liability exposure)
    public BigDecimal applyBusinessRiskFactors(BigDecimal basePremium, BusinessRiskFactor factor) {
        if (factor == null) {
            return basePremium;
        }

        BigDecimal multiplier = BigDecimal.valueOf(factor.getConstructionTypeFactor())
                .multiply(BigDecimal.valueOf(factor.getLocationRiskFactor()))
                .multiply(BigDecimal.valueOf(factor.getTheftProtectionFactor()))
                .multiply(BigDecimal.valueOf(factor.getOccupancyRiskFactor()));

        return basePremium.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }


    // Calculate Business Interruption Premium
    BigDecimal calculateBusinessInterruptionPremium(PropertyInsurance insurance,
                                                    QuoteInsurance quoteInsurance,
                                                    BusinessRiskFactor riskFactor) {
        if (!Boolean.TRUE.equals(insurance.getBusinessInterruptionCoverRequired()) ||
                insurance.getBusinessInterruptionLimit() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal biPremium = insurance.getBusinessInterruptionLimit()
                .multiply(BI_PREMIUM_RATE)
                .multiply(BigDecimal.valueOf(riskFactor.getOccupancyRiskFactor()))
                .setScale(2, RoundingMode.HALF_UP);

        return biPremium;
    }

    // Save the premium details (base premium, BI premium, taxes, and total premium)
    void savePremiumDetails(QuoteInsurance quoteInsurance,
                            BigDecimal basePremium,
                            BigDecimal biPremium,
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

    // Get Business Risk Factor for the client
    BusinessRiskFactor getBusinessRiskFactor(QuoteInsurance quoteInsurance) {
        Client client = quoteInsurance.getQuote().getClient();
        BusinessType businessType = parse(client.getBusinessType());
        return businessRiskFactorRepository.findByBusinessType(businessType)
                .orElseThrow(() -> new ResourceNotFoundException("Risk factor not found for " + businessType));
    }

    // Calculate tax rate based on state

}
