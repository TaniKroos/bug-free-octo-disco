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
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CyberPremiumCalculatorServiceImpl implements CyberPremiumCalculatorService {

    private final CyberInsuranceRepository cyberInsuranceRepository;
    private final QuoteInsuranceRepository quoteInsuranceRepository;
    private final PremiumRepository premiumRepository;
    private final BusinessRiskFactorRepository businessRiskFactorRepository;
    private final CoveragePremiumRepository coveragePremiumRepository;

    @Override
    @Transactional
    public void calculatePremium(Long quoteInsuranceId) {
        QuoteInsurance quoteInsurance = quoteInsuranceRepository.findById(quoteInsuranceId)
                .orElseThrow(() -> new ResourceNotFoundException("QuoteInsurance not found for ID: " + quoteInsuranceId));

        CyberInsurance insurance = cyberInsuranceRepository.findByQuoteInsuranceIdAndDeletedFalse(quoteInsuranceId)
                .orElseThrow(() -> new ResourceNotFoundException("Cyber Insurance not found for QuoteInsurance ID: " + quoteInsuranceId));

        Client client = quoteInsurance.getQuote().getClient();
        String businessTypeStr = client.getBusinessType();
        BusinessType businessType = parseBusinessType(businessTypeStr);

        BusinessRiskFactor riskFactor = businessRiskFactorRepository.findByBusinessType(businessType)
                .orElseThrow(() -> new ResourceNotFoundException("Risk factor not found for BusinessType: " + businessType));

        if (Boolean.TRUE.equals(insurance.getDeleted())) {
            throw new IllegalStateException("Cannot calculate premium for soft-deleted insurance");
        }

        log.info("Calculating Cyber Insurance Premium for QuoteInsurance ID: {}", quoteInsuranceId);

        int baseRate = 800;
        BigDecimal basePremium = BigDecimal.valueOf(riskFactor.getBusinessRisk())
                .multiply(BigDecimal.valueOf(riskFactor.getDataExposure()))
                .multiply(BigDecimal.valueOf(baseRate))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal taxes = basePremium.multiply(BigDecimal.valueOf(0.18)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPremium = basePremium.add(taxes);

        // Save base premium and taxes in Premium table
        Premium premium = Optional.ofNullable(quoteInsurance.getPremium()).orElseGet(Premium::new);
        premium.setQuoteInsurance(quoteInsurance);
        premium.setBasePremium(basePremium.doubleValue());
        premium.setTaxes(taxes.doubleValue());
        premium.setTotalPremium(totalPremium.doubleValue());

        premiumRepository.save(premium);
        quoteInsurance.setPremium(premium);
        quoteInsuranceRepository.save(quoteInsurance);
        // Store coverage-specific premiums
        calculateAndStoreCoveragePremiums(quoteInsurance, insurance);

        log.info("Cyber Premium calculated: base={}, taxes={}, total={}", basePremium, taxes, totalPremium);
    }

    private void calculateAndStoreCoveragePremiums(QuoteInsurance quoteInsurance, CyberInsurance insurance) {
        List<Coverage> selectedCoverages = quoteInsurance.getCoverages();

        // Soft delete existing coverage premiums
        List<CoveragePremium> existing = coveragePremiumRepository.findByQuoteInsurance(quoteInsurance);
        existing.forEach(cp -> cp.setDeleted(true));
        coveragePremiumRepository.saveAll(existing);

        // Create and save new coverage premiums
        for (Coverage coverage : selectedCoverages) {

                CoverageType type = CoverageType.fromStringSafe(coverage.getCoverageType());
                if (type == null) {
                    System.err.println("Invalid or unmapped CoverageType: " + coverage.getCoverageType());
                    continue; // skip invalid ones
                }
                BigDecimal coverageAmount = coverage.getCoverageAmount();
                BigDecimal premiumAmount = calculatePremiumByCoverageType(type, insurance);

                CoveragePremium coveragePremium = new CoveragePremium();
                coveragePremium.setQuoteInsurance(quoteInsurance);
                coveragePremium.setCoverageType(type);
                coveragePremium.setCoverageAmount(coverageAmount);
                coveragePremium.setPremiumAmount(premiumAmount);
                coveragePremium.setDeleted(false);

                coveragePremiumRepository.save(coveragePremium);

        }
    }


    private BigDecimal calculatePremiumByCoverageType(CoverageType type, CyberInsurance insurance) {
        BigDecimal premium = new BigDecimal("1000");

        switch (type) {
            case DATA_BREACH -> {
                if (Boolean.FALSE.equals(insurance.getUsesFirewallAntivirus())) premium = premium.add(new BigDecimal("250"));
                if (Boolean.TRUE.equals(insurance.getStoresCustomerData())) premium = premium.add(new BigDecimal("200"));
                if (Boolean.FALSE.equals(insurance.getHasCybersecurityTraining())) premium = premium.add(new BigDecimal("150"));
                if (Boolean.FALSE.equals(insurance.getHasDataBackupPolicy())) premium = premium.add(new BigDecimal("180"));
            }
            case NETWORK_SECURITY -> {
                if (Boolean.FALSE.equals(insurance.getUsesFirewallAntivirus())) premium = premium.add(new BigDecimal("300"));
                if (Boolean.FALSE.equals(insurance.getHasCybersecurityTraining())) premium = premium.add(new BigDecimal("180"));
                if (Boolean.FALSE.equals(insurance.getHasDataBackupPolicy())) premium = premium.add(new BigDecimal("160"));
                if(Boolean.TRUE.equals(insurance.getHasPriorCyberIncidents())) premium = premium.add(new BigDecimal(insurance.getNumberOfPriorIncidents()*100));
            }
            default -> {
                premium = BigDecimal.ZERO;
            }
        }

        return premium.setScale(2, RoundingMode.HALF_UP);
    }

//    private String buildCalculationJson(CoverageType type, CyberInsurance insurance) {
//        return switch (type) {
//            case DATA_BREACH -> String.format("""
//                    {
//                      "firewallUsed": %s,
//                      "storesCustomerData": %s,
//                      "cyberTraining": %s,
//                      "dataBackup": %s
//                    }
//                    """, insurance.getUsesFirewallAntivirus(), insurance.getStoresCustomerData(),
//                    insurance.getHasCybersecurityTraining(), insurance.getHasDataBackupPolicy());
//
//            case NETWORK_SECURITY -> String.format("""
//                    {
//                      "firewallUsed": %s,
//                      "cyberTraining": %s,
//                      "dataBackup": %s
//                    }
//                    """, insurance.getUsesFirewallAntivirus(), insurance.getHasCybersecurityTraining(), insurance.getHasDataBackupPolicy());
//
//            case RANSOMWARE -> String.format("""
//                    {
//                      "priorIncidents": %s,
//                      "firewallUsed": %s,
//                      "dataBackup": %s
//                    }
//                    """, insurance.getHasPriorCyberIncidents(), insurance.getUsesFirewallAntivirus(), insurance.getHasDataBackupPolicy());
//
//            default -> "{}";
//        };
//    }

    private BusinessType parseBusinessType(String businessTypeStr) {
        try {
            return BusinessType.valueOf(businessTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid business type: " + businessTypeStr);
        }
    }
}
