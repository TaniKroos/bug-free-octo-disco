package com.example.brokerportal.quoteservice.service.premimpl;

import com.example.brokerportal.quoteservice.entities.*;
import com.example.brokerportal.quoteservice.enums.BusinessType;
import com.example.brokerportal.quoteservice.exceptions.ResourceNotFoundException;
import com.example.brokerportal.quoteservice.repositories.BusinessRiskFactorRepository;
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
    private final BusinessRiskFactorRepository businessRiskFactorRepository;


    @Override
    @Transactional
    public void calculatePremium(Long quoteInsuranceId) {
        QuoteInsurance quoteInsurance = quoteInsuranceRepository.findById(quoteInsuranceId)
                .orElseThrow(() -> new ResourceNotFoundException("QuoteInsurance not found for ID: " + quoteInsuranceId));
        CyberInsurance insurance = cyberInsuranceRepository.findByQuoteInsuranceIdAndDeletedFalse(quoteInsuranceId)
                .orElseThrow(() -> new ResourceNotFoundException("Cyber Insurance not found for QuoteInsurance ID: " + quoteInsuranceId));

        Client client = quoteInsurance.getQuote().getClient();
        String businessTypeStr = client.getBusinessType();

        BusinessType businessType;
        try {
            businessType = BusinessType.valueOf(businessTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid business type: " + businessTypeStr);
        }

        BusinessRiskFactor riskFactor = businessRiskFactorRepository.findByBusinessType(businessType)
                .orElseThrow(() -> new ResourceNotFoundException("Risk factor not found for BusinessType: " + businessType));

       
        if (Boolean.TRUE.equals(insurance.getDeleted())) {
            throw new IllegalStateException("Cannot calculate premium for soft-deleted insurance");
        }

        log.info("Calculating Cyber Insurance Premium for QuoteInsurance ID: {}", quoteInsuranceId);
        int baseRate = 800;
        // Only using DB-fetched risk factors to calculate base premium
        BigDecimal basePremium = BigDecimal.valueOf(riskFactor.getBusinessRisk())
                .multiply(BigDecimal.valueOf(riskFactor.getDataExposure()))
                .multiply(BigDecimal.valueOf(baseRate))
                .setScale(2, RoundingMode.HALF_UP);


        BigDecimal additionalFactors = getBigDecimal(insurance);


        BigDecimal taxes = basePremium.multiply(BigDecimal.valueOf(0.18)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPremium = basePremium.multiply(additionalFactors).add(taxes).setScale(2,RoundingMode.HALF_UP);

        
        
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

    private static BigDecimal getBigDecimal(CyberInsurance insurance) {
        int securityPosture = 0;
        if(!insurance.getHasCybersecurityTraining()) {
            securityPosture += 1;
        }
        if(!insurance.getUsesFirewallAntivirus()){
            securityPosture+=1;
        }
        if(insurance.getHasPriorCyberIncidents()){
            securityPosture+=1;
        }

        int coverageLimitFactor = 2;
        // int deduc = 1;

        BigDecimal additionalFactors = BigDecimal.valueOf(securityPosture)
                .multiply(BigDecimal.valueOf(coverageLimitFactor))
                .setScale(2,RoundingMode.HALF_UP);
        return additionalFactors;
    }
//    private static BigDecimal premForDataBreach(QuoteInsurance insurance) {
//        boolean hasDataBreachCoverage = insurance.getCoverages().stream()
//                .anyMatch(cov -> "DATA_BREACH".equalsIgnoreCase(cov.getCoverageType()));
//
//        if (!hasDataBreachCoverage) return BigDecimal.ZERO;
//
//        BigDecimal premium = BigDecimal.ZERO;
//
//        CyberInsurance cyberInsurance = insurance.getCyberInsurance();
//        if (cyberInsurance == null || Boolean.TRUE.equals(cyberInsurance.getDeleted())) {
//            return BigDecimal.ZERO;
//        }
//
//        // Add premium based on data handling risk factors
//        if (Boolean.FALSE.equals(cyberInsurance.getUsesFirewallAntivirus())) {
//            premium = premium.add(new BigDecimal("250"));
//        }
//
//        if (Boolean.TRUE.equals(cyberInsurance.getStoresCustomerData())) {
//            premium = premium.add(new BigDecimal("200"));
//        }
//
//        if (Boolean.FALSE.equals(cyberInsurance.getHasCybersecurityTraining())) {
//            premium = premium.add(new BigDecimal("150"));
//        }
//
//        if (Boolean.FALSE.equals(cyberInsurance.getHasDataBackupPolicy())) {
//            premium = premium.add(new BigDecimal("180"));
//        }
//
//
//
//        return premium;
//    }
//
//    private static BigDecimal premForNetworkSecurity(QuoteInsurance insurance) {
//        boolean hasNetworkSecurityCoverage = insurance.getCoverages().stream()
//                .anyMatch(cov -> "NETWORK_SECURITY".equalsIgnoreCase(cov.getCoverageType()));
//
//        if (!hasNetworkSecurityCoverage) return BigDecimal.ZERO;
//
//        BigDecimal premium = BigDecimal.ZERO;
//
//        CyberInsurance cyberInsurance = insurance.getCyberInsurance();
//        if (cyberInsurance == null || Boolean.TRUE.equals(cyberInsurance.getDeleted())) {
//            return BigDecimal.ZERO;
//        }
//
//        // Add premium based on network security-related risk factors
//        if (Boolean.FALSE.equals(cyberInsurance.getUsesFirewallAntivirus())) {
//            premium = premium.add(new BigDecimal("300"));
//        }
//
//        if (Boolean.FALSE.equals(cyberInsurance.getHasCybersecurityTraining())) {
//            premium = premium.add(new BigDecimal("180"));
//        }
//
//        if (Boolean.FALSE.equals(cyberInsurance.getHasDataBackupPolicy())) {
//            premium = premium.add(new BigDecimal("160"));
//        }
//
//
//
//        // You can define additional risk logic here (e.g., VPN usage, patch management, etc.)
//        // premium = premium.add(...);
//
//        return premium;
//    }


}
