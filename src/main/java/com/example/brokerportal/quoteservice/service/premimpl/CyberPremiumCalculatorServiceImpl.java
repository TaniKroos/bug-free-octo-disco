package com.example.brokerportal.quoteservice.service.premimpl;

import com.example.brokerportal.quoteservice.entities.*;
import com.example.brokerportal.quoteservice.enums.BusinessType;
import com.example.brokerportal.quoteservice.enums.CoverageType;
import com.example.brokerportal.quoteservice.exceptions.ResourceNotFoundException;
import com.example.brokerportal.quoteservice.repositories.*;
import jakarta.mail.Address;
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
    private static final BigDecimal BASE_RATE_PER_MILLION = new BigDecimal("1000"); // Base rate per $1M coverage
    private static final BigDecimal MINIMUM_PREMIUM = new BigDecimal("500");
    private static final BigDecimal MAXIMUM_RISK_FACTOR = new BigDecimal("3.0");

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


        // Store coverage-specific premiums
        calculateAndStoreCoveragePremiums(quoteInsurance, insurance);
        final BigDecimal[] basePremium = {calculateBasePremium(riskFactor, insurance)};
        ;

        List<CoveragePremium> cps = coveragePremiumRepository.findByQuoteInsurance(quoteInsurance);
        List<Coverage> covs= coverageRepository.findByQuoteInsurance(quoteInsurance);
        for (Coverage cov : covs) {
            CoverageType type = CoverageType.valueOf(cov.getCoverageType());

            cps.stream()
                    .filter(cp -> cp.getCoverageType() == type)
                    .findFirst()
                    .ifPresent(cp -> basePremium[0] = basePremium[0].add(cp.getPremiumAmount()));
        }

        BigDecimal stateTaxRate = calculateTaxRateBasedOnState(client.getAddress());
        BigDecimal taxes = basePremium[0].multiply(stateTaxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPremium = basePremium[0].add(taxes);

        // Save base premium and taxes in Premium table
        Premium premium = Optional.ofNullable(quoteInsurance.getPremium()).orElseGet(Premium::new);
        premium.setQuoteInsurance(quoteInsurance);
        premium.setBasePremium(basePremium[0].doubleValue());
        premium.setTaxes(taxes.doubleValue());
        premium.setTotalPremium(totalPremium.doubleValue());

        premiumRepository.save(premium);
        quoteInsurance.setPremium(premium);
        quoteInsuranceRepository.save(quoteInsurance);

        log.info("Cyber Premium calculated: base={}, taxes={}, total={}", basePremium[0], taxes, totalPremium);
    }



    /**
     * Extracts the state from a plain string address.
     * Assumes the format is: "Street, City, State, Zip"
     *
     * @param address the full address as a String
     * @return the state part of the address, or null if not found
     */
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

    private void calculateAndStoreCoveragePremiums(QuoteInsurance quoteInsurance, CyberInsurance insurance) {
        List<Coverage> selectedCoverages = quoteInsurance.getCoverages();

        // Soft delete existing coverage premiums
        for (Coverage coverage : selectedCoverages) {
            CoverageType type = CoverageType.fromStringSafe(coverage.getCoverageType());
            if (type == null) {
                log.warn("Invalid CoverageType: {}", coverage.getCoverageType());
                continue;
            }

            BigDecimal coverageAmount = coverage.getCoverageAmount();
            BigDecimal premiumAmount = calculatePremiumByCoverageType(type, insurance);

            // Check if already exists
            CoveragePremium coveragePremium = coveragePremiumRepository
                    .findByQuoteInsuranceAndCoverageTypeAndDeletedFalse(quoteInsurance, type)
                    .orElseGet(() -> {
                        CoveragePremium cp = new CoveragePremium();
                        cp.setQuoteInsurance(quoteInsurance);
                        cp.setCoverageType(type);
                        return cp;
                    });

            coveragePremium.setCoverageAmount(coverageAmount);
            coveragePremium.setPremiumAmount(premiumAmount);
            coveragePremium.setDeleted(false);

            coveragePremiumRepository.save(coveragePremium);
        }
    }

    private BigDecimal calculateBasePremium(BusinessRiskFactor riskFactor, CyberInsurance insurance) {
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

    private BigDecimal calculatePremiumByCoverageType(CoverageType type, CyberInsurance insurance) {
        if (type == null || insurance == null) return BigDecimal.ZERO;

        BigDecimal baseMultiplier = switch (type) {
            case DATA_BREACH -> new BigDecimal("0.015");
            case NETWORK_SECURITY -> new BigDecimal("0.012");
            default -> BigDecimal.ZERO;
        };

        if (baseMultiplier.equals(BigDecimal.ZERO)) return BigDecimal.ZERO;

        BigDecimal riskMultiplier = calculateRiskMultiplier(type, insurance);

        BusinessRiskFactor businessRisk = businessRiskFactorRepository.findByBusinessType(
                        parseBusinessType(insurance.getQuoteInsurance().getQuote().getClient().getBusinessType()))
                .orElseThrow(() -> new ResourceNotFoundException("Risk factor not found"));

        BigDecimal basePremium = calculateBasePremium(businessRisk, insurance);

        return basePremium
                .multiply(baseMultiplier)
                .multiply(riskMultiplier)
                .setScale(2, RoundingMode.HALF_UP);
    }



    private BusinessType parseBusinessType(String businessTypeStr) {
        try {
            return BusinessType.valueOf(businessTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid business type: " + businessTypeStr);
        }
    }

    private BigDecimal calculateRiskMultiplier(CoverageType type, CyberInsurance insurance) {
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
                    .divide(new BigDecimal("1000000"), 4, RoundingMode.HALF_UP); // e.g. 2M = 2.0
            multiplier = multiplier.add(volumeFactor); // 1.0 means +100%
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
