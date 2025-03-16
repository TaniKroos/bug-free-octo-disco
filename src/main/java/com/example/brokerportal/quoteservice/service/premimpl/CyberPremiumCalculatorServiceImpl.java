package com.example.brokerportal.quoteservice.service.premimpl;

import com.example.brokerportal.quoteservice.entities.CyberInsurance;
import com.example.brokerportal.quoteservice.entities.Premium;
import com.example.brokerportal.quoteservice.entities.QuoteInsurance;
import com.example.brokerportal.quoteservice.exceptions.ResourceNotFoundException;
import com.example.brokerportal.quoteservice.repositories.CyberInsuranceRepository;
import com.example.brokerportal.quoteservice.repositories.PremiumRepository;
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
public class CyberPremiumCalculatorServiceImpl implements CyberPremiumCalculatorService {

    private final CyberInsuranceRepository cyberInsuranceRepository;
    private final QuoteInsuranceRepository quoteInsuranceRepository;
    private final PremiumRepository premiumRepository;

    @Override
    @Transactional
    public void calculatePremium(Long quoteInsuranceId) {
        QuoteInsurance quoteInsurance = quoteInsuranceRepository.findById(quoteInsuranceId)
                .orElseThrow(() -> new ResourceNotFoundException("QuoteInsurance not found for ID: " + quoteInsuranceId));

        CyberInsurance insurance = cyberInsuranceRepository.findByQuoteInsuranceIdAndDeletedFalse(quoteInsuranceId)
                .orElseThrow(() -> new ResourceNotFoundException("Cyber Insurance not found for QuoteInsurance ID: " + quoteInsuranceId));

        if (Boolean.TRUE.equals(insurance.getDeleted())) {
            throw new IllegalStateException("Cannot calculate premium for soft-deleted insurance");
        }

        log.info("Calculating Cyber Insurance Premium for QuoteInsurance ID: {}", quoteInsuranceId);

        //
        BigDecimal basePremium = BigDecimal.ZERO;

        if (insurance.getCoverageLimit() != null) {
            basePremium = basePremium.add(insurance.getCoverageLimit().multiply(new BigDecimal("0.015"))); // 1.5% of coverage limit
        }

        if (insurance.getDeductible() != null) {
            basePremium = basePremium.subtract(insurance.getDeductible().multiply(new BigDecimal("0.004"))); // 0.4% deductible discount
        }

        if (Boolean.TRUE.equals(insurance.getHasPriorCyberIncidents()) && insurance.getNumberOfPriorIncidents() != null) {
            basePremium = basePremium.add(BigDecimal.valueOf(insurance.getNumberOfPriorIncidents()).multiply(new BigDecimal("120")));
        }

        if (insurance.getDataRecordsVolume() != null) {
            basePremium = basePremium.add(BigDecimal.valueOf(insurance.getDataRecordsVolume()).multiply(new BigDecimal("0.08")));
        }

        if (Boolean.FALSE.equals(insurance.getUsesFirewallAntivirus())) {
            basePremium = basePremium.add(new BigDecimal("300"));
        }

        if (Boolean.FALSE.equals(insurance.getHasDataBackupPolicy())) {
            basePremium = basePremium.add(new BigDecimal("250"));
        }

        if (Boolean.FALSE.equals(insurance.getHasCybersecurityTraining())) {
            basePremium = basePremium.add(new BigDecimal("150"));
        }

        if (Boolean.TRUE.equals(insurance.getStoresCustomerData())) {
            basePremium = basePremium.add(new BigDecimal("200"));
        }


        if ("Finance".equalsIgnoreCase(insurance.getIndustryType())) {
            basePremium = basePremium.multiply(new BigDecimal("1.15"));
        } else if ("Retail".equalsIgnoreCase(insurance.getIndustryType())) {
            basePremium = basePremium.multiply(new BigDecimal("1.10"));
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

        log.info("Cyber Premium calculated: base={}, taxes={}, total={}", basePremium, taxes, totalPremium);
    }
}
