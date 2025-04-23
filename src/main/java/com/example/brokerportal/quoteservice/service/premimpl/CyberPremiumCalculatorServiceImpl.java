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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static com.example.brokerportal.quoteservice.enums.CoverageType.DATA_BREACH;
import static com.example.brokerportal.quoteservice.enums.CoverageType.NETWORK_SECURITY;

@Service
@RequiredArgsConstructor
@Slf4j
public class CyberPremiumCalculatorServiceImpl implements CyberPremiumCalculatorService {

    private final CyberInsuranceRepository cyberInsuranceRepository;
    private final QuoteInsuranceRepository quoteInsuranceRepository;
    private final PremiumRepository premiumRepository;
    private final BusinessRiskFactorRepository businessRiskFactorRepository;
    private final CoveragePremiumRepository coveragePremiumRepository;
    private final CoverageRepository coverageRepository;

    private static final BigDecimal BASE_RATE_PER_MILLION = new BigDecimal("800");
    private static final BigDecimal MINIMUM_PREMIUM = new BigDecimal("500");
    private static final BigDecimal MAXIMUM_RISK_FACTOR = new BigDecimal("3.0");

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

        Client client = quoteInsurance.getQuote().getClient();
        BusinessType businessType = PremiumUtils.parse(client.getBusinessType());

        BusinessRiskFactor riskFactor = businessRiskFactorRepository.findByBusinessType(businessType)
                .orElseThrow(() -> new ResourceNotFoundException("Risk factor not found for BusinessType: " + businessType));

        log.info("Calculating Cyber Insurance Premium for QuoteInsurance ID: {}", quoteInsuranceId);

        // Calculate base premium using business risk factor and data exposure
        BigDecimal basePremium = calculateBasePremium(riskFactor, insurance);

        // Calculate taxes based on state
        BigDecimal stateTaxRate = PremiumUtils.getTaxRate(client.getAddress());
        BigDecimal taxes = basePremium.multiply(stateTaxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPremium = basePremium.add(taxes);

        // Save premium
        Premium premium = Optional.ofNullable(quoteInsurance.getPremium()).orElseGet(Premium::new);
        premium.setQuoteInsurance(quoteInsurance);
        premium.setBasePremium(basePremium.doubleValue());
        premium.setTaxes(taxes.doubleValue());
        premium.setTotalPremium(totalPremium.doubleValue());

        premiumRepository.save(premium);
        quoteInsurance.setPremium(premium);
        quoteInsuranceRepository.save(quoteInsurance);

        log.info("Cyber Premium calculated: base={}, taxes={}, total={}", basePremium, taxes, totalPremium);
    }

    BigDecimal calculateBasePremium(BusinessRiskFactor riskFactor, CyberInsurance insurance) {
        // Convert risk factors to BigDecimal
        BigDecimal businessRisk = BigDecimal.valueOf(riskFactor.getBusinessRisk());
        BigDecimal dataExposure = BigDecimal.valueOf(riskFactor.getDataExposure());

        // Calculate composite risk factor (normalized to 1.0-3.0 range)
        BigDecimal compositeRisk = businessRisk
                .add(dataExposure.multiply(new BigDecimal("0.7")))
                .max(BigDecimal.ONE)
                .min(MAXIMUM_RISK_FACTOR);

        // Get coverage limit and convert to "millions of dollars" unit
        BigDecimal coverageLimitInMillions = insurance.getCoverageLimit()
                .divide(new BigDecimal("1000000"), 6, RoundingMode.HALF_UP);

        // Calculate base premium with more sophisticated formula
        BigDecimal basePremium = BASE_RATE_PER_MILLION
                .multiply(coverageLimitInMillions)
                .multiply(compositeRisk)
                .setScale(2, RoundingMode.HALF_UP);

        // Apply minimum premium
        return basePremium.max(MINIMUM_PREMIUM);
    }

    BigDecimal calculateRiskMultiplier(CoverageType type, CyberInsurance insurance) {
        BigDecimal multiplier = BigDecimal.ONE;

        boolean noFirewall = Boolean.FALSE.equals(insurance.getUsesFirewallAntivirus());
        boolean noBackup = Boolean.FALSE.equals(insurance.getHasDataBackupPolicy());
        boolean noTraining = Boolean.FALSE.equals(insurance.getHasCybersecurityTraining());
        boolean storesData = Boolean.TRUE.equals(insurance.getStoresCustomerData());
        boolean hadIncidents = Boolean.TRUE.equals(insurance.getHasPriorCyberIncidents());

        // Common risk additions
        if (noFirewall) multiplier = multiplier.add(new BigDecimal("0.25"));
        if (noBackup) multiplier = multiplier.add(new BigDecimal("0.15"));
        if (noTraining) multiplier = multiplier.add(new BigDecimal("0.20"));

        // Volume-based risk (only if data is stored)
        if (storesData && insurance.getDataRecordsVolume() > 100000) {
            BigDecimal volumeFactor = BigDecimal.valueOf(insurance.getDataRecordsVolume())
                    .divide(new BigDecimal("1000000"), 4, RoundingMode.HALF_UP);
            multiplier = multiplier.add(volumeFactor);
        }

        // Prior incidents for NETWORK_SECURITY
        if (type == CoverageType.NETWORK_SECURITY && hadIncidents) {
            multiplier = multiplier.add(BigDecimal.valueOf(insurance.getNumberOfPriorIncidents() * 50));
        }

        // Payment method risk (adds if includes "Credit Card")
        if (Optional.ofNullable(insurance.getPaymentProcessingMethods())
                .orElse("")
                .toLowerCase()
                .contains("credit card")) {
            multiplier = multiplier.add(new BigDecimal("0.10"));
        }

        // Cloud provider risk (adds if using "AWS" or multiple)
        String[] clouds = Optional.ofNullable(insurance.getCloudServicesUsed())
                .orElse("")
                .split(",");
        if (clouds.length > 1) multiplier = multiplier.add(new BigDecimal("0.08"));
        if (Arrays.stream(clouds).map(String::trim).anyMatch(cloud -> cloud.equalsIgnoreCase("AWS")))
            multiplier = multiplier.add(new BigDecimal("0.05"));

        return multiplier;
    }
}