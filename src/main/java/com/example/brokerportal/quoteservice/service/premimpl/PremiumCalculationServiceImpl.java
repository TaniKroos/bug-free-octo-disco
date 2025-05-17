package com.example.brokerportal.quoteservice.service.premimpl;

import com.example.brokerportal.authservice.entities.User;
import com.example.brokerportal.authservice.service.UserService;
import com.example.brokerportal.quoteservice.entities.Quote;
import com.example.brokerportal.quoteservice.entities.QuoteInsurance;
import com.example.brokerportal.quoteservice.enums.AuditAction;
import com.example.brokerportal.quoteservice.enums.QuoteStatus;
import com.example.brokerportal.quoteservice.exceptions.ResourceNotFoundException;
import com.example.brokerportal.quoteservice.repositories.QuoteRepository;
import com.example.brokerportal.quoteservice.service.AuditLogService;
import com.example.brokerportal.quoteservice.service.PremiumCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PremiumCalculationServiceImpl implements PremiumCalculationService {

    private final QuoteRepository quoteRepository;
    private final CyberPremiumCalculatorService cyberPremiumCalculatorService;
    private final PropertyPremiumCalculatorService propertyPremiumCalculatorService;
    private final GeneralLiabilityPremiumCalculatorService generalPremiumCalculatorService;
    private final UserService userService;
    private final AuditLogService auditLogService;

    @Override
    public void calculatePremiumForQuote(Long quoteId) {

        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote not found with ID: " + quoteId));

        if (quote.isDeleted()) {
            throw new ResourceNotFoundException("Quote with this id has been marked soft deleted");
        }
        if (QuoteStatus.valueOf(quote.getStatus()).equals(QuoteStatus.BOUND)) {
            throw new IllegalStateException("Quote is already bound and cannot be modified.");
        }
        authorizeBrokerAccess(quote);

        // Create a thread pool (adjust the pool size based on your needs)
        ExecutorService executorService = Executors.newFixedThreadPool(3); // e.g., 3 threads for 3 insurance types

        // Collect all tasks (Runnable/Callable) for parallel execution
        List<Callable<Void>> tasks = new ArrayList<>();

        for (QuoteInsurance insurance : quote.getInsurances()) {
            if (!Boolean.TRUE.equals(insurance.isSelected())) continue;

            // Add a task for each selected insurance
            switch (insurance.getInsuranceType().toUpperCase()) {
                case "CYBER" -> tasks.add(() -> {
                    cyberPremiumCalculatorService.calculatePremium(insurance.getId());
                    return null;
                });
                case "PROPERTY" -> tasks.add(() -> {
                    propertyPremiumCalculatorService.calculatePremium(insurance.getId());
                    return null;
                });
                case "GENERAL" -> tasks.add(() -> {
                    generalPremiumCalculatorService.calculatePremium(insurance.getId());
                    return null;
                });
                default -> log.warn("Unknown insurance type: {}", insurance.getInsuranceType());
            }
        }

        try {
            // Execute all tasks in parallel and wait for completion
            executorService.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupt flag
            throw new RuntimeException("Premium calculation was interrupted", e);
        } finally {
            executorService.shutdown(); // Always shut down the executor
        }

        // Proceed with audit logging after all calculations are done
        String performedBy = userService.getCurrentUser().getEmail();
        String changedDetails = "Premium generated for Quote ID: " + quote.getId() +
                ", Status: " + quote.getStatus();

        quote.setStatus("PENDING");
        auditLogService.logAction(AuditAction.PREMIUM_CALCULATED, quote, changedDetails, performedBy);
    }
    @Override
    public Double getPremium(Long quoteId){
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("No quote with this id"));
        if(quote.isDeleted()){
            throw new ResourceNotFoundException("QUote with this id has been marked soft deleted");
        }
//        if (QuoteStatus.valueOf(quote.getStatus()).equals(QuoteStatus.BOUND)) {
//            throw new IllegalStateException("Quote is already bound and cannot be modified.");
//        }
        authorizeBrokerAccess(quote);
        Double finalprem = 0.0;
        for(QuoteInsurance qi:quote.getInsurances()){
            if(!Boolean.TRUE.equals(qi.isSelected())) continue;
            finalprem = finalprem  +  qi.getPremium().getTotalPremium();
        }
        return finalprem;
    }

    private void authorizeBrokerAccess(Quote quote) {
        User user = userService.getCurrentUser();
        if (!quote.getBroker().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to access or modify this quote");
        }
    }
}

