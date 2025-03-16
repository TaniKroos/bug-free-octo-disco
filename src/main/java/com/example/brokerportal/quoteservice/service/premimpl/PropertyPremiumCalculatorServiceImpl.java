package com.example.brokerportal.quoteservice.service.premimpl;

import com.example.brokerportal.quoteservice.entities.Premium;
import com.example.brokerportal.quoteservice.entities.PropertyInsurance;
import com.example.brokerportal.quoteservice.entities.QuoteInsurance;
import com.example.brokerportal.quoteservice.exceptions.ResourceNotFoundException;
import com.example.brokerportal.quoteservice.repositories.PremiumRepository;
import com.example.brokerportal.quoteservice.repositories.PropertyInsuranceRepository;
import com.example.brokerportal.quoteservice.repositories.QuoteInsuranceRepository;
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
            basePremium = basePremium.add(insurance.getPropertyValue().multiply(new BigDecimal("0.012"))); //
    }

        if (insurance.getEquipmentValue() != null) {
            basePremium = basePremium.add(insurance.getEquipmentValue().multiply(new BigDecimal("0.01")));
        }

        if (insurance.getInventoryValue() != null) {
            basePremium = basePremium.add(insurance.getInventoryValue().multiply(new BigDecimal("0.008")));
        }

        if (insurance.getCoverageLimit() != null) {
            basePremium = basePremium.add(insurance.getCoverageLimit().multiply(new BigDecimal("0.01")));
        }

        if (insurance.getDeductible() != null) {
            basePremium = basePremium.subtract(insurance.getDeductible().multiply(new BigDecimal("0.004")));
        }


        if (insurance.getBuildingAge() != null && insurance.getBuildingAge() > 20) {
            basePremium = basePremium.add(new BigDecimal("300"));
        }

        if (Boolean.FALSE.equals(insurance.getHasFireAlarmSystem())) {
            basePremium = basePremium.add(new BigDecimal("200"));
        }

        if (Boolean.FALSE.equals(insurance.getHasSecuritySystem())) {
            basePremium = basePremium.add(new BigDecimal("150"));
        }

        if (Boolean.FALSE.equals(insurance.getHasSprinklerSystem())) {
            basePremium = basePremium.add(new BigDecimal("250"));
        }

        if (Boolean.FALSE.equals(insurance.getIsCompliantWithLocalCodes())) {
            basePremium = basePremium.add(new BigDecimal("500"));
        }

        if (Boolean.TRUE.equals(insurance.getBusinessInterruptionCoverRequired()) && insurance.getBusinessInterruptionLimit() != null) {
            basePremium = basePremium.add(insurance.getBusinessInterruptionLimit().multiply(new BigDecimal("0.015"))); // 1.5% of BI limit
        }


        if (insurance.getPropertyType() != null) {
            switch (insurance.getPropertyType()) {
                case INDUSTRIAL -> basePremium = basePremium.multiply(new BigDecimal("1.2"));
                case COMMERCIAL -> basePremium = basePremium.multiply(new BigDecimal("1.1"));

            }
        }


        if (insurance.getConstructionType() != null) {
            switch (insurance.getConstructionType()) {
                case WOOD -> basePremium = basePremium.multiply(new BigDecimal("1.15"));
                case STEEL -> basePremium = basePremium.multiply(new BigDecimal("1.05"));
                case CONCRETE -> basePremium = basePremium.multiply(new BigDecimal("0.95"));
            }
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

        log.info("Property Premium calculated: base={}, taxes={}, total={}", basePremium, taxes, totalPremium);
    }
}
