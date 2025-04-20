package com.example.brokerportal.quoteservice.service.premimpl;

import com.example.brokerportal.quoteservice.entities.*;
import com.example.brokerportal.quoteservice.enums.BusinessType;
import com.example.brokerportal.quoteservice.enums.CoverageType;
import com.example.brokerportal.quoteservice.exceptions.ResourceNotFoundException;
import com.example.brokerportal.quoteservice.repositories.*;
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
public class PropertyPremiumCalculatorServiceImpl implements PropertyPremiumCalculatorService {

    private final PropertyInsuranceRepository propertyInsuranceRepository;
    private final QuoteInsuranceRepository quoteInsuranceRepository;
    private final PremiumRepository premiumRepository;
    private final CoveragePremiumRepository coveragePremiumRepository;
    private final BusinessRiskFactorRepository businessRiskFactorRepository;

    private static final BigDecimal MAX_PREMIUM_PERCENTAGE = new BigDecimal("0.03"); // 2% cap
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

        // Calculate BI premium
        BigDecimal biPremium = calculateBusinessInterruptionPremium(insurance, quoteInsurance, riskFactor);

        // Combine and cap premium
        BigDecimal stateTaxRate = calculateTaxRateBasedOnState(client.getAddress());
        BigDecimal totalBeforeTax = basePremium.add(biPremium);
        BigDecimal taxes = basePremium.multiply(stateTaxRate);
        BigDecimal totalPremium = totalBeforeTax.add(taxes);

        // Save all premium components
        savePremiumDetails(quoteInsurance, basePremium, biPremium, taxes, totalPremium);

        log.info("Premium calculated - TotalBeforeTax: {} , Base: {}, BI: {}, Taxes: {}, Total: {}",
                totalBeforeTax,  basePremium, biPremium, taxes, totalPremium);
    }

    private QuoteInsurance getQuoteInsurance(Long quoteInsuranceId) {
        return quoteInsuranceRepository.findById(quoteInsuranceId)
                .orElseThrow(() -> new ResourceNotFoundException("QuoteInsurance not found for ID: " + quoteInsuranceId));
    }

    private PropertyInsurance getPropertyInsurance(Long quoteInsuranceId) {
        PropertyInsurance insurance = propertyInsuranceRepository.findByQuoteInsuranceIdAndDeletedFalse(quoteInsuranceId)
                .orElseThrow(() -> new ResourceNotFoundException("Property Insurance not found"));

        if (Boolean.TRUE.equals(insurance.getDeleted())) {
            throw new IllegalStateException("Cannot calculate premium for deleted insurance");
        }
        return insurance;
    }

    private BigDecimal calculateCorePremiumComponents(PropertyInsurance insurance) {
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

    private BigDecimal calculateStructuralFactor(PropertyInsurance insurance) {
        BigDecimal factor = BigDecimal.ONE;

        if (insurance.getBuildingAge() != null && insurance.getBuildingAge() > 20) {
            factor = factor.add(BigDecimal.valueOf(0.05));
        }
        if (Boolean.FALSE.equals(insurance.getHasFireAlarmSystem())) {
            factor = factor.add(BigDecimal.valueOf(0.10));
        }
        if (Boolean.FALSE.equals(insurance.getHasSecuritySystem())) {
            factor = factor.add(BigDecimal.valueOf(0.08));
        }
        if (Boolean.FALSE.equals(insurance.getHasSprinklerSystem())) {
            factor = factor.add(BigDecimal.valueOf(0.07));
        }
        if (Boolean.FALSE.equals(insurance.getIsCompliantWithLocalCodes())) {
            factor = factor.add(BigDecimal.valueOf(0.12));
        }

        return factor;
    }

    private BigDecimal applyBusinessRiskFactors(BigDecimal premium, BusinessRiskFactor riskFactor) {
        return premium.multiply(BigDecimal.valueOf(riskFactor.getTheftProtectionFactor()))
                .multiply(BigDecimal.valueOf(riskFactor.getOccupancyRiskFactor()))
                .multiply(BigDecimal.valueOf(riskFactor.getLiabilityExposureFactor()))
                .multiply(BigDecimal.valueOf(riskFactor.getEmployeeRiskFactor()))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateBusinessInterruptionPremium(PropertyInsurance insurance,
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

        saveCoveragePremium(quoteInsurance, biPremium, CoverageType.BUSINESS_INTERRUPTION);
        return biPremium;
    }



    private BigDecimal calculateTaxes(BigDecimal premium, double baseTaxRate) {
        return premium.multiply(BigDecimal.valueOf(baseTaxRate))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void saveCoveragePremium(QuoteInsurance quoteInsurance, BigDecimal amount, CoverageType type) {
        CoveragePremium coveragePremium = coveragePremiumRepository
                .findByQuoteInsuranceAndCoverageTypeAndDeletedFalse(quoteInsurance, type)
                .orElseGet(() -> {
                    CoveragePremium cp = new CoveragePremium();
                    cp.setQuoteInsurance(quoteInsurance);
                    cp.setCoverageType(type);
                    return cp;
                });

        coveragePremium.setCoverageAmount(amount);
        coveragePremium.setPremiumAmount(amount);
        coveragePremium.setDeleted(false);
        coveragePremiumRepository.save(coveragePremium);
    }

    private void savePremiumDetails(QuoteInsurance quoteInsurance,
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

    private BusinessRiskFactor getBusinessRiskFactor(QuoteInsurance quoteInsurance) {
        Client client = quoteInsurance.getQuote().getClient();
        BusinessType businessType = BusinessType.valueOf(client.getBusinessType().toUpperCase());
        return businessRiskFactorRepository.findByBusinessType(businessType)
                .orElseThrow(() -> new ResourceNotFoundException("Risk factor not found for " + businessType));
    }

    private BigDecimal calculateTaxRateBasedOnState(String address) {
        String state = extractStateFromAddress(address);  // Extract the state from address
        BigDecimal taxRate = BigDecimal.valueOf(0.18);  // Default tax rate (18%)

        // Define tax rates for different states
        Map<String, BigDecimal> stateTaxRates = new HashMap<>();
        stateTaxRates.put("CALIFORNIA", BigDecimal.valueOf(0.075));  // Example: California has 7.5% tax
        stateTaxRates.put("TEXAS", BigDecimal.valueOf(0.0625));  // Example: Texas has 6.25% tax
        stateTaxRates.put("NEW_YORK", BigDecimal.valueOf(0.088));  // Example: New York has 8.8% tax
        stateTaxRates.put("FLORIDA", BigDecimal.valueOf(0.06));  // Example: Florida has 6% tax
        stateTaxRates.put("ILLINOIS", BigDecimal.valueOf(0.062));  // Example: Illinois has 6.2% tax
        stateTaxRates.put("OHIO", BigDecimal.valueOf(0.05));  // Example: Ohio has 5% tax
        stateTaxRates.put("MASSACHUSETTS", BigDecimal.valueOf(0.0625));  // Example: Massachusetts has 6.25% tax
        stateTaxRates.put("PENNSYLVANIA", BigDecimal.valueOf(0.06));  // Example: Pennsylvania has 6% tax

        // Fetch tax rate based on the client's state (if it's available in the map)
        if (state != null && stateTaxRates.containsKey(state.toUpperCase())) {
            taxRate = stateTaxRates.get(state.toUpperCase());
        }

        return taxRate;
    }
    public String extractStateFromAddress(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }

        String[] parts = address.split(",");
        if (parts.length < 3) {
            return null; // Not enough parts to extract state
        }

        // Trim to clean up leading/trailing spaces
        return parts[2].trim().toUpperCase(); // Return state in uppercase for consistency
    }
}