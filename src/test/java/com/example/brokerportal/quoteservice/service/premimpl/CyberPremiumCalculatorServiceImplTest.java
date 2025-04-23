package com.example.brokerportal.quoteservice.service.premimpl;

import com.example.brokerportal.quoteservice.entities.*;
import com.example.brokerportal.quoteservice.enums.BusinessType;
import com.example.brokerportal.quoteservice.exceptions.ResourceNotFoundException;
import com.example.brokerportal.quoteservice.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class CyberPremiumCalculatorServiceImplTest {

    @Mock
    private CyberInsuranceRepository cyberInsuranceRepository;

    @Mock
    private QuoteInsuranceRepository quoteInsuranceRepository;

    @Mock
    private PremiumRepository premiumRepository;

    @Mock
    private BusinessRiskFactorRepository businessRiskFactorRepository;

    @Mock
    private CoveragePremiumRepository coveragePremiumRepository;

    @Mock
    private CoverageRepository coverageRepository;

    @InjectMocks
    private CyberPremiumCalculatorServiceImpl premiumCalculatorService;

    private QuoteInsurance quoteInsurance;
    private CyberInsurance cyberInsurance;
    private Client client;
    private BusinessRiskFactor businessRiskFactor;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setBusinessType("HEALTHCARE");
        client.setAddress("123 Main St, San Francisco, CALIFORNIA, 94105");

        Quote quote = new Quote();
        quote.setClient(client);

        quoteInsurance = new QuoteInsurance();
        quoteInsurance.setId(1L);
        quoteInsurance.setQuote(quote);

        cyberInsurance = new CyberInsurance();
        cyberInsurance.setId(1L);
        cyberInsurance.setQuoteInsurance(quoteInsurance);
        cyberInsurance.setCoverageLimit(new BigDecimal("5000000"));
        cyberInsurance.setUsesFirewallAntivirus(true);
        cyberInsurance.setHasDataBackupPolicy(true);
        cyberInsurance.setHasCybersecurityTraining(true);
        cyberInsurance.setStoresCustomerData(true);
        cyberInsurance.setDataRecordsVolume(100000);
        cyberInsurance.setDeleted(false);

        businessRiskFactor = new BusinessRiskFactor();
        businessRiskFactor.setBusinessType(BusinessType.HEALTHCARE);
        businessRiskFactor.setBusinessRisk(0.5);
        businessRiskFactor.setDataExposure(0.5);
    }

    @Test
    void calculatePremium_shouldCalculateSuccessfully() {
        // Arrange
        when(quoteInsuranceRepository.findById(1L)).thenReturn(Optional.of(quoteInsurance));
        when(cyberInsuranceRepository.findByQuoteInsuranceIdAndDeletedFalse(1L)).thenReturn(Optional.of(cyberInsurance));
        when(businessRiskFactorRepository.findByBusinessType(BusinessType.HEALTHCARE)).thenReturn(Optional.of(businessRiskFactor));
        when(premiumRepository.save(any(Premium.class))).thenAnswer(i -> i.getArgument(0));
        when(quoteInsuranceRepository.save(any(QuoteInsurance.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        premiumCalculatorService.calculatePremium(1L);

        // Assert
        verify(premiumRepository, times(1)).save(any(Premium.class));
        verify(quoteInsuranceRepository, times(1)).save(any(QuoteInsurance.class));
    }

    @Test
    void calculatePremium_shouldThrowExceptionWhenQuoteInsuranceNotFound() {
        when(quoteInsuranceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> premiumCalculatorService.calculatePremium(1L));
    }

    @Test
    void calculatePremium_shouldThrowExceptionWhenCyberInsuranceNotFound() {
        when(quoteInsuranceRepository.findById(1L)).thenReturn(Optional.of(quoteInsurance));
        when(cyberInsuranceRepository.findByQuoteInsuranceIdAndDeletedFalse(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> premiumCalculatorService.calculatePremium(1L));
    }

    @Test
    void calculatePremium_shouldThrowExceptionWhenBusinessRiskFactorNotFound() {
        when(quoteInsuranceRepository.findById(1L)).thenReturn(Optional.of(quoteInsurance));
        when(cyberInsuranceRepository.findByQuoteInsuranceIdAndDeletedFalse(1L)).thenReturn(Optional.of(cyberInsurance));
        when(businessRiskFactorRepository.findByBusinessType(BusinessType.HEALTHCARE)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> premiumCalculatorService.calculatePremium(1L));
    }
}
