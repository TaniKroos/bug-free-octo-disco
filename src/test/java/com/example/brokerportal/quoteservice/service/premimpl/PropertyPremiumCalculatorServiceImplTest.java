package com.example.brokerportal.quoteservice.service.premimpl;

import com.example.brokerportal.quoteservice.entities.*;
import com.example.brokerportal.quoteservice.repositories.*;
import com.example.brokerportal.quoteservice.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PropertyPremiumCalculatorServiceImplTest {

    @Mock
    private PropertyInsuranceRepository propertyInsuranceRepository;

    @Mock
    private QuoteInsuranceRepository quoteInsuranceRepository;

    @Mock
    private PremiumRepository premiumRepository;

    @Mock
    private BusinessRiskFactorRepository businessRiskFactorRepository;

    @InjectMocks
    private PropertyPremiumCalculatorServiceImpl propertyPremiumCalculatorService;

    private QuoteInsurance quoteInsurance;
    private PropertyInsurance propertyInsurance;
    private BusinessRiskFactor businessRiskFactor;
    private Client client;

    @BeforeEach
    void setUp() {
        // Setup mock data
        client = new Client();
        client.setBusinessType("HEALTHCARE");
        client.setAddress("123 Main St, New York, NY");

        Quote quote = new Quote();
        quote.setClient(client);

        quoteInsurance = new QuoteInsurance();
        quoteInsurance.setId(1L); // Explicitly set the ID
        quoteInsurance.setQuote(quote);

        propertyInsurance = new PropertyInsurance();
        propertyInsurance.setPropertyValue(new BigDecimal("1000000"));
        propertyInsurance.setCoverageLimit(new BigDecimal("500000"));
        propertyInsurance.setDeductible(new BigDecimal("10000"));
        propertyInsurance.setBuildingAge(15);
        propertyInsurance.setHasFireAlarmSystem(false);
        propertyInsurance.setHasSecuritySystem(false);
        propertyInsurance.setHasSprinklerSystem(false);
        propertyInsurance.setIsCompliantWithLocalCodes(false);
        propertyInsurance.setBusinessInterruptionCoverRequired(true);
        propertyInsurance.setBusinessInterruptionLimit(new BigDecimal("100000"));

        businessRiskFactor = new BusinessRiskFactor();
        businessRiskFactor.setConstructionTypeFactor(1.2);
        businessRiskFactor.setLocationRiskFactor(1.1);
        businessRiskFactor.setTheftProtectionFactor(1.0);
        businessRiskFactor.setOccupancyRiskFactor(1.0);
        businessRiskFactor.setLiabilityExposureFactor(1.0);
        businessRiskFactor.setEmployeeRiskFactor(1.0);
    }

    @Test
    void testCalculatePremium() {
        // Mock repository calls
        when(quoteInsuranceRepository.findById(1L)).thenReturn(Optional.of(quoteInsurance));
        when(propertyInsuranceRepository.findByQuoteInsuranceIdAndDeletedFalse(1L)).thenReturn(Optional.of(propertyInsurance));
        when(businessRiskFactorRepository.findByBusinessType(any())).thenReturn(Optional.of(businessRiskFactor));
        when(premiumRepository.save(any(Premium.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Call the method
        propertyPremiumCalculatorService.calculatePremium(1L);

        // Verify interactions
        verify(quoteInsuranceRepository).findById(1L);
        verify(propertyInsuranceRepository).findByQuoteInsuranceIdAndDeletedFalse(1L);
        verify(businessRiskFactorRepository).findByBusinessType(any());
        verify(premiumRepository).save(any(Premium.class));
        verify(quoteInsuranceRepository).save(quoteInsurance);
    }

    @Test
    void testCalculatePremium_QuoteInsuranceNotFound() {
        when(quoteInsuranceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            propertyPremiumCalculatorService.calculatePremium(1L);
        });

        verify(quoteInsuranceRepository).findById(1L);
        verifyNoMoreInteractions(propertyInsuranceRepository, businessRiskFactorRepository, premiumRepository);
    }

    @Test
    void testCalculateCorePremiumComponents() {
        BigDecimal premium = propertyPremiumCalculatorService.calculateCorePremiumComponents(propertyInsurance);
        premium = premium.setScale(2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("590.00"), premium);
    }

    @Test
    void testCalculateStructuralFactor() {
        BigDecimal factor = propertyPremiumCalculatorService.calculateStructuralFactor(propertyInsurance);
        assertEquals(new BigDecimal("1.77"), factor);
    }

    @Test
    void testApplyBusinessRiskFactors() {
        BigDecimal basePremium = new BigDecimal("1000.00");
        BigDecimal premiumWithRiskFactors = propertyPremiumCalculatorService.applyBusinessRiskFactors(basePremium, businessRiskFactor);
        assertEquals(new BigDecimal("1320.00"), premiumWithRiskFactors); // 1000 * 1.2 * 1.1
    }

    @Test
    void testCalculateBusinessInterruptionPremium() {
        BigDecimal biPremium = propertyPremiumCalculatorService.calculateBusinessInterruptionPremium(propertyInsurance, quoteInsurance, businessRiskFactor);
        assertEquals(new BigDecimal("800.00"), biPremium);
    }

    public BigDecimal calculateTaxRateBasedOnState(String address) {
        if (address == null) {
            return new BigDecimal("0.18"); // default tax rate
        }

        String lowerAddress = address.toLowerCase();

        if (lowerAddress.contains("ny") || lowerAddress.contains("new york")) {
            return new BigDecimal("0.088"); // NY = 8.8%
        } else if (lowerAddress.contains("ca") || lowerAddress.contains("california")) {
            return new BigDecimal("0.0925"); // CA = 9.25%
        } else if (lowerAddress.contains("tx") || lowerAddress.contains("texas")) {
            return new BigDecimal("0.0825"); // TX = 8.25%
        }

        return new BigDecimal("0.18"); // fallback/default tax rate
    }



}