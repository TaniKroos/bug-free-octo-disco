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
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GeneralLiabilityPremiumCalculatorServiceImplTest {

    @Mock
    private GeneralLiabilityInsuranceRepository generalInsuranceRepository;

    @Mock
    private QuoteInsuranceRepository quoteInsuranceRepository;

    @Mock
    private PremiumRepository premiumRepository;

    @Mock
    private BusinessRiskFactorRepository businessRiskFactorRepository;

    @InjectMocks
    private GeneralLiabilityPremiumCalculatorServiceImpl premiumCalculatorService;

    private QuoteInsurance quoteInsurance;
    private GeneralLiabilityInsurance insurance;
    private BusinessRiskFactor riskFactor;
    private Client client;

    @BeforeEach
    void setUp() {
        // Mock objects setup
        client = new Client();
        client.setBusinessType("OUTLET");
        client.setAddress("123 Main St, Los Angeles, CALIFORNIA");

        Quote quote = new Quote();
        quote.setClient(client);

        quoteInsurance = new QuoteInsurance();
        quoteInsurance.setQuote(quote);

        insurance = new GeneralLiabilityInsurance();
        insurance.setCoverageLimit(new BigDecimal("2000000")); // 2 million limit
        insurance.setAnnualPayroll(new BigDecimal("500000")); // 500K payroll
        insurance.setDeductible(new BigDecimal("10000")); // 10K deductible
        insurance.setHasPriorClaims(true);
        insurance.setRiskClassification("HIGH");
        insurance.setBusinessAreaSqft(6000);
        insurance.setClientInteractionLevel("HIGH");
        insurance.setAdditionalInsuredRequired(true);
        insurance.setNumberOfClaims(3);

        riskFactor = new BusinessRiskFactor();
        riskFactor.setLiabilityExposureFactor(1.5);
        riskFactor.setConstructionTypeFactor(1.2);
        riskFactor.setOccupancyRiskFactor(1.3);
    }

    @Test
    void testCalculatePremium() {
        // Setup mock behaviors
        when(quoteInsuranceRepository.findById(anyLong())).thenReturn(Optional.of(quoteInsurance));
        when(generalInsuranceRepository.findByQuoteInsuranceIdAndDeletedFalse(anyLong())).thenReturn(Optional.of(insurance));
        when(businessRiskFactorRepository.findByBusinessType(any())).thenReturn(Optional.of(riskFactor));

        // Act
        premiumCalculatorService.calculatePremium(1L);

        // Verify the premium calculation
        ArgumentCaptor<Premium> premiumCaptor = ArgumentCaptor.forClass(Premium.class);
        verify(premiumRepository).save(premiumCaptor.capture());

        Premium savedPremium = premiumCaptor.getValue();
        assertNotNull(savedPremium);
        assertTrue(savedPremium.getBasePremium() > 0);
        assertTrue(savedPremium.getTaxes() > 0);
        assertTrue(savedPremium.getTotalPremium() > 0);

        verify(quoteInsuranceRepository, times(1)).save(quoteInsurance);
    }

    @Test
    void testCalculatePremium_whenQuoteInsuranceNotFound() {
        when(quoteInsuranceRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> premiumCalculatorService.calculatePremium(1L));
        verify(premiumRepository, never()).save(any());
    }

    @Test
    void testCalculatePremium_whenGeneralLiabilityInsuranceNotFound() {
        when(quoteInsuranceRepository.findById(anyLong())).thenReturn(Optional.of(quoteInsurance));
        when(generalInsuranceRepository.findByQuoteInsuranceIdAndDeletedFalse(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> premiumCalculatorService.calculatePremium(1L));
        verify(premiumRepository, never()).save(any());
    }

    @Test
    void testCalculateBasePremium() {
        // Expected calculation:
        // Coverage: 2M * 400/1M = 800
        // Payroll: 500K * 0.002 = 1000
        // Deductible credit: 10K * 0.0005 = 5
        // Total: 800 + 1000 - 5 = 1795
        BigDecimal expected = new BigDecimal("1795.00");

        BigDecimal actual = premiumCalculatorService.calculateBasePremium(insurance);

        assertEquals(0, expected.compareTo(actual),
                "Expected: " + expected + " but was: " + actual);
    }

    @Test
    void testApplyRiskFactors() {
        // Base premium: 1795
        // Risk factors: 1.5 * 1.2 * 1.3 = 2.34
        // Policy factors:
        // - HIGH risk class: 1.2
        // - Prior claims: 1 + (0.1 * 3) = 1.3
        // - Large premises: 1.1
        // - Additional insured: 1.05
        // - High interaction: 1.15
        // Total policy factor: 1.2 * 1.3 * 1.1 * 1.05 * 1.15 ≈ 2.067
        // Adjusted premium: 1795 * 2.34 * 2.067 ≈ 8680.00

        BigDecimal basePremium = new BigDecimal("1795.00");
        BigDecimal expected = new BigDecimal("8703.32");

        BigDecimal actual = premiumCalculatorService.applyRiskFactors(basePremium, insurance, riskFactor);

        assertEquals(0, expected.compareTo(actual),
                "Expected: " + expected + " but was: " + actual);
    }


    @Test
    void testSavePremium() {
        when(premiumRepository.save(any(Premium.class)))
                .thenAnswer(invocation -> invocation.getArgument(0)); // return the same instance

        premiumCalculatorService.savePremium(
                quoteInsurance,
                new BigDecimal("10000.00"),
                new BigDecimal("750.00"),
                new BigDecimal("10750.00")
        );

        // Capture the saved premium
        ArgumentCaptor<Premium> captor = ArgumentCaptor.forClass(Premium.class);
        verify(premiumRepository).save(captor.capture());
        Premium savedPremium = captor.getValue();

        verify(quoteInsuranceRepository).save(quoteInsurance);

        assertEquals(savedPremium, quoteInsurance.getPremium()); // Same object now
        assertEquals(new BigDecimal("10000.00").doubleValue(), savedPremium.getBasePremium());
        assertEquals(new BigDecimal("750.00").doubleValue(), savedPremium.getTaxes());
        assertEquals(new BigDecimal("10750.00").doubleValue(), savedPremium.getTotalPremium());
    }
}