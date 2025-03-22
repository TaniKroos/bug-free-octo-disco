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

@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyPremiumCalculatorServiceImpl implements PropertyPremiumCalculatorService {

    private final PropertyInsuranceRepository propertyInsuranceRepository;
    private final QuoteInsuranceRepository quoteInsuranceRepository;
    private final PremiumRepository premiumRepository;
    private final CoveragePremiumRepository coveragePremiumRepository;
    private final BusinessRiskFactorRepository businessRiskFactorRepository;

    @Override
    @Transactional
    public void calculatePremium(Long quoteInsuranceId) {
        QuoteInsurance quoteInsurance = quoteInsuranceRepository.findById(quoteInsuranceId)
                .orElseThrow(() -> new ResourceNotFoundException("QuoteInsurance not found for ID: " + quoteInsuranceId));

        PropertyInsurance insurance = propertyInsuranceRepository.findByQuoteInsuranceIdAndDeletedFalse(quoteInsuranceId)
                .orElseThrow(() -> new ResourceNotFoundException("Property Insurance not found for QuoteInsurance ID: " + quoteInsuranceId));

        if (Boolean.TRUE.equals(insurance.getDeleted())) {
            throw new IllegalStateException("Cannot calculate premium for soft-deleted insurance");
        }

        log.info("Calculating Property Insurance Premium for QuoteInsurance ID: {}", quoteInsuranceId);

        BigDecimal basePremium = BigDecimal.ZERO;

        if (insurance.getPropertyValue() != null) {
            basePremium = basePremium.add(insurance.getPropertyValue().multiply(BigDecimal.valueOf(0.007))); // was 0.012
        }
        if (insurance.getEquipmentValue() != null) {
            basePremium = basePremium.add(insurance.getEquipmentValue().multiply(BigDecimal.valueOf(0.006))); // was 0.01
        }
        if (insurance.getInventoryValue() != null) {
            basePremium = basePremium.add(insurance.getInventoryValue().multiply(BigDecimal.valueOf(0.005))); // was 0.008
        }
        if (insurance.getCoverageLimit() != null) {
            basePremium = basePremium.add(insurance.getCoverageLimit().multiply(BigDecimal.valueOf(0.004))); // was 0.01
        }
        if (insurance.getDeductible() != null) {
            basePremium = basePremium.subtract(insurance.getDeductible().multiply(BigDecimal.valueOf(0.004))); // same
        }

        // BI Premium Calculation (Separate Line Item)
        BigDecimal biPremium = BigDecimal.ZERO;
        if (Boolean.TRUE.equals(insurance.getBusinessInterruptionCoverRequired()) && insurance.getBusinessInterruptionLimit() != null) {
            biPremium = insurance.getBusinessInterruptionLimit().multiply(BigDecimal.valueOf(0.015)); // 1.5%
            basePremium = basePremium.add(biPremium);

            // 💾 Save BI Coverage Premium entry to CoveragePremium table
            CoveragePremium coveragePremium = coveragePremiumRepository
                    .findByQuoteInsuranceAndCoverageTypeAndDeletedFalse(quoteInsurance, CoverageType.BUSINESS_INTERRUPTION)
                    .orElseGet(() -> {
                        CoveragePremium newCp = new CoveragePremium();
                        newCp.setQuoteInsurance(quoteInsurance);
                        newCp.setCoverageType(CoverageType.BUSINESS_INTERRUPTION);
                        return newCp;
                    });

            coveragePremium.setCoverageAmount(biPremium);
            coveragePremium.setPremiumAmount(biPremium); // Optional: set both if you differentiate
            coveragePremium.setDeleted(false); // in case it was soft deleted earlier
            coveragePremiumRepository.save(coveragePremium);
        }

        basePremium = basePremium.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        // Additional risk factor (excluding BI logic now)
        BigDecimal riskFactor = getRiskFactor(insurance);

        BigDecimal adjustedBasePremium = basePremium.multiply(riskFactor).setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxes = adjustedBasePremium.multiply(BigDecimal.valueOf(0.18)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPremium = adjustedBasePremium.add(taxes).setScale(2, RoundingMode.HALF_UP);

        Premium premium = quoteInsurance.getPremium();
        if (premium == null) {
            premium = new Premium();
            premium.setQuoteInsurance(quoteInsurance);
        }

        premium.setBasePremium(basePremium.doubleValue());
        premium.setTaxes(taxes.doubleValue());
        premium.setTotalPremium(totalPremium.doubleValue());
        // Optional: Set BI separately if you're adding a field like premium.setBusinessInterruptionPremium(biPremium.doubleValue());

        premiumRepository.save(premium);

        log.info("Property Premium calculated: base={}, BI={}, taxes={}, total={}", basePremium, biPremium, taxes, totalPremium);
    }

    private  BigDecimal getRiskFactor(PropertyInsurance insurance) {
        BigDecimal riskFactor = BigDecimal.ONE;

        // Add basic structural/compliance-based risk scoring
        int additiveFactor = 0;
        if (insurance.getBuildingAge() != null && insurance.getBuildingAge() > 20) additiveFactor += 1;
        if (Boolean.FALSE.equals(insurance.getHasFireAlarmSystem())) additiveFactor += 1;
        if (Boolean.FALSE.equals(insurance.getHasSecuritySystem())) additiveFactor += 1;
        if (Boolean.FALSE.equals(insurance.getHasSprinklerSystem())) additiveFactor += 1;
        if (Boolean.FALSE.equals(insurance.getIsCompliantWithLocalCodes())) additiveFactor += 1;

        // Convert additiveFactor to multiplier (e.g., 1 = 1.1x, 2 = 1.2x, etc.)
        BigDecimal structuralFactor = BigDecimal.valueOf(1 + (0.05 * additiveFactor)); // 5% per risk point

        // Now apply business risk factor from DB
        QuoteInsurance quoteInsurance = insurance.getQuoteInsurance();
        Client client = quoteInsurance.getQuote().getClient();
        String businessTypeStr = client.getBusinessType();

        BusinessType businessType = parseBusinessType(businessTypeStr);
        BusinessRiskFactor riskBusinessFactor = businessRiskFactorRepository.findByBusinessType(businessType)
                .orElseThrow(() -> new ResourceNotFoundException("Risk factor not found for BusinessType: " + businessType));

        return structuralFactor
                .multiply(riskFactor)
                .multiply(BigDecimal.valueOf(riskBusinessFactor.getBusinessRisk())).setScale(2, RoundingMode.HALF_UP);
    }
    private BusinessType parseBusinessType(String businessTypeStr) {
        try {
            return BusinessType.valueOf(businessTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid business type: " + businessTypeStr);
        }
    }
}
